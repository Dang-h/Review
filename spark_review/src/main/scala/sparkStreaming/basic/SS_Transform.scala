package sparkStreaming.basic

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SS_Transform {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkStreaming_Transform")
		val ssc = new StreamingContext(conf, Seconds(3))

		val lineDStream: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop100", 9999)

		// TODO transform为无状态操作，允许在DStream上做Rdd-to-RDD的操作
		// 1.DStream功能能不完善时使用
		// 2.需要代码周期性的执行
		val transformDStream: DStream[(String, Int)] = lineDStream.transform(
			rdd => {
				// Driver端周期性执行
			val value: RDD[(String, Int)] = rdd.flatMap(
				line =>{ // Executor端执行
					line.split(" ")
				}).map((_, 1)).reduceByKey(_ + _)
			value
		})

		transformDStream.print()

		ssc.start()
		ssc.awaitTermination()
	}
}
