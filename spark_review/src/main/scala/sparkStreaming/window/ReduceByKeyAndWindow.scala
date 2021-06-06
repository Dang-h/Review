package sparkStreaming.window

import com.alibaba.fastjson.{JSON, JSONObject, TypeReference}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import utils.Env.makeSSC
import utils.{KfkUtils, OffsetUtil}

/**
 * 每2秒统计10秒内的平均温度
 * 从Kafka获取数据，手动提交偏移量，偏移量由Redis维护
 * 并将结果写入HBase，ClickHouse，ES、MySQL
 */
object ReduceByKeyAndWindow {
	def main(args: Array[String]): Unit = {
		val ssc: StreamingContext = makeSSC("reduceByKeyAndWindow", 2)


		val msgTopic = "msg"
		val cityTopic = "city"
		val group = "window"

		// 获取数据
		val msgDS: InputDStream[ConsumerRecord[String, String]] = getOffsetAndData(msgTopic, "msg", ssc)
		val cityDS: InputDStream[ConsumerRecord[String, String]] = getOffsetAndData(cityTopic, "city", ssc)

		// 处理json数据,将{"city":"4","temperature":37.0}转换成（4，37.0）
		val msgFields = Array("city", "temperature")
		// {"id":"4","name":"杭州"} --> (4,杭州)
		val cityFields = Array("id", "name")

		val msg = json2Map(msgDS, msgFields)
		val city = json2Map(cityDS, cityFields)

		msg.print(10)
		city.print(10)

		// (城市,(温度,出现次数))
//		val sumTemperatureAndCity: DStream[(String, (Double, Int))] = msg.mapValues(temperature => (temperature.toDouble, 1)) // 数据（city,(temperature,1)）
//		  .reduceByKey((t1: (Double, Int), t2: (Double, Int)) => (t1._1 + t2._1, t1._2 + t2._2))
//
//		val result1: DStream[(String, Double)] = sumTemperatureAndCity.mapValues(t => t._1 / t._2)
//		result1.print()

		//		result1.foreachRDD {
		//			rdd => {
		//				rdd.foreachPartition {
		//					resultItr => {
		//
		//						// 数据存入ES
		//
		//						// 数据存入HBase
		//
		//						// 数据存入ClickHouse
		//
		//						// 数据存入MySQL
		//
		//
		//					}
		//				}
		//			}
		//		}


		ssc.start()
		ssc.awaitTermination()
	}

	/**
	 * 获取指定Topic和GroupId的Kafka数据
	 *
	 * @param topic
	 * @param ssc
	 * @param group
	 * @return
	 */
	def getOffsetAndData(topic: String, group: String, ssc: StreamingContext) = {
		var recordDS: InputDStream[ConsumerRecord[String, String]] = null
		// 获取偏移量信息
		val offsetMsg: Map[TopicPartition, Long] = OffsetUtil.getOffset(topic,group)

		// 偏移量不为空，从偏移量位置开始消费
		if (offsetMsg != null && offsetMsg.nonEmpty) {
			recordDS = KfkUtils.getKafkaStream(topic, ssc, offsetMsg, group)
		} else {
			// 没有偏移量信息，从最新位置开始消费
			recordDS = KfkUtils.getKafkaStream(topic, ssc, group)
		}

		recordDS
	}

	def json2Map(inputDS: InputDStream[ConsumerRecord[String, String]], fields: Array[String]) = {

		require(fields.length == 2, "Require 2 fields!")

		inputDS.map(json => {
			// 获取json数据
			val jsonStr: String = json.value()
			// 解析json
			val jsonObject: JSONObject = JSON.parseObject(jsonStr)
			val field1 = jsonObject.get(fields(0)).toString
			val field2 = jsonObject.get(fields(1)).toString
			(field1, field2)
		})
	}
}
