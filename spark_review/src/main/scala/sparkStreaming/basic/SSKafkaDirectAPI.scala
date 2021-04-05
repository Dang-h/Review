package sparkStreaming.basic

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SSKafkaDirectAPI {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setAppName("SparkStreaming_Kafka_DirectAPI").setMaster("local[*]")
		val ssc = new StreamingContext(conf, Seconds(3))

		// 定义Kafka参数
		val kafkaPara: Map[String, Object] = Map[String, Object](
			ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop100:9092,hadoop101:9092,hadoop102:9092",
			ConsumerConfig.GROUP_ID_CONFIG -> "sparkStreaming",
			"key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
			"value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
		)

		// 读取Kafka数据创建DStream
		val kafkaDStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(
			ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](Set("sparkStreaming"), kafkaPara)
		)

		// 将每条消息的kv取出
		val wordCount: DStream[(String, Int)] = kafkaDStream.map(record => record.value()).flatMap(_.split(" ")).map((_, 1)).reduceByKey(_ + _)
		wordCount.print()

		//7.开启任务
		ssc.start()
		ssc.awaitTermination()
	}
}
