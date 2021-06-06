package sparkCore.rddTest

import java.util

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import utils.Env.{makeSS, makeSc}

import scala.collection.mutable
import scala.util.Random

object Optimize {
	def main(args: Array[String]): Unit = {
		val sc: SparkContext = makeSc("dataSkew test")
		val spark: SparkSession = makeSS("dataSkew")

		val rdd1 = sc.makeRDD(List(("a", 1), ("a", 2), ("a", 3), ("a", 4), ("a", 5), ("a", 6), ("b", 7), ("b", 8), ("b", 9)))
		val rdd2 = sc.makeRDD(List(("a", "Male"), ("b", "Female")))

		// TODO 将reduce join转为map join
		// Join时使用广播机制避免shuffle
		// rdd1.join(rdd2)
		val rdd2Broadcast = sc.broadcast(rdd2.collectAsMap())
		val result = rdd1.map(t => {
			val rdd2Map = rdd2Broadcast.value
			if (rdd2Map.contains(t._1)) {
				(t._1, (t._2, rdd2Map(t._1)))
			} else {
				null
			}
		}).filter(_ != null) //.collect().mkString(",")

		val userBaseDF: DataFrame = spark.sql("select * from test.user_base").toDF("id", "name")

		// TODO 两阶段聚合处理数据倾斜
		// 对RDD执行reduceByKey等聚合类shuffle算子，或者sparkSQL使用groupBy进行分组聚合
		// 给key加上随机值聚合一次
		val localAgg = rdd1.map(t2 => {
			val prefix: Int = new Random().nextInt(3)
			(prefix + "_" + t2._1, t2._2)
		}).reduceByKey(_ + _)

		// 聚合后数据取出前缀再聚合一次
		val result2: RDD[(String, Int)] = localAgg.map(t2 => (t2._1.split("_")(1), t2._2)).reduceByKey(_ + _)

		// TODO

			Thread.sleep(3600 * 1000)

		//sc.stop()
	}

	case class UserBase(
						  id:Int,
						  name:String
						  )

	case class CityInfo(id:Int, province:String, area:String)

	case class Student(id:String, name:String,birthday:String,gender:String)

}
