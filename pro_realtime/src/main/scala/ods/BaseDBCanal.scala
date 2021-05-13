package ods

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import utils.{MyKafkaUtil, OffsetManagerUtil}

/**
 * 使用canal同步MySQL中数据
 */
object BaseDBCanal {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setAppName("BaseDBCanalApp").setMaster("local[*]")
		val ssc = new StreamingContext(conf, Seconds(5))

		val topic = "gmall_db_canal"
		val groupId = "base_db_canal_group"

		// TODO 1-从Redis中获取偏移量信息，如果存在偏移量信息则从存储的偏移量开始消费，如果没有则从头消费
		val offsetMap: Map[TopicPartition, Long] = OffsetManagerUtil.getOffset(topic, groupId)
		var recordDStream: InputDStream[ConsumerRecord[String, String]] = null
		if (offsetMap != null && offsetMap.nonEmpty) {
			recordDStream = MyKafkaUtil.getKafkaStream(topic, ssc, offsetMap, groupId)
		} else {
			recordDStream = MyKafkaUtil.getKafkaStream(topic, ssc, groupId)
		}

		// TODO 2-从当前批次读取kafka主题中偏移量范围
		// empty[T]返回一个长度为0的T类型数组
		var offsetRanges = Array.empty[OffsetRange]

		val offsetDStream: DStream[ConsumerRecord[String, String]] = recordDStream.transform {
			rdd => {
				offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
				rdd
			}
		}

		// TODO 提取kafka没条record数据并解析成Json
		val jsonObjDStream: DStream[JSONObject] = offsetDStream.map {
			record => {
				val jsonStr: String = record.value()
				val jsonObj: JSONObject = JSON.parseObject(jsonStr)
				jsonObj
			}
		}

		// TODO 处理每条Json
		jsonObjDStream.foreachRDD {
			rdd => {
				rdd.foreach {
					jsonObj => {
						val opType: String = jsonObj.getString("type")
						if ("INSERT".equals(opType)) {
							val tableName: String = jsonObj.getString("table")
							val dataArr: JSONArray = jsonObj.getJSONArray("data")
							var sendTopic = "ods_" + tableName

							import scala.collection.JavaConverters._
							for (dataJson <- dataArr.asScala) {
								MyKafkaUtil.send(sendTopic, dataJson.toString)
							}
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
