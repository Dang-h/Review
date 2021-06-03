package StructuredStreaming

import org.apache.spark.sql.streaming.{StreamingQuery, Trigger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import utils.Env.makeSS

object WelcomeStructuredStreaming {
	def main(args: Array[String]): Unit = {
		val spark: SparkSession = makeSS("test Structured Streaming")

		import spark.implicits._

		val line: DataFrame = spark.readStream.format("socket")
		  .option("host", "hadoop100")
		  .option("port", 9999)
		  .load()

		val wordCount: DataFrame = line.as[String].flatMap(_.split(" ")).groupBy("value").count()

		val query: StreamingQuery = wordCount.writeStream
		  .outputMode("complete")
		  .trigger(Trigger.ProcessingTime(3000))
		  .format("console")
		  .start()

		query.awaitTermination()
	}
}
