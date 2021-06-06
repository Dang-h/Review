package dwd

import java.text.SimpleDateFormat
import java.util.Date

import bean.{ProvinceInfo, UserInfo}
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

object UserInfoApp {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setAppName("get_user_info").setMaster("local[*]")
		val ssc = new StreamingContext(conf, Seconds(5))

		val topic = "ods_user_info"
		val groupId = "user_info_group"
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


		val userInfoDS: DStream[UserInfo] = offsetRangeDS.map {
			record => {
				val jsonStr: String = record.value()
				val userInfo: UserInfo = JSON.parseObject(jsonStr, classOf[UserInfo])

				// 生日转日期
				val date: Date = new SimpleDateFormat("yyyy-MM-dd").parse(userInfo.birthday)
				val currentTime: Long = System.currentTimeMillis()
				val ageTime: Long = currentTime - date.getTime
				val age: Long = ageTime / 1000L / 60L / 60L / 24L / 365L

				if (age < 20) {
					userInfo.age_group = "20岁及以下"
				} else if (age > 30) {
					userInfo.age_group = "30岁以上"
				} else {
					userInfo.age_group = "21岁到30岁"
				}

				if (userInfo.gender == "M") {
					userInfo.gender_name = "男"
				} else {
					userInfo.gender_name = "女"
				}
				userInfo
			}
		}

		import org.apache.phoenix.spark._
		userInfoDS.foreachRDD{
			rdd => {
				rdd.saveToPhoenix(
					"GMALL_USER_INFO",
					Seq("ID","USER_LEVEL","BIRTHDAY","GENDER","AGE_GROUP","GENDER_NAME"),
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
