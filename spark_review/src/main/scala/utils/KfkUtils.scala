package utils

import java.util
import java.util.Properties

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

object KfkUtils {

	private val prop: Properties = PropUtils.load("cnf.properties")
	private val broker_list: String = prop.getProperty("kafka.broker.list")

	var kfkParam = collection.mutable.Map[String, Object](
		ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> broker_list,
		ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
		ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
		ConsumerConfig.GROUP_ID_CONFIG -> "consumerGroup",
		ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
		// 手动维护偏移量
		ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
	)

	/**
	 * 获取指定Topic数据
	 *
	 * @param topic
	 * @param ssc
	 * @return
	 */
	def getKafkaStream(topic: String, ssc: StreamingContext) = {
		val kfkDS: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
			ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](Array(topic), kfkParam)
		)
		kfkDS
	}

	/**
	 * 获取指定Topic和GropuId数据
	 *
	 * @param topic
	 * @param ssc
	 * @param groupId
	 * @return
	 */
	def getKafkaStream(topic: String, ssc: StreamingContext, groupId: String) = {
		kfkParam(ConsumerConfig.GROUP_ID_CONFIG) = groupId
		val kfkDS: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
			ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](Array(topic), kfkParam)
		)
		kfkDS
	}

	/**
	 * 获取指定Topic和偏移量范围数据
	 *
	 * @param topic
	 * @param ssc
	 * @param offset
	 * @return
	 */
	def getKafkaStream(topic: String, ssc: StreamingContext, offset: Map[TopicPartition, Long]) = {

		val kfkDS: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
			ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](Array(topic), kfkParam, offset)
		)
		kfkDS
	}

	/**
	 * 获取指定Topic，GroupId和偏移量范围数据
	 *
	 * @param topic
	 * @param ssc
	 * @param offset
	 * @param groupId
	 * @return
	 */
	def getKafkaStream(topic: String, ssc: StreamingContext, offset: Map[TopicPartition, Long], groupId: String) = {
		kfkParam(ConsumerConfig.GROUP_ID_CONFIG) = groupId
		val kfkDS: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
			ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](Array(topic), kfkParam, offset)
		)
		kfkDS
	}

	var kafkaProducer: KafkaProducer[String, String] = null

	/**
	 * 发送数据到指定Topic
	 *
	 * @param topic
	 * @param msg
	 * @return
	 */
	def send(topic: String, msg: String) = {
		if (kafkaProducer == null) {
			kafkaProducer = createKafkaProducer
		}
		kafkaProducer.send(new ProducerRecord[String, String](topic, msg))
	}

	/**
	 * 发送数据到指定Topic和GropuId
	 *
	 * @param topic
	 * @param key
	 * @param msg
	 * @return
	 */
	def send(topic: String, key: String, msg: String) = {
		if (kafkaProducer == null) {
			kafkaProducer = createKafkaProducer
		}

		kafkaProducer.send(new ProducerRecord[String, String](topic, key, msg))
	}

	/**
	 * 创建KafkaProducer<p>
	 * 自动提交偏移量
	 *
	 * @return
	 */
	def createKafkaProducer(): KafkaProducer[String, String] = {
		createKafkaProducer(true)
	}

	/**
	 * 创建KafkaProducer
	 *
	 * @param autoCommit 是否自动提交偏移量
	 * @return
	 */
	def createKafkaProducer(autoCommit: java.lang.Boolean) = {
		import collection.JavaConverters._
		kfkParam.remove(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)
		kfkParam.remove(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)
		kfkParam.remove(ConsumerConfig.GROUP_ID_CONFIG)
		// 偏移量维护
		kfkParam(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG) = autoCommit

		val props: util.Map[String, Object] = kfkParam.asJava

		var producer: KafkaProducer[String, String] = null
		try {
			producer = new KafkaProducer[String, String](props)
		} catch {
			case e: Exception => e.printStackTrace()
		}
		producer
	}
}
