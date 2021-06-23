package utils

import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.kafka010.OffsetRange
import redis.clients.jedis.Jedis

import java.util

/**
 * kafka偏移量管理类，用于偏移量读取和保存
 */
object OffsetManagerUtil {


	/**
	 * 从Redis中获取偏移量<br>
	 * [key=>offset:topic:groupId<br>
	 * field=>partitionId <br>
	 * value=> 偏移量值 ]
	 *
	 * @param topic   主题
	 * @param groupId 消费者组
	 * @return 当前消费者组中，消费者主题对应的分区偏移量信息：Map[(Topic,Partition), endOffset]
	 */
	def getOffset(topic: String, groupId: String): Map[TopicPartition, Long] = {

		val jedis: Jedis = MyRedisUtil.getRedisClient()
		var offsetKey: String = "offset:" + topic + ":" + groupId

		// 获取当前消费者组消费的主题和对应分区的偏移量
		// (String,String) = (partitionId,endOffset)
		val offsetMap: util.Map[String, String] = jedis.hgetAll(offsetKey)

		jedis.close()

		import scala.collection.JavaConverters._
		val offsetM: Map[TopicPartition, Long] = offsetMap.asScala.map {
			case (partition, offset) => {
				println("读取分区偏移量:" + partition + ":" + offset)

				(new TopicPartition(topic, partition.toInt), offset.toLong)
			}
		}.toMap

		offsetM
	}

	/**
	 * 将offset信息保存到Redis
	 *
	 * @param topic        主题
	 * @param groupId      消费者组
	 * @param offsetRanges 偏移量范围 (topic, partition, fromOffset, toOffset)
	 */
	def saveOffset(topic: String, groupId: String, offsetRanges: Array[OffsetRange]) = {
		var offsetKey = "offset:" + topic + ":" + groupId

		// 定义一个Java的map集合用于存放每个分区对应的偏移量
		val offsetMap = new util.HashMap[String, String]()

		// 对每个offsetRanges进行遍历，将数据封装近offsetMap
		for (offsetRange <- offsetRanges) {
			// 分区
			val partitionId: Int = offsetRange.partition
			// 起始偏移量
			val startOffset: Long = offsetRange.fromOffset
			// 结束偏移量
			val endOffset: Long = offsetRange.untilOffset

			offsetMap.put(partitionId.toString, endOffset.toString)

			println("保存到分区" + partitionId + ":" + startOffset + "--->" + endOffset)
		}

		val jedis: Jedis = MyRedisUtil.getRedisClient()

		// offsetKey:offset:topic:groupId
		// offsetMap:partitionId:endOffset
		jedis.hmset(offsetKey, offsetMap)

		jedis.close()
	}
}
