package sparkStreaming.req.controller

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import sparkStreaming.req.bean.{CityInfo, RanOpt}
import sparkStreaming.req.util.PropertiesUtil

import java.util.Properties
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object MockRealTime {

	/**
	 * 模拟的数据
	 *
	 * 格式 ：timestamp area city userid adid
	 * 某个时间点 某个地区 某个城市 某个用户 某个广告
	 *
	 * 数据流向 Application => Kafka => SparkStreaming => Analysis
	 */

	def main(args: Array[String]): Unit = {
		//获取配置文件SS_Req_config.properties中kafka配置参数
		// 路径相较于当前工程
		val config: Properties = PropertiesUtil.load("conf/SS_Req_config.properties")

		val brokers: String = config.getProperty("kafka.broker.list")
		val topic = "sparkStreaming"

		// 创建kafka生产者
		val kafkaProducer: KafkaProducer[String, String] = createKafkaProducer(brokers)

		while (true) {
			// 随机产生实时数据并通过Kafka生产者发送到Kafka
			for (line <- generateMockData()) {
				kafkaProducer.send(new ProducerRecord[String, String](topic, line))
				println(line)
			}

			Thread.sleep(2000)
		}
	}

	def generateMockData() = {
		val array= ArrayBuffer[String]()
		val cityRandomOpt: RandomOptions[CityInfo] = RandomOptions(
			RanOpt(CityInfo(1, "北京", "华北"), 30),
			RanOpt(CityInfo(2, "上海", "华东"), 30),
			RanOpt(CityInfo(3, "广州", "华南"), 10),
			RanOpt(CityInfo(4, "深圳", "华南"), 20),
			RanOpt(CityInfo(5, "天津", "华北"), 10)
		)

		val random = new Random()

		// 模拟实时数据
		for (i <- 0 to 50) {
			val timestamp: Long = System.currentTimeMillis()
			val cityInfo: CityInfo = cityRandomOpt.getRandomOpt

			val city_name: String = cityInfo.city_name
			val area: String = cityInfo.area
			val adId: Int = 1 + random.nextInt(6)
			val userId: Int = 1 + random.nextInt(6)

			// 拼接实时数据
			array += timestamp + " " + area + " " + city_name + " " + userId + " " + adId
		}
		array.toArray
	}

	def createKafkaProducer(broker: String): KafkaProducer[String, String] = {
		val prop = new Properties()
		prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
		prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
			"org.apache.kafka.common.serialization.StringSerializer")
		prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
			"org.apache.kafka.common.serialization.StringSerializer")

		// 根据配置创建kafka生产者
		new KafkaProducer[String, String](prop)
	}

}
