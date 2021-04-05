package sparkStreaming.basic

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext, StreamingContextState}

import java.net.URI


object SS_GracefullyStop {

	def main(args: Array[String]): Unit = {

		// TODO 优雅的关闭
		// 优雅的恢复，优雅关闭后重新从检查点恢复数据
		val ssc: StreamingContext = StreamingContext.getActiveOrCreate("./sparkCheckPoint", () => createSSC())

		new Thread(new MonitorStop(ssc)).start()

		ssc.start()
		ssc.awaitTermination()
	}

	def createSSC() = {

		val update: (Seq[Int], Option[Int]) => Some[Int] = (values: Seq[Int], status: Option[Int]) => {
			// 当前批次内容的计算
			val sum: Int = values.sum

			// 取出状态信息中上一次的状态
			val lastStatus: Int = status.getOrElse(0)
			Some(sum + lastStatus)
		}

		val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("testGracefullyStop")

		// 设置优雅的关闭
		sparkConf.set("spark.streaming.stopGracefullyOnShutdown", "true")

		val ssc = new StreamingContext(sparkConf, Seconds(5))

		ssc.checkpoint("sparkCheckPoint")

		val lineDStream: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop100", 9999)

		val wordCount: DStream[(String, Int)] = lineDStream.flatMap(_.split(" ")).map((_, 1)).updateStateByKey(update)

		wordCount.print()

		ssc
	}


	class MonitorStop(ssc: StreamingContext) extends Runnable {

		override def run(): Unit = {
			//实例化Hadoop文件操作
			val fs: FileSystem = FileSystem.get(new URI("hdfs://hadoop100:9820"), new Configuration(), "hadoop")

			while (true) {
				try
					Thread.sleep(5000)
				catch {
					case e: InterruptedException => e.printStackTrace()
				}

				val state: StreamingContextState = ssc.getState()

				val flag: Boolean = fs.exists(new Path("hdfs://hadoop100:9820/stopSpark"))

				if (flag) {
					if (state == StreamingContextState.ACTIVE) {
						ssc.stop(stopSparkContext = true, stopGracefully = true)
						System.exit(0)
					}
				}
			}
		}
	}

}
