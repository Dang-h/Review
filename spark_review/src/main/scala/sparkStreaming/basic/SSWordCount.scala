package sparkStreaming.basic

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SSWordCount {
	def main(args: Array[String]): Unit = {
		//创建环境对象
		val conf: SparkConf = new SparkConf().setAppName("sparkStreamingWordCount").setMaster("local[*]")
		// 第二个参数表示批处理的周期（采集周期；滑动周期必须是采集周期的整数倍）
		val ssc = new StreamingContext(conf, Seconds(3))

		// 获取端口数据
		val lines = ssc.socketTextStream("hadoop100", 9999)
		val wordCount: DStream[(String, Int)] = lines.flatMap(_.split(" ")).map((_, 1)).reduceByKey(_ + _)
		wordCount.print()

		// 由于streaming采集器是7*24执行，不能直接关闭，main方法结束应用程序就会自动结束
		// 启动采集器
		ssc.start()
		// 等待采集器的关闭
		ssc.awaitTermination()

	}
}
