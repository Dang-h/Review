package sparkCore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark_WorkCount2 {
	def main(args: Array[String]): Unit = {
		// 建立和Spark的连接
		val conf: SparkConf = new SparkConf().setMaster("local").setAppName("WordCount")
		val sc = new SparkContext(conf)

		// 执行业务
		val datas: RDD[String] = sc.textFile("input")
		val wordToOne: RDD[(String, Int)] = datas.flatMap(_.split(" ")).map(word => (word, 1))
		// val wordGroup: RDD[(String, Iterable[(String, Int)])] = wordToOne.groupBy { case (word, one) => word }
//		val wordToCount: RDD[(String, Int)] = wordGroup.map { case (word, list) =>
//			list.reduce { case (wordGroup1, wordGroup2) => (wordGroup1._1, wordGroup1._2 + wordGroup2._2) }
//		}

		val word2Count: RDD[(String, Int)] = wordToOne.reduceByKey(_ + _)
		// 关闭连接
		sc.stop()
	}

}
