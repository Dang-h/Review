package utils

import java.util

import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.kafka010.OffsetRange
import redis.clients.jedis.Jedis

import scala.collection.JavaConverters._
import scala.collection.mutable

object OffsetUtil {
	/**
	 * 根据指定topic获取偏移量
	 *
	 * @param topic
	 * @return
	 */
	def getOffset(topic: String) = {
		val jedis: Jedis = RedisUtil.getRedisClient
		val offsetKey = "offset:" + topic

		// 获取当前消费者的主题及其对应偏移量
		// (partition, offset)
		val offsetMap: util.Map[String, String] = jedis.hgetAll(offsetKey)

		jedis.close()

		val parOffset: Map[TopicPartition, Long] = offsetMap.asScala.map {
			case (partition, offset) => {
				println("partition: " + partition + " offset: " + offset)
				(new TopicPartition(topic, partition.toInt), offset.toLong)
			}
		}.toMap

		parOffset

	}
	/**
	 * 获取指定Topic和groupId的偏移量
	 * @param topic
	 * @param groupId
	 * @return
	 */
	def getOffset(topic: String,groupId:String) = {
		val jedis: Jedis = RedisUtil.getRedisClient
		val offsetKey = "groupId - offset:" +groupId + "-" + topic

		// 获取当前消费者的主题及其对应偏移量
		// (partition, offset)
		val offsetMap: util.Map[String, String] = jedis.hgetAll(offsetKey)

		jedis.close()

		val parOffset: Map[TopicPartition, Long] = offsetMap.asScala.map {
			case (partition, offset) => {
				println("partition: " + partition + " offset: " + offset)
				(new TopicPartition(topic, partition.toInt), offset.toLong)
			}
		}.toMap

		parOffset

	}

	/**
	 * 保存指定Topic的偏移量
	 * @param topic
	 * @param offsetRanges
	 */
	def saveOffset(topic: String, offsetRanges: Array[OffsetRange]) = {

		// 用于存放偏移量
		val offsetMap = new util.HashMap[String, String]()
		val jedis = RedisUtil.getRedisClient()
		val offsetKey = "offset:" + topic

		for (offsetRange <- offsetRanges) {
			val partition: Int = offsetRange.partition
			val fromOffset: Long = offsetRange.fromOffset
			val endOffset: Long = offsetRange.untilOffset

			offsetMap.put(partition.toString, endOffset.toString)

			println("partition: " + partition + " ,startOffset: " + fromOffset + "-->" + endOffset + "(endOffset)")
		}

		jedis.hmset(offsetKey, offsetMap)

		jedis.close()
	}

	/**
	 * 保存指定Topic和groupId的偏移量
	 * @param topic
	 * @param groupId
	 * @param offsetRanges
	 */
	def saveOffset(topic: String, groupId:String, offsetRanges: Array[OffsetRange]) = {

		// 用于存放偏移量
		val offsetMap = new util.HashMap[String, String]()
		val jedis = RedisUtil.getRedisClient()
		val offsetKey = "groupId - offset:" +groupId + "-" + topic

		for (offsetRange <- offsetRanges) {
			val partition: Int = offsetRange.partition
			val fromOffset: Long = offsetRange.fromOffset
			val endOffset: Long = offsetRange.untilOffset

			offsetMap.put(partition.toString, endOffset.toString)

			println("partition: " + partition + " ,startOffset: " + fromOffset + "-->" + endOffset + "(endOffset)")
		}

		jedis.hmset(offsetKey, offsetMap)

		jedis.close()
	}
}
