package sparkCore

import java.util.UUID

import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, Partitioner, SparkContext}
import utils.Env.makeSc

import scala.util.Random

object PartitionerExec {
	def main(args: Array[String]): Unit = {
		val sc: SparkContext = makeSc("exercise partitioner")

		val arr = Array(
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户A", "www.baidu.com"),
			("用户B", "www.baidu.com"),
			("用户B", "www.baidu.com")
		)

		val result: RDD[(String, String)] = sc.makeRDD(arr)
//		  .partitionBy(new HashPartitioner(2)).map(t => (t._1, "http://" + t._2))
		  .partitionBy(new UserPartitioner(2)).map(t => (t._1, "http://" + t._2))
		println(result.collect().mkString(","))

		Thread.sleep(1000 * 3600)
	}

}

/**
 * 自定义分区器缓解数据倾斜
 * @param partitions
 */
class UserPartitioner(partitions: Int) extends Partitioner {

	require(partitions >= 0, s"Number of partitions ($partitions) cannot be negative")

	override def numPartitions: Int = partitions

	def nonNegativeMod(x: Int, mod: Int): Int = {
		val rawMod: Int = x % mod
		rawMod + (if (rawMod < 0) mod else 0)
	}

	/**
	 * 重写key分区规则
	 * @param key
	 * @return
	 */
	override def getPartition(key: Any): Int = {
		key match {
			case null => nonNegativeMod(UUID.randomUUID().hashCode(), numPartitions)
			case _ => nonNegativeMod((key.toString + "_" + UUID.randomUUID()).hashCode, numPartitions)
		}
	}


}