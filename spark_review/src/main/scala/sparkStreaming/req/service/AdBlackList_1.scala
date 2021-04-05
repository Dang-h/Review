package sparkStreaming.req.service

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.datanucleus.store.rdbms.JDBCUtils
import sparkStreaming.req.bean.AdClickData
import sparkStreaming.req.controller.BlackListHandler
import sparkStreaming.req.util.JDBCUtil

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer

object AdBlackList_1 {

	// 实现实时的动态黑名单机制：将每天对某个广告点击超过 100 次的用户拉黑

	// 从kafka读取数据，和MySQL中黑名单数据做校验
	// 通过校验的继续校验当前点击数是否超过阈值,超过阈值的加入黑名单
	// 未超过阈值的更新当天点击量
	def main(args: Array[String]): Unit = {
		val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkStreaming")
		val ssc = new StreamingContext(sparkConf, Seconds(3))

		// kafka配置
		val kafkaPara: Map[String, Object] = Map[String, Object](
			ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop100:9092,hadoop100:9092,hadoop100:9092",
			ConsumerConfig.GROUP_ID_CONFIG -> "spark",
			"key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
			"value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
		)

		// 使用Driect模式从kafka中取数据
		val kafkaData: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
			ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](Set("sparkStreaming"), kafkaPara)
		)

		val adClickData: DStream[AdClickData] = kafkaData.map(
			dataFromKafka => {
				val response: String = dataFromKafka.value()
				val datas: Array[String] = response.split(" ")
				AdClickData(datas(0), datas(1), datas(2), datas(3), datas(4))
			}
		)


		// 将数据过筛
		val filteredByBlackListData: DStream[AdClickData] = BlackListHandler.filterByBlackList(adClickData)

		// 将初筛未在黑名单中的用户再次过筛
		BlackListHandler.addBlackList(filteredByBlackListData)

		filteredByBlackListData.cache()
		filteredByBlackListData.count().print()

		ssc.start()
		ssc.awaitTermination()
	}

}
