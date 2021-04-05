package sparkSQL

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkSQL_Basic {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setAppName("sparkSQLBasic").setMaster("local[*]")
		val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()

		val df: DataFrame = spark.read.json("input/testSparkSql.json")
		//df.show()

		df.createOrReplaceTempView("user")
		spark.sql("select * from user")

		spark.udf.register("addName" , (s:String) => "Name:" + s)
		spark.sql("select addName(username), age from user")

		spark.close()
	}
}
