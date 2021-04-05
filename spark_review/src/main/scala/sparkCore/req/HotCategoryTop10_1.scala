package sparkCore.req

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, TaskContext}

// Top10的热门品类
/*
数据格式：
0用户点击行为日期，1用户ID，2SessionID，3页面ID，4动作时间，5搜索关键词，6品类ID，7商品ID，8一次订单品类ID集合，9一次订单商品集合，10一次支付品类ID集合，11一次支付商品集合，12城市ID
热门商品：点击次数，下单次数，支付次数来量化。次数多则热门
 */
object HotCategoryTop10_1 {
	def main(args: Array[String]): Unit = {
		val start: Long = System.currentTimeMillis()
		val conf: SparkConf = new SparkConf().setAppName("HotCategoryTop10_1").setMaster("local[*]")
		val sc = new SparkContext(conf)

		// TODO 获取原始数据
		val datas: RDD[String] = sc.textFile("data/user_visit_action.csv")

		// TODO 统计品类点击数量：（品类ID，点击数量）
		// 原始数据中第7列为品类ID，不存在为-1
		val clickData: RDD[String] = datas.filter(datas => datas.split(",")(6) != "-1")
		//clickData.collect.take(10).foreach(println)
		val clickCount: RDD[(String, Int)] = clickData.map(datas => (datas.split(",")(6), 1)).reduceByKey(_ + _)

		// TODO 统计品类下单数量：（品类ID，下单数量）
		val orderData: RDD[String] = datas.filter(datas => datas.split(",")(8).nonEmpty)
		//orderData.collect.take(10).foreach(println)
		val orderCount: RDD[(String, Int)] = orderData.flatMap(datas => {
			val cIds: Array[String] = datas.split(",")(8).split("-")
			cIds.map(id => (id, 1))
		}).reduceByKey(_ + _)
		//orderCount.collect.take(10).foreach(println)

		// TODO 统计品类支付数量：（品类ID，支付数量）
		val payData: RDD[String] = datas.filter(datas => datas.split(",")(10).nonEmpty)
		//payData.collect.take(10).foreach(println)
		val payCount: RDD[(String, Int)] = payData.flatMap(datas => {
			val cIds: Array[String] = datas.split(",")(10).split("-")
			cIds.map(id => (id, 1))
		}).reduceByKey(_ + _)
		//payCount.collect.take(10).foreach(println)

		// TODO 将品类进行排序，取Top10：先看点击，再看下单，最后看支付
		// 将品类点击，下单，支付数量转换结构为（品类ID，（点击数量，下单数量，支付数量））
		// 排序
		val categoryGroup: RDD[(String, (Iterable[Int], Iterable[Int], Iterable[Int]))] = clickCount
		  .cogroup(orderCount, payCount)
		val categoryCount: RDD[(String, (Int, Int, Int))] = categoryGroup.mapValues {
			case (clickItr, orderItr, payItr) => {
				var clickCnt = 0
				val itrClick: Iterator[Int] = clickItr.iterator
				if (itrClick.hasNext) clickCnt = itrClick.next()

				var orderCnt = 0
				val itrOrder: Iterator[Int] = orderItr.iterator
				if (itrOrder.hasNext) orderCnt = itrOrder.next()

				var payCnt = 0
				val itrPay: Iterator[Int] = payItr.iterator
				if (itrPay.hasNext) payCnt = itrPay.next()

				(clickCnt, orderCnt, payCnt)
			}
		}

		val result: Array[(String, (Int, Int, Int))] = categoryCount.sortBy(_._2, false).take(10)
		// TODO 输出结果
		result.foreach(println)

		sc.stop()
		val end: Long = System.currentTimeMillis()

		println("spendTime = " + (end - start))
	}
}
