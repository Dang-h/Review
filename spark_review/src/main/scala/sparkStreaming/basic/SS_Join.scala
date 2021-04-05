package sparkStreaming.basic

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SS_Join {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkStreaming_Join")
		val ssc = new StreamingContext(conf, Seconds(3))

		// TODO 双流join,需要两个流的批次大小一样才能同时触发计算
		// 通过两个不同的数据流将相同key的数据join在一起
		val lineDStream1: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop100", 9999)
		val lineDStream2: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop100", 8888)

		// 转换两个流的结构
		val mapDStream1: DStream[(String, String)] = lineDStream1.flatMap(_.split(" ")).map((_, "hadoop100"))
		val mapDStream2: DStream[(String, String)] = lineDStream2.flatMap(_.split(" ")).map((_, "hadoop101"))

		// 双流join
		val joinDStream: DStream[(String, (String, String))] = mapDStream1.join(mapDStream2)
		joinDStream.print()

		ssc.start()
		ssc.awaitTermination()
	}
}
