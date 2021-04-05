package sparkCore.req

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object PageFlow {
	def main(args: Array[String]): Unit = {
		//页面跳转率
		val conf: SparkConf = new SparkConf().setAppName("PageFlow").setMaster("local[*]")
		val sc = new SparkContext(conf)

		val datas: RDD[String] = sc.textFile("data/user_visit_action.csv")

		//关联样例类
		val actionDataRDD: RDD[UserVisitAction] = datas.map(
			action => {
				val datas: Array[String] = action.split(",")
				UserVisitAction(
					datas(0),
					datas(1).toLong,
					datas(2),
					datas(3).toLong,
					datas(4),
					datas(5),
					datas(6).toLong,
					datas(7).toLong,
					datas(8),
					datas(9),
					datas(10),
					datas(11),
					datas(12).toLong
				)
			}
		)

		actionDataRDD.cache()

		// 对指定页面连续跳转进行统计
		val ids: List[Long] = List[Long](1, 2, 3, 4, 5, 6, 7)
		val okFlowIds: List[(Long, Long)] = ids.zip(ids.tail)
		//println(flowIds)

		// 分母
		val pageIdToCountMap: Map[Long, Long] = actionDataRDD.filter(
			action => ids.init.contains(action.page_id)
		).map(action => (action.page_id, 1L)).reduceByKey(_ + _).collect().toMap

		// 分子
		// 根据session进行分组
		val sessionGroup: RDD[(String, Iterable[UserVisitAction])] = actionDataRDD.groupBy(_.session_id)
		val mapValRDD: RDD[(String, List[((Long, Long), Int)])] = sessionGroup.mapValues(
			itr => {
				// 根据action_time升序排列数据
				val sortList: List[UserVisitAction] = itr.toList.sortBy(_.action_time)
				// 取出访问的页面并组合
				val flowIds: List[Long] = sortList.map(_.page_id)
				//println("flowIds= "+flowIds)
				val pageFlowIds: List[(Long, Long)] = flowIds.zip(flowIds.tail)
				//println("pageFlowIds= " + pageFlowIds)
				pageFlowIds.map(t => (t, 1))
				// 过滤与指定页面不同的页面,并转换结构
				//pageFlowIds.filter(t => okFlowIds.contains(t)).map(t => (t, 1))
			}
		)
		val flatMapRDD: RDD[((Long, Long), Int)] = mapValRDD.map(_._2).flatMap(list => list)

		val dataRdd: RDD[((Long, Long), Int)] = flatMapRDD.reduceByKey(_ + _)

		// 计算转换率
		dataRdd.foreach{
			case ((pageId1, pageId2), sum) =>{
				val l: Long = pageIdToCountMap.getOrElse(pageId1, 0L)

				println(s"page${pageId1} to ${pageId2} :" + (sum.toDouble / l))
			}
		}
		sc.stop()
	}


	case class UserVisitAction(
								date: String, //用户点击行为的日期
								user_id: Long, //用户的ID
								session_id: String, //Session的ID
								page_id: Long, //某个页面的ID
								action_time: String, //动作的时间点
								search_keyword: String, //用户搜索的关键词
								click_category_id: Long, //某一个商品品类的ID
								click_product_id: Long, //某一个商品的ID
								order_category_ids: String, //一次订单中所有品类的ID集合
								order_product_ids: String, //一次订单中所有商品的ID集合
								pay_category_ids: String, //一次支付中所有品类的ID集合
								pay_product_ids: String, //一次支付中所有商品的ID集合
								city_id: Long //城市 id
							  )

}
