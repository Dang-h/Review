package sparkStreaming.window

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import utils.Env.makeSSC
import utils.{KfkUtils, OffsetUtil}

object TestKafkaConsumer {
	def main(args: Array[String]): Unit = {
		val ssc: StreamingContext = makeSSC("testKafkaConsumer", 1)

		val msgTopic = "msg"
		val group = "window"

		var recordDS: InputDStream[ConsumerRecord[String, String]] = null
		// 获取偏移量信息
		val offsetMsg: Map[TopicPartition, Long] = OffsetUtil.getOffset(msgTopic,group)

		// 偏移量不为空，从偏移量位置开始消费
		if (offsetMsg != null && offsetMsg.nonEmpty) {
			recordDS = KfkUtils.getKafkaStream(msgTopic, ssc, offsetMsg, group)
		} else {
			// 没有偏移量信息，从最新位置开始消费
			recordDS = KfkUtils.getKafkaStream(msgTopic, ssc)
		}

		recordDS.print(10)

		ssc.start()
		ssc.awaitTermination()
	}

}
