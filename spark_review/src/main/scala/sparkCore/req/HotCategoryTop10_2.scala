package sparkCore.req

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

// Top10的热门品类
/*
数据格式：
0用户点击行为日期，1用户ID，2SessionID，3页面ID，4动作时间，5搜索关键词，6品类ID，7商品ID，8一次订单品类ID集合，9一次订单商品集合，10一次支付品类ID集合，11一次支付商品集合，12城市ID
热门商品：点击次数，下单次数，支付次数来量化。次数多则热门
 */
object HotCategoryTop10_2 {
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

		/* TODO 过滤数据只保留必要数据->品类ID，一次订单品类ID集合，一次支付品类ID集合，
		    	转换结构为：点击的场合 : ( 品类ID，( 1, 0, 0 ) )
		    			  下单的场合 : ( 品类ID，( 0, 1, 0 ) )
		    			  支付的场合 : ( 品类ID，( 0, 0, 1 ) )
		 */
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

		val result: Array[(String, (Int, Int, Int))] = categoryCount.sortBy(_._2, false).take(10)
		// TODO 输出结果
		result.foreach(println)

		sc.stop()
		val end: Long = System.currentTimeMillis()

		println("spendTime = " + (end - start))

	}
}
