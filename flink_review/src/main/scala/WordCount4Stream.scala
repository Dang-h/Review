import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object WordCount4Stream {
	def main(args: Array[String]): Unit = {
		val params: ParameterTool = ParameterTool.fromArgs(args)


		val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

		val textDS: DataStream[String] = env.socketTextStream("hadoop100", 7777)


		val result: DataStream[(String, Int)] = textDS.flatMap(_.split(" ")).map((_, 1)).keyBy(0).sum(1)

		result.print().setParallelism(1)

		env.execute("Socket stream worCount")
	}

}
