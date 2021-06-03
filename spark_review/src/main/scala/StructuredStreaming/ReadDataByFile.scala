package StructuredStreaming

import org.apache.spark.sql.streaming.{StreamingQuery, Trigger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import utils.Env.makeSS

object ReadDataByFile {

	val citySchema = StructType(List(
		StructField("id", StringType, false),
		StructField("province", StringType, false),
		StructField("area", StringType, false)
	))

	def main(args: Array[String]): Unit = {
		val spark: SparkSession = makeSS("readDataByFile")

		// 读取数据
		val cityInfo: DataFrame = spark.readStream.format("csv").schema(citySchema).load("data/tmp")

		// 创建视图
		cityInfo.createTempView("cityInfo")

		// 操作数据
		val result: DataFrame = spark.sql("select area, concat_ws('|',collect_list(concat(id,'-',province))) province from cityInfo group by area")

		// 执行
		val query: StreamingQuery = result.writeStream.outputMode("complete").trigger(Trigger.ProcessingTime(0)).format("console").start()

		query.awaitTermination()
	}
}
