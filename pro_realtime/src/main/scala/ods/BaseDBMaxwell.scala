package ods

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import utils.{MyKafkaUtil, OffsetManagerUtil}

/**
 * 使用maxwell同步MySQL中数据
 */
object BaseDBMaxwell {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("DB_kafka_Maxwell")
		val ssc = new StreamingContext(conf, Seconds(5))

		val topic = "gmall_db_maxwell"
		val groupId = "base_db_maxwell_group"

		// 从Redis中获取偏移量，偏移量存在则从偏移量位置消费，否则从头开始
		var recordDStream: InputDStream[ConsumerRecord[String, String]] = null
		val offsetMap: Map[TopicPartition, Long] = OffsetManagerUtil.getOffset(topic, groupId)
		if (offsetMap != null && offsetMap.nonEmpty) {
			recordDStream = MyKafkaUtil.getKafkaStream(topic, ssc, offsetMap, groupId)
		} else {
			recordDStream = MyKafkaUtil.getKafkaStream(topic, ssc, groupId)
		}

		//获取当前采集周期对应主题的分区和偏移量
		var offsetRanges = Array.empty[OffsetRange]
		val offsetDStream: DStream[ConsumerRecord[String, String]] = recordDStream.transform {
			rdd => {
				offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
				rdd
			}
		}

		//对读取的数据进行格式转换
		val jsonObjDStream: DStream[JSONObject] = offsetDStream.map {
			record => {
				val jsonStr: String = record.value()
				val jsonObj: JSONObject = JSON.parseObject(jsonStr)
				jsonObj
			}
		}

		// 分流处理
		jsonObjDStream.foreachRDD {
			rdd: RDD[JSONObject] => {
				rdd.foreach {
					jsonObj => {
						val opType: String = jsonObj.getString("type")
						val dataJsonObj: JSONObject = jsonObj.getJSONObject("data")
						val tableName: String = jsonObj.getString("table")

						if (dataJsonObj != null && !dataJsonObj.isEmpty && !"delete".equals(opType)) {
							var sendTopic = "ods_" + tableName
							MyKafkaUtil.send(sendTopic, dataJsonObj.toString)
						}
					}
				}
				// 提交偏移量
				OffsetManagerUtil.saveOffset(topic, groupId, offsetRanges)
			}
		}


		ssc.start()
		ssc.awaitTermination()
	}

}
