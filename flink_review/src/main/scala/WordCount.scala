import org.apache.flink.api.scala.{AggregateDataSet, DataSet, ExecutionEnvironment, createTypeInformation}

object WordCount {

	val path: String = "input/word.txt"

	def main(args: Array[String]): Unit = {
		// 创建环境
		val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment

		val dataInput: DataSet[String] = env.readTextFile(path)

		val result: AggregateDataSet[(String, Int)] = dataInput.flatMap(_.split(" ")).map((_, 1)).groupBy(0).sum(1)

		result.print()
	}
}
