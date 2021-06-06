package dwd

import java.text.SimpleDateFormat
import java.util.Date

import bean.{OrderInfo, ProvinceInfo, UserInfo, UserStatus}
import com.alibaba.fastjson.serializer.SerializeConfig
import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import utils.{MyEsUtil, MyKafkaUtil, OffsetManagerUtil, PhoenixUtil}

/**
 * 读取订单信息，查询用户状态（判断是否是首单）
 */
object dwd_OrderInfoApp {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("dwd_OrderInfoApp")
		val ssc = new StreamingContext(conf, Seconds(5))

		val topic = "ods_order_info"
		val groupId = "order_info_group"

		// TODO 从kafka主题中读取数据
		val offsetMap: Map[TopicPartition, Long] = OffsetManagerUtil.getOffset(topic, groupId)
		var recordDStream: InputDStream[ConsumerRecord[String, String]] = null
		if (offsetMap != null && offsetMap.nonEmpty) {
			recordDStream = MyKafkaUtil.getKafkaStream(topic, ssc, offsetMap, groupId)
		} else {
			recordDStream = MyKafkaUtil.getKafkaStream(topic, ssc, groupId)
		}
		var offsetRanges = Array.empty[OffsetRange]
		val offsetDStream: DStream[ConsumerRecord[String, String]] = recordDStream.transform {
			rdd => {
				offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
				rdd
			}
		}

		val orderInfoDStream: DStream[OrderInfo] = offsetDStream.map {
			record => {
				val jsonStr: String = record.value()
				val orderInfo: OrderInfo = JSON.parseObject(jsonStr, classOf[OrderInfo])
				val create_time: String = orderInfo.create_time
				val createTimeArr: Array[String] = create_time.split(" ")
				orderInfo.create_date = createTimeArr(0)
				orderInfo.create_hour = createTimeArr(1).split(":")(0)
				orderInfo
			}
		}

//		 orderInfoDStream.print(10)
		// TODO 判断是否是首单
		val orderInfoWithFirstFlagDStream: DStream[OrderInfo] = orderInfoDStream.mapPartitions {
			orderInfoItr: Iterator[OrderInfo] => {
				// 当前一个分区中所有订单的集合
				val orderInfoList: List[OrderInfo] = orderInfoItr.toList
				// 取出userId
				val userIdList: List[Long] = orderInfoList.map(_.user_id)
//				println("userIdList=" + userIdList.mkString(","))

				// TODO 坑1：字符串拼接：根据用户集合到Phoenix中查询哪些用户下过单
				val sql = s"select user_id,if_consumed from user_status where user_id in('${userIdList.mkString("','")}')"

				val userStatusList: List[JSONObject] = PhoenixUtil.queryList(sql)

				// TODO 坑2：字段需要大写：获取消费过的用户ID
				val consumerUserIdList: List[String] = userStatusList.map(_.getString("USER_ID"))
//				println("consumerUserIdList=" + consumerUserIdList.mkString(","))
				for (orderInfo <- orderInfoList) {
//					println(orderInfo.toString)
					// TODO 坑3：类型转换Long->String
					if (consumerUserIdList.contains(orderInfo.user_id.toString)) {
						orderInfo.if_first_order = "0"
					} else {
						orderInfo.if_first_order = "1"
					}
				}
				orderInfoList.toIterator
			}
		}
//		orderInfoWithFirstFlagDStream.print(10)

		// TODO 同一批次中数据状态修正
		val orderInfoMapDStream: DStream[(Long, OrderInfo)] = orderInfoWithFirstFlagDStream.map(orderInfo => (orderInfo.user_id, orderInfo))
		// 根据用户ID进行分组
		val orderInfoGroupDStream: DStream[(Long, Iterable[OrderInfo])] = orderInfoMapDStream.groupByKey()

		val orderInfoRealDS: DStream[OrderInfo] = orderInfoGroupDStream.flatMap {
			case (userId, orderInfoItr) => {
				val orderInfoList: List[OrderInfo] = orderInfoItr.toList
				// 判断在一个采集周期中同一用户是否下了多个订单
				if (orderInfoList != null && orderInfoList.size > 1) {
					// 下了多个订单按照下单时间升序排列
					val sortedOrderInfoList: List[OrderInfo] = orderInfoList.sortWith {
						(orderInfo1, orderInfo2) => orderInfo1.create_time < orderInfo2.create_time
					}

					// 取出按下单时间从前往后排序后的第一单
					if (sortedOrderInfoList.head.if_first_order == "1") {
						for (i <- 1 until sortedOrderInfoList.size) {
							sortedOrderInfoList(i).if_first_order = "0"
						}
					}
					sortedOrderInfoList
				} else {
					orderInfoList
				}
			}
		}
//		orderInfoRealDS.print(10)

		// TODO 和省份维度关联
		val orderInfoWithProIdDS: DStream[OrderInfo] = orderInfoRealDS.transform {
			rdd: RDD[OrderInfo] => {
				val sql = "select id,name,area_code,iso_code from gmall_province_info"
				val provinceInfoList: List[JSONObject] = PhoenixUtil.queryList(sql)

				val provinceInfoMap: Map[String, ProvinceInfo] = provinceInfoList.map {
					provinceJson => {
						val provinceInfo: ProvinceInfo = JSON.toJavaObject(provinceJson, classOf[ProvinceInfo])
						(provinceInfo.id, provinceInfo)
					}
				}.toMap

				val pbMap: Broadcast[Map[String, ProvinceInfo]] = ssc.sparkContext.broadcast(provinceInfoMap)

				rdd.map {
					orderInfo => {
						val proInfo: ProvinceInfo = pbMap.value.getOrElse(orderInfo.province_id.toString, null)
						if (proInfo != null) {
							orderInfo.province_name = proInfo.name
							orderInfo.province_area_code = proInfo.area_code
							orderInfo.province_iso_code = proInfo.iso_code
						}
						orderInfo
					}
				}
			}
		}
//		orderInfoWithProIdDS.print(10)

		// TODO 和用户维度关联
		val orderInfoWithUserInfoDS: DStream[OrderInfo] = orderInfoWithProIdDS.mapPartitions {
			orderInfoItr => {
				val orderInfoList: List[OrderInfo] = orderInfoItr.toList
				val userIdList: List[Long] = orderInfoList.map(_.user_id)

				val sql = s"select id,user_level,birthday,gender,age_group,gender_name " +
				  s"from gmall_user_info where id in ('${userIdList.mkString("','")}')"
				val userList: List[JSONObject] = PhoenixUtil.queryList(sql)
				val userInfoMap = userList.map {
					userJson => {
						val userInfo = JSON.toJavaObject(userJson, classOf[UserInfo])
						(userInfo.id, userInfo)
					}
				}.toMap

				for (orderInfo <- orderInfoList) {
					val userInfo = userInfoMap.getOrElse(orderInfo.user_id.toString, null)
					if (userInfo != null) {
						orderInfo.user_age_group = userInfo.age_group
						orderInfo.user_gender = userInfo.gender_name
					}
				}
				orderInfoList.toIterator
			}
		}
		orderInfoWithUserInfoDS.print(10)

		// TODO 维护首单状态，保存到ES中
		import org.apache.phoenix.spark._
		orderInfoWithUserInfoDS.foreachRDD {
			rdd => {
				rdd.cache()

				// 过滤出首单用户
				val firstOrderRDD: RDD[OrderInfo] = rdd.filter(_.if_first_order == "1")
				// 更改用户状态
				val userStatusRDD: RDD[UserStatus] = firstOrderRDD.map {
					orderInfo => UserStatus(orderInfo.user_id.toString, "1")
				}

				// 将用户状态存入HBase
				userStatusRDD.saveToPhoenix(
					"USER_STATUS",
					Seq("USER_ID", "IF_CONSUMED"),
					new Configuration,
					Some("hadoop100,hadoop101,hadoop102:2181")
				)

				// 保存订单数据到ES
				rdd.foreachPartition {
					orderInfoItr => {
						val orderInfoList: List[(String, OrderInfo)] = orderInfoItr.toList.map(orderInfo => (orderInfo.id.toString, orderInfo))
						val dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date())
						MyEsUtil.bulkInsert(orderInfoList, "gmall_order_info_" + dateStr)

						// 数据写回Kafka
						for ((orderInfoId, orderInfo) <- orderInfoList) {
							MyKafkaUtil.send("dwd_order_info", JSON.toJSONString(orderInfo, new SerializeConfig(true)))
						}
					}
				}
			}
		}


		ssc.start()
		ssc.awaitTermination()
	}
}
