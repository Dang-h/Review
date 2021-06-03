package utils

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

import java.util.Properties
import scala.collection.mutable

object MyKafkaUtil {

	private val prop: Properties = MyPropertyUtil.load("config.properties")
	private val broker_list: String = prop.getProperty("kafka.broker.list")

	// Map要加泛型！！！
	var kafkaParam: mutable.Map[String, Object] = collection.mutable.Map[String, Object](
		ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> broker_list,
		ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
		ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
		ConsumerConfig.GROUP_ID_CONFIG -> "gmall",
		ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
		// 手动维护偏移量
		ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
	)

	//	val kafkaParam = collection.mutable.Map(
	//		"bootstrap.servers" -> broker_list,
	//		"key.deserializer" -> classOf[StringDeserializer],
	//		"value.deserializer" -> classOf[StringDeserializer],
	//		//消费者组
	//		"group.id" -> "gmall",
	//		//如果没有初始化偏移量或者当前的偏移量不存在任何服务器上，可以使用这个配置属性
	//		//可以使用这个配置，latest 自动重置偏移量为最新的偏移量
	//		"auto.offset.reset" -> "latest",
	//		//如果是 true，则这个消费者的偏移量会在后台自动提交,但是 kafka 宕机容易丢失数据
	//		//如果是 false，会需要手动维护 kafka 偏移量
	//		"enable.auto.commit" -> (false:java.lang.Boolean)
	//	)

	/**
	 * 使用默认消费者组创建DStream，返回接收的数据
	 *
	 * @param topic topic
	 * @param ssc   SparkSteamingContext
	 * @return
	 */
	def getKafkaStream(topic: String, ssc: StreamingContext): InputDStream[ConsumerRecord[String, String]] = {
		val dStream = KafkaUtils.createDirectStream[String, String](
			ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](Array(topic), kafkaParam),
		)

		dStream
	}


	/**
	 * 对kafka数据进行消费时可指定消费者组
	 *
	 * @param topic
	 * @param ssc
	 * @param groupId
	 * @return
	 */
	def getKafkaStream(topic: String, ssc: StreamingContext, groupId: String): InputDStream[ConsumerRecord[String, String]] = {
		kafkaParam(ConsumerConfig.GROUP_ID_CONFIG) = groupId
		val dStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
			ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](Array(topic), kafkaParam)
		)

		dStream
	}

	/**
	 * 对kafka数据进行消费时可指定消费者组和偏移量
	 *
	 * @param topic
	 * @param ssc
	 * @param offset
	 * @param groupId
	 * @return
	 */
	def getKafkaStream(topic: String, ssc: StreamingContext, offset: Map[TopicPartition, Long], groupId: String) = {
		kafkaParam(ConsumerConfig.GROUP_ID_CONFIG) = groupId

		val dStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
			ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](Array(topic), kafkaParam, offset)
		)

		dStream
	}

	def createKafkaProducer = {
		val properties = new Properties

		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker_list)
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
		properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, (true: java.lang.Boolean))

		var producer: KafkaProducer[String, String] = null
		try {
			producer = new KafkaProducer[String, String](properties)
		} catch {
			case e: Exception => e.printStackTrace()
		}
		producer
	}

	var kafkaProducer: KafkaProducer[String, String] = null
	def send(topic: String, msg: String) = {
		if (kafkaProducer == null) {
			kafkaProducer = createKafkaProducer
		}
		kafkaProducer.send(new ProducerRecord[String, String](topic, msg))
	}

	def send(topic: String, key: String, msg: String) = {
		if (kafkaProducer == null) {
			kafkaProducer = createKafkaProducer
		}

		kafkaProducer.send(new ProducerRecord[String, String](topic, key, msg))
	}
}
