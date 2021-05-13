package app

import bean.DauInfo
import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis
import utils.{MyEsUtil, MyKafkaUtil, MyRedisUtil, OffsetManagerUtil}

import java.lang
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer

/**
 * 日活业务
 */
object DauApp {


	def main(args: Array[String]): Unit = {
		val conf = new SparkConf().setMaster("local[*]").setAppName("DauApp")
		val ssc = new StreamingContext(conf, Seconds(5))
		//		ssc.sparkContext.setLogLevel("ERROR")

		// TODO 消费kafka数据
		val topic = "start"
		val groupId = "gmall_dau"

		// 获取偏移量信息
		val offsetMap: Map[TopicPartition, Long] = OffsetManagerUtil.getOffset(topic, groupId)

		var recordDStream: InputDStream[ConsumerRecord[String, String]] = null
		if (offsetMap != null && offsetMap.nonEmpty) {
			// redis中存在有当前消费者组对该主题的偏移量信息，则从偏移量位置执行
			recordDStream = MyKafkaUtil.getKafkaStream(topic, ssc, offsetMap, groupId)
		} else {
			// redis中没数据则从最新位置开始消费
			recordDStream = MyKafkaUtil.getKafkaStream(topic, ssc, groupId)
		}

		// 获取当前采集周期从Kafka中消费数据的起始偏移量以及结束偏移量
		var offsetRanges = Array.empty[OffsetRange]

		val offsetDStream: DStream[ConsumerRecord[String, String]] = recordDStream.transform {
			rdd => {
				// 获取偏移量范围
				offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
				rdd
			}
		}

		// 测试kafka数据读取
		//recordDStream.map(_.value()).print(10)

		val jsonObjDStream: DStream[JSONObject] = offsetDStream.map {
			record => {
				val jsonStr: String = record.value()
				val jsonObject: JSONObject = JSON.parseObject(jsonStr)
				val ts: lang.Long = jsonObject.getLong("ts")
				val dateStr: String = new SimpleDateFormat("yyyy-MM-dd HH-mm").format(new Date(ts))
				val dateArr: Array[String] = dateStr.split(" ")
				var dt: String = dateArr(0)
				var hr: String = dateArr(1).split("-")(0)
				var min: String = dateArr(1).split("-")(1)

				jsonObject.put("dt", dt)
				jsonObject.put("hr", hr)
				jsonObject.put("mi", min)

				jsonObject
			}
		}

		//jsonObjDStream.print(10)

		// 通过Redis对采集到的启动日志进行去重操作
		val filterDStream: DStream[JSONObject] = jsonObjDStream.mapPartitions {
			jsonObjItr => {
				val jedis: Jedis = MyRedisUtil.getRedisClient()

				val filterList = new ListBuffer[JSONObject]()

				// 对分区数据进行遍历
				for (jsonObj <- jsonObjItr) {
					val dt: String = jsonObj.getString("dt")
					val mid: String = jsonObj.getJSONObject("common").getString("mid")
					var dayKey = "dau" + dt

					// redis向Set类型中添加数据,返回0则说明key已存在，返回1则不存在
					val isFirst: lang.Long = jedis.sadd(dayKey, mid)
					// 设置key的失效时间
					// 2.8及以后版本，如果key不存在返回-1，key失效返回-2
					if (jedis.ttl(dayKey) < 0) {
						jedis.expire(dayKey, 3600 * 24)
					}
					if (isFirst == 1L) {
						filterList.append(jsonObj)
					}
				}

				jedis.close()
				filterList.toIterator
			}
		}

		// filterDStream.count().print()

		// 数据批量写入ES
		filterDStream.foreachRDD {
			rdd => {
				// 以分区对数据进行处理
				rdd.foreachPartition {
					jsonObjItr => {

						val dauInfoList: List[(String, DauInfo)] = jsonObjItr.map {
							jsonObj => {
								val commonJsonObj: JSONObject = jsonObj.getJSONObject("common")

								// 数据封装进样例类
								val dauInfo = DauInfo(
									commonJsonObj.getString("mid"),
									commonJsonObj.getString("uid"),
									commonJsonObj.getString("ar"),
									commonJsonObj.getString("ch"),
									commonJsonObj.getString("vc"),
									jsonObj.getString("dt"),
									jsonObj.getString("hr"),
									jsonObj.getString("mi"),
									jsonObj.getLong("ts")
								)
								(dauInfo.mid, dauInfo)
							}
						}.toList

						val dt: String = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
						MyEsUtil.bulkInsert(dauInfoList, "gmall_dau_info_" + dt)
					}
				}

				OffsetManagerUtil.saveOffset(topic, groupId, offsetRanges)
			}
		}

		ssc.start()
		ssc.awaitTermination()
	}

}
