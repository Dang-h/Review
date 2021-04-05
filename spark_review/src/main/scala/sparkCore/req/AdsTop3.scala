package sparkCore.req

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object AdsTop3 {
	def main(args: Array[String]): Unit = {
		// 数据格式：agent.log：时间戳，省份，城市，用户，广告，中间字段使用空格分隔
		// 需求：统计出每一个省份 每个广告被点击数量排行的 Top3
		// 结果示例：(省份，广告，点击次数)
		val conf: SparkConf = new SparkConf().setAppName("AdsTop3").setMaster("local[*]")
		val sc = new SparkContext(conf)

		// 导入数据
		val data: RDD[String] = sc.textFile("data/agent.log")

		// 原始数据结构转换==> ((省份，广告), 1)
		val mapRDD: RDD[((String, String), Int)] = data.map(
			line => {
				val datas: Array[String] = line.split(" ")
				((datas(1), datas(4)), 1)
			}
		)
		// 按照(省份，广告)聚合并转换结构==>(省份， (广告，sum))
		val newMapRDD: RDD[(String, (String, Int))] = mapRDD.reduceByKey(_ + _).map {
			case ((prov, ad), sum) => (prov, (ad, sum))
		}
		// 按省份分组，排序取前三
		val resultRDD: RDD[(String, List[(String, Int)])] = newMapRDD.groupByKey().mapValues(
			iter => iter.toList.sortBy(_._2)(Ordering.Int.reverse).take(3)
		)

		resultRDD.collect.foreach(println)

		sc.stop()
	}

}
