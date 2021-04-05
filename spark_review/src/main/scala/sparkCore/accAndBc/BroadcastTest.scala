package sparkCore.accAndBc

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object BroadcastTest {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setAppName("BroadCastTest").setMaster("local[*]")
		val sc = new SparkContext(conf)

		// (a, (1,4)),(b, (2,5)),(c, (3,6))
		val rdd1 = sc.makeRDD(List(
			("a", 1),("b", 2),("c", 3)
		))
		val map = mutable.Map(("a", 4),("b", 5),("c", 6))

		// 封装广播变量
		val bc: Broadcast[mutable.Map[String, Int]] = sc.broadcast(map)


		rdd1.map{
			case(word, count) => {
				// 方法广播变量
				val l: Int = bc.value.getOrElse(word, 0)
				(word, (count, l))
			}
		}.collect.foreach(println)

		sc.stop()
	}

}
