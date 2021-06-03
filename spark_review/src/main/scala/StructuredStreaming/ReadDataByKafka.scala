package StructuredStreaming

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import utils.Env.makeSS

object ReadDataByKafka {
	def main(args: Array[String]): Unit = {
		val spark: SparkSession = makeSS("ReadDataByKafka")

		import spark.implicits._

		val inputDF: DataFrame = spark
		  .read
		  .format("kafka")
		  .option("kafka.bootstrap.servers", "hadoop100:9092,hadoop101:9092")
		  .option("subscribe", "structured")
		  .option("startingOffsets", "earliest")
		  .option("endingOffsets", "latest")
		  .load()

		// 输入的数据源格式：北京，地铁口A，地铁口C，2（城市名称，进站口，出站口，金额）
		val kvDataDS: Dataset[(String, String)] = inputDF.selectExpr("cast(key as string)", "cast(value as string)").as[(String, String)]

		// 转换格式为（（北京，地铁口A），（北京，地铁口C））
		val subwayDF: DataFrame = kvDataDS.flatMap(t => {
			val arr: Array[String] = t._2.split(",")
			Array((arr(0), arr(1)), (arr(0), arr(2)))
		}).toDF("city", "station_in_or_out")

		subwayDF.createTempView("subway")
		spark.sql("select * from subway").show()

		val result: DataFrame = spark.sql("select city, station_in_or_out, count(*) as hot from subway group by city, station_in_or_out order by city, hot desc")

		result.write.format("console").save()
	}

}
