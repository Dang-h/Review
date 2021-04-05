package sparkCore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark_WorkCount {
	def main(args: Array[String]): Unit = {
		// 建立和Spark的连接
		val conf: SparkConf = new SparkConf().setMaster("local").setAppName("WordCount")
		val sc = new SparkContext(conf)

		// 执行业务
		val datas: RDD[String] = sc.textFile("input")
		val words: RDD[String] = datas.flatMap(_.split(" "))
		val wordGroup: RDD[(String, Iterable[String])] = words.groupBy(w => w)
		val wordMap: RDD[(String, Int)] = wordGroup.map { case (word, list) => (word, list.size) }
		val word2count: Array[(String, Int)] = wordMap.collect()
		word2count.foreach(println)
		// 关闭连接
		sc.stop()
	}

}
