package sparkStreaming.req.service

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import sparkStreaming.req.bean.AdClickData
import sparkStreaming.req.controller.{AreaCityAdCountHandler, BlackListHandler, LastHourAdCntHandler}
import sparkStreaming.req.util.MyKafkaUtil

object LastHourAdCnt {
	// 实时统计每天各地区各城市各广告的点击总流量，并将其存入 MySQL
	def main(args: Array[String]): Unit = {
		val sparkConf = new SparkConf().setMaster("local[*]").setAppName("LastHourAdCnt")
		val ssc = new StreamingContext(sparkConf, Seconds(2))

		// 获取kafka中数据
		val kafkaData: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(topic =
		  "sparkStreaming", ssc)

		// 数据转换为样例类
		val adClickData: DStream[AdClickData] = kafkaData.map(
			dataFromKafka => {
				val response: String = dataFromKafka.value()
				val datas: Array[String] = response.split(" ")
				AdClickData(datas(0), datas(1), datas(2), datas(3), datas(4))
			}
		)

		// 需求：统计每天各地区各城市各广告的点击总流量，并将其存入 MySQL
		// 先根据数据库中黑名单过滤
		val filteredAdClickCnt: DStream[AdClickData] = BlackListHandler.filterByBlackList(adClickData)

		val result: DStream[(String, List[(String, Long)])] = LastHourAdCntHandler.getAdHourClickCnt(filteredAdClickCnt)

		result.print()


		ssc.start()
		ssc.awaitTermination()
	}
}
