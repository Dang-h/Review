package sparkSQL

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SparkSQL_Hive{
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("sparkSQL_Hive")
		// 连接外置的Hive需要在创建SparkSession时加入Hive支持：enableHiveSupport()
		val spark: SparkSession = SparkSession.builder().enableHiveSupport().config(conf).getOrCreate()

		spark.sql("show databases").show
		spark.sql("use test")
		spark.sql("show tables").show()
		spark.sql("select * from test").show()



		spark.close()
	}
}
