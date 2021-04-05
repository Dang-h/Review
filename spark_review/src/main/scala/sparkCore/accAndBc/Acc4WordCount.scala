package sparkCore.accAndBc

import org.apache.spark.util.AccumulatorV2
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object Acc4WordCount {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("AccForWordCount")
		val sc = new SparkContext(conf)

		val rdd = sc.makeRDD(List("hello", "spark", "hello", "hello"))

		// 创建累加器对象
		val wcAcc = new MyAcc()
		// 想spark注册累加器
		sc.register(wcAcc, "wordCount")

		rdd.foreach(
			word => wcAcc.add(word)
		)
		// 获得累加器结果
		println(wcAcc.value)

		sc.stop()
	}
}

/*
自定义累加器
1. 继承AccumulatorV2， 定义输入和输出类型的泛型
2. 重写6个方法
 */
class MyAcc extends AccumulatorV2[String, mutable.Map[String, Long]] {

	private var wcMap: mutable.Map[String, Long] = mutable.Map[String, Long]()

	// 判断是否是初始状态
	override def isZero: Boolean = wcMap.isEmpty

	override def copy(): AccumulatorV2[String, mutable.Map[String, Long]] = new MyAcc

	override def reset(): Unit = wcMap.clear()

	// 获取累加器需要计算的值
	override def add(word: String): Unit = {
		val newCount: Long = wcMap.getOrElse(word, 0L) + 1
		wcMap.update(word, newCount)
	}

	// Driver端合并多个累加器
	override def merge(othr: AccumulatorV2[String, mutable.Map[String, Long]]): Unit = {
		val map1: mutable.Map[String, Long] = this.wcMap
		val map2: mutable.Map[String, Long] = othr.value

		map2.foreach {
			case (word, count) => {
				val newCount: Long = map1.getOrElse(word, 0L) + count
				map1.update(word, newCount)
			}
		}
	}

	// 累加器结果
	override def value: mutable.Map[String, Long] = wcMap
}
