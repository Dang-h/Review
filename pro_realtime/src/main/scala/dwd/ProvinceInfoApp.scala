package dwd

import bean.ProvinceInfo
import com.alibaba.fastjson.JSON
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import utils.{MyKafkaUtil, OffsetManagerUtil}

object ProvinceInfoApp {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setAppName("get_province_info").setMaster("local[*]")
		val ssc = new StreamingContext(conf, Seconds(5))

		val topic = "ods_base_province"
		val groupId = "province_info_group"
		val offsetMap: Map[TopicPartition, Long] = OffsetManagerUtil.getOffset(topic, groupId)

		var recordDS: InputDStream[ConsumerRecord[String, String]] = null;
		if (offsetMap != null && offsetMap.nonEmpty) {
			recordDS = MyKafkaUtil.getKafkaStream(topic, ssc, offsetMap, groupId)
		} else {
			recordDS = MyKafkaUtil.getKafkaStream(topic, ssc, groupId)
		}

		var offsetRanges = Array.empty[OffsetRange]
		val offsetRangeDS: DStream[ConsumerRecord[String, String]] = recordDS.transform {
			rdd => {
				offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
				rdd
			}
		}

		import org.apache.phoenix.spark._
		offsetRangeDS.foreachRDD {
			rdd => {
				val proInfoRdd: RDD[ProvinceInfo] = rdd.map {
					record => {
						val jsonStr: String = record.value()
						val proInfo: ProvinceInfo = JSON.parseObject(jsonStr, classOf[ProvinceInfo])
						proInfo
					}
				}

				proInfoRdd.saveToPhoenix(
					"GMALL_PROVINCE_INFO",
					Seq("ID", "NAME", "AREA_CODE", "ISO_CODE"),
					new Configuration,
					Some("hadoop100,hadoop101,hadoop102:2181")
				)

				OffsetManagerUtil.saveOffset(topic, groupId, offsetRanges)
			}
		}

		ssc.start()
		ssc.awaitTermination()
	}
}
