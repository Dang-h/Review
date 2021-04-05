package sparkCore

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object Spark_WordCount3 {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setAppName("wordCount").setMaster("local[*]")
		val sc = new SparkContext(conf)

		wc9(sc)

		sc.stop()
	}

	// groupBy
	def wc1(sc: SparkContext): Unit = {
		val data: RDD[String] = sc.makeRDD(List("Hello Scala", "Hello Spark"))
		val groupByRdd: RDD[(String, Iterable[String])] = data.flatMap(line => line.split(" ")).groupBy(word => word)
		val result: RDD[(String, Int)] = groupByRdd.mapValues(iter => iter.size)
		result.collect.foreach(println)
	}

	// groupByKey
	def wc2(sc: SparkContext): Unit = {
		val data: RDD[String] = sc.makeRDD(List("Hello Scala", "Hello Spark"))
		val groupByKeyRdd: RDD[(String, Iterable[Int])] = data.flatMap(line => line.split(" ")).map((_, 1)).groupByKey()
		val result: RDD[(String, Int)] = groupByKeyRdd.mapValues(iter => iter.size)

		result.collect.foreach(println)
	}
	// reduceByKey
	def wc3(sc: SparkContext): Unit = {
		val data: RDD[String] = sc.makeRDD(List("Hello Scala", "Hello Spark"))
		val result: RDD[(String, Int)] = data.flatMap(line => line.split(" ")).map((_, 1)).reduceByKey(_ + _)

		result.collect.foreach(println)
	}
	// aggregateByKey
	def wc4(sc: SparkContext): Unit = {
		val data: RDD[String] = sc.makeRDD(List("Hello Scala", "Hello Spark"))
		val result: RDD[(String, Int)] = data.flatMap(line => line.split(" ")).map((_, 1)).aggregateByKey(0)(_ + _, _ + _)

		result.collect.foreach(println)
	}
	// foldByKey
	def wc5(sc: SparkContext): Unit = {
		val data: RDD[String] = sc.makeRDD(List("Hello Scala", "Hello Spark"))
		val result: RDD[(String, Int)] = data.flatMap(line => line.split(" ")).map((_, 1)).foldByKey(0)(_ + _)

		result.collect.foreach(println)
	}
	// combineByKey
	def wc6(sc: SparkContext): Unit = {
		val data: RDD[String] = sc.makeRDD(List("Hello Scala", "Hello Spark"))
		val result: RDD[(String, Int)] = data.flatMap(line => line.split(" "))
		  .map((_, 1))
		  .combineByKey(v => v, (x, y) => x + y, (x, y) => x + y)

		result.collect.foreach(println)
	}
	// countByKey
	def wc7(sc: SparkContext): Unit = {
		val data: RDD[String] = sc.makeRDD(List("Hello Scala", "Hello Spark"))
		val result: collection.Map[String, Long] = data.flatMap(line => line.split(" "))
		  .map((_, 1))
		  .countByKey()

		result.foreach(println)
	}
	// countByValue
	def wc8(sc: SparkContext): Unit = {
		val data: RDD[String] = sc.makeRDD(List("Hello Scala", "Hello Spark"))
		val result: collection.Map[String, Long] = data.flatMap(line => line.split(" "))
		  .countByValue()

		result.foreach(println)
	}
	// reduce/aggregate/fold
	def wc9(sc: SparkContext): Unit = {
		val data: RDD[String] = sc.makeRDD(List("Hello Scala", "Hello Spark"))
		val words: RDD[String] = data.flatMap(line => line.split(" "))
		val mapWord: RDD[mutable.Map[String, Long]] = words.map(word => mutable.Map[String, Long]((word, 1)))
		val result: mutable.Map[String, Long] = mapWord.reduce(
			(map1, map2) => {
				map2.foreach {
					case (word, count) => {
						//println(word)
						val newCount: Long = map1.getOrElse(word, 0L) + 1
						map1.update(word, newCount)
					}
				}
				map1
			}
		)

		println(result)
	}
}
