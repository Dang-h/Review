package sparkStreaming.basic

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SS_Window {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkStreaming_Window")
		val ssc = new StreamingContext(conf, Seconds(3)) // Seconds(3)采集时长，每3秒采集一次，进行一次计算
		ssc.checkpoint("tmp")

		// TODO window(窗口时长，滑动步长），时长和步长都应该是采集时间的整数倍
		//window(windowLength, slideInterval):对源DStream窗口的批次进行计算返回一个新的DStream
		//countByWindow(winLength,slideInterval):统计一个滑窗中的元素数量
		//reduceByWindow(func, winLength, slideInterval):将滑窗中的数据聚合
		//reduceByKeyAndWindow(func,winLength, slideInterval,[numTasks]):将滑窗中的数据按相同的Key聚合
		//reduceByKeyAndWindow(func,invFunc,winLength, slideInterval,[numTasks]):当窗口范围比较大，但是滑动幅度比较小，
		//那么可以采用增加数据和删除数据的方式，无需重复计算，提升性能

		val lineDStream: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop100", 9999)

		val wordToOne: DStream[(String, Int)] = lineDStream.map((_, 1))
		val result: DStream[(String, Int)] = wordToOne.reduceByKeyAndWindow(
			(x:Int, y:Int) => x + y,
			//(x:Int, y:Int) => x - y,
			Seconds(9),
			Seconds(3)
		)

		result.print()

		ssc.start()
		ssc.awaitTermination()
	}

}
