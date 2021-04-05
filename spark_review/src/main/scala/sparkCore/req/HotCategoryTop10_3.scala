package sparkCore.req

import org.apache.spark.rdd.RDD
import org.apache.spark.util.AccumulatorV2
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

// Top10的热门品类
/*
数据格式：
0用户点击行为日期，1用户ID，2SessionID，3页面ID，4动作时间，5搜索关键词，6品类ID，7商品ID，8一次订单品类ID集合，9一次订单商品集合，10一次支付品类ID集合，11一次支付商品集合，12城市ID
热门商品：点击次数，下单次数，支付次数来量化。次数多则热门
 */
object HotCategoryTop10_3 {
	// 优化reduceByKey:虽然底层存在预聚合和cache的处理但是shuffle依然影响性能
	def main(args: Array[String]): Unit = {
		val start: Long = System.currentTimeMillis()
		val conf: SparkConf = new SparkConf().setAppName("HotCategoryTop10_2").setMaster("local[*]")
		val sc = new SparkContext(conf)

		// TODO 获取原始数据
		val datas: RDD[String] = sc.textFile("data/user_visit_action.csv")

		// 创建累加器对象
		val acc = new HotCategoryAcc
		// 注册累加器
		sc.register(acc, "HotCategoryAcc")

		datas.foreach(
			action => {
				val data: Array[String] = action.split(",")
				if (data(6) != "-1") {
					// 点击场合
					acc.add((data(6), "click"))
				} else if (data(8).nonEmpty) {
					// 下单场合
					data(8).split("-").foreach(id => acc.add(id, "order"))
				} else if (data(10).nonEmpty) {
					// 支付场合
					data(10).split("-").foreach(id => acc.add((id, "pay")))
				}
			}
		)

		val accValue: mutable.Map[String, HotCategory] = acc.value
		val categories: mutable.Iterable[HotCategory] = accValue.map(_._2)
		//val values: Iterable[HotCategory] = accValue.values

		val result: List[HotCategory] = categories.toList.sortWith(
			(left, right) => {
				if (left.clickCnt > right.clickCnt) true
				else if (left.clickCnt == right.clickCnt) {
					if (left.orderCnt > right.orderCnt) true
					else if (left.orderCnt == right.orderCnt)
						left.payCnt > right.payCnt
					else false
				}
				else false
			}
		)
		// TODO 输出结果
		result.take(10).foreach(println)

		sc.stop()
		val end: Long = System.currentTimeMillis()

		println("spendTime = " + (end - start))

	}
}

class HotCategoryAcc extends AccumulatorV2[(String, String), mutable.Map[String, HotCategory]] {

	private val hcMap: mutable.Map[String, HotCategory] = mutable.Map[String, HotCategory]()

	override def isZero: Boolean = hcMap.isEmpty

	override def copy(): AccumulatorV2[(String, String), mutable.Map[String, HotCategory]] = new HotCategoryAcc

	override def reset(): Unit = hcMap.clear()

	override def add(v: (String, String)): Unit = {
		val cId: String = v._1
		val actionType: String = v._2
		val category: HotCategory = hcMap.getOrElse(cId, HotCategory(cId, 0, 0, 0))
		if (actionType == "click") category.clickCnt += 1
		else if (actionType == "order") category.orderCnt += 1
		else if (actionType == "pay") category.payCnt += 1

		hcMap.update(cId, category)
	}

	override def merge(other: AccumulatorV2[(String, String), mutable.Map[String, HotCategory]]): Unit = {
		val map1: mutable.Map[String, HotCategory] = this.hcMap
		val map2: mutable.Map[String, HotCategory] = other.value

		map2.foreach {
			case (cId, hc) => {
				val category: HotCategory = map1.getOrElse(cId, HotCategory(cId, 0, 0, 0))
				category.clickCnt += hc.clickCnt
				category.orderCnt += hc.orderCnt
				category.payCnt += hc.payCnt

				map1.update(cId, category)
			}
		}
	}
	override def value: mutable.Map[String, HotCategory] = hcMap
}

case class HotCategory(cId: String, var clickCnt: Int, var orderCnt: Int, var payCnt: Int)
