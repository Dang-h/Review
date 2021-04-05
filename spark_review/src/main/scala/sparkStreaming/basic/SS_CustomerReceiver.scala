package sparkStreaming.basic

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket
import java.nio.charset.StandardCharsets

object SS_CustomerReceiver {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setAppName("SparkStreaming_CustomerReceiver").setMaster("local[*]")
		val ssc = new StreamingContext(conf, Seconds(3))

		// 使用自定义receiver
		val messageDS: ReceiverInputDStream[String] = ssc.receiverStream(new MyReceiver("hadoop100", 9999))

		// 做wordCount
		val wcStream: DStream[(String, Int)] = messageDS.flatMap(_.split(" ")).map((_, 1)).reduceByKey(_ + _)

		wcStream.print()

		ssc.start()
		ssc.awaitTermination()
	}

	class MyReceiver(host: String, port: Int) extends Receiver[String](StorageLevel.MEMORY_ONLY) {
		// 启动时调用，作用：读取数据并将数据发送给spark
		override def onStart(): Unit = {
			new Thread("Socket Receiver") {
				override def run(){
					receive()
				}
			}.start()
		}

		// 读取数据并发送给spark
		def receive(): Unit = {
			// 创建一个socket
			val socket = new Socket(host, port)

			// 定义一个变量，用来接收端口传来的数据
			var input : String = null

			// 创建一个BufferedReader用来读取端口传来的数据
			val reader = new BufferedReader(new InputStreamReader(socket.getInputStream, StandardCharsets.UTF_8))

			// 读取数据
			input = reader.readLine()

			// 当receiver没有关闭且输入数据不为空，就循环发送数据给spark
			while (!isStopped() && input != null) {
				store(input)
				input = reader.readLine()
			}

			//跳出循环，关闭资源
			reader.close()
			socket.close()

			// 重启任务
			restart("restart")
		}
		override def onStop(): Unit = {}
	}

}
