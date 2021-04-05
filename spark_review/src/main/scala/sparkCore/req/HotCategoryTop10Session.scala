package sparkCore.req

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

// Top10的热门品类
/*
数据格式：
0用户点击行为日期，1用户ID，2SessionID，3页面ID，4动作时间，5搜索关键词，6品类ID，7商品ID，8一次订单品类ID集合，9一次订单商品集合，10一次支付品类ID集合，11一次支付商品集合，12城市ID
热门商品：点击次数，下单次数，支付次数来量化。次数多则热门
 */
object HotCategoryTop10Session {
	// 优化cogroup；
	// cogroup再数据源不同的情况下会产生shuffle；替换cogroup的操作
	def main(args: Array[String]): Unit = {
		val start: Long = System.currentTimeMillis()
		val conf: SparkConf = new SparkConf().setAppName("HotCategoryTop10_2").setMaster("local[*]")
		val sc = new SparkContext(conf)

		// TODO 获取原始数据
		val datas: RDD[String] = sc.textFile("data/user_visit_action.csv")
		// 同一份数据源使用了多次，缓存
		datas.cache()
		val top10Ids: Array[String] = hotCategoryTop10(datas)

		// TODO 过滤原始数据,保留点击和Top10品类ID的数据
		val categoryIds: RDD[String] = datas.filter(
			data => {
				val ids: String = data.split(",")(6)
				if (ids != "-1")
					top10Ids.contains(ids)
				else false
			}
		)
		//categoryIds.collect.take(10).foreach(println)

		// 转换格式为：(（品类ID，Session），sum)
		val reduceRdd: RDD[((String, String), Int)] = categoryIds.map(
			data => {
				val datas: Array[String] = data.split(",")
				((datas(6), datas(2)), 1)
			}
		).reduceByKey(_ + _)

		// 转换结构为==>(品类ID,(session， sum))并分组
		val groupRdd: RDD[(String, Iterable[(String, Int)])] = reduceRdd.map {
			case ((cId, sId), sum) => (cId, (sId, sum))
		}.groupByKey()

		val result: RDD[(String, List[(String, Int)])] = groupRdd.mapValues(itr => itr.toList.sortBy(_._2)(Ordering.Int.reverse).take(10))

		// TODO 输出结果
		result.collect.foreach(println)

		sc.stop()
		val end: Long = System.currentTimeMillis()

		println("spendTime = " + (end - start))

	}

	def hotCategoryTop10(datas: RDD[String]) = {

		val categoryNum: RDD[(String, (Int, Int, Int))] = datas.flatMap(
			datas => {
				val data: Array[String] = datas.split(",")
				if (data(6) != "-1") {
					// 点击场合
					List((data(6), (1, 0, 0)))
				} else if (data(8).nonEmpty) {
					// 下单场合
					data(8).split("-").map(id => (id, (0, 1, 0)))
				} else if (data(10).nonEmpty) {
					// 支付场合
					data(10).split("-").map(id => (id, (0, 0, 1)))
				} else Nil
			}
		)

		// TODO 根据相同品类ID分组聚合==>（品类ID，(点击数量，下单数量，支付数量))
		val categoryCount: RDD[(String, (Int, Int, Int))] = categoryNum.reduceByKey(
			(t1: (Int, Int, Int), t2: (Int, Int, Int)) => (t1._1 + t2._1, t1._2 + t2._2, t1._3 + t2._3)
		)

		categoryCount.sortBy(_._2, false).take(10).map(_._1)
	}
}
