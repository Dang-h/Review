package sparkSQL

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}

import java.util.Properties

object SparkSQL_JDBC {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("sparkSQL_JDBC")
		val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
		import spark.implicits._

		// TODO 通用的load方式读取
		spark.read.format("jdbc")
		  .option("url", "jdbc:mysql://hadoop100:3306/testcode")
		  .option("driver", "com.mysql.jdbc.Driver")
		  .option("user", "root")
		  .option("password", "mysql")
		  .option("dbtable", "employee")
		  .load() //.show()

		// TODO load方法另一种参数形式
		spark.read.format("jdbc")
		  .options(Map(
			  "url" -> "jdbc:mysql://hadoop100:3306/testcode?user=root&password=mysql",
			  "driver" -> "com.mysql.jdbc.Driver",
			  "dbtable" -> "employee")
		  ).load()//.show()

		// TODO 使用JDBC的方式读取
		val prop = new Properties()
		prop.setProperty("user", "root")
		prop.setProperty("password", "mysql")
		val df: DataFrame = spark.read.jdbc("jdbc:mysql://hadoop100:3306/testcode", "employee", prop)
		//df.show()

		// TODO 通用的写入方式：format指定写入类型
		val rdd: RDD[Employee] = spark.sparkContext.makeRDD(List(Employee("3", 1213.1)))
		// rdd转ds
		val ds: Dataset[Employee] = rdd.toDS()
		ds.write.format("jdbc")
		  .option("url", "jdbc:mysql://hadoop100:3306/testcode")
		  .option("driver", "com.mysql.jdbc.Driver")
		  .option("user", "root")
		  .option("password", "mysql")
		  .option("dbtable", "employee")
		  .mode(SaveMode.Append)
		  .save()

		spark.close()

	}

	case class Employee(id: String, salary: Double)

}
