package sparkSQL.req

import org.apache.spark.SparkConf
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.{Encoder, Encoders, SparkSession, functions}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object PartAreaGoodsTop3_2 {
	//热门商品是从点击量的维度来看的，
	// 计算各个区域前三大热门商品，并备注上每个商品在主要城市中的分布比例，超过两个城市用其他显示
	//地区|商品名称|点击次数|城市备注
	//华北|商品 A|100000|北京 21.2%，天津 13.2%，其他 65.6%
	//华北|商品 P|80200 |北京 63.0%，太原 10%，其他 27.0%
	//华北|商品 M|40000 |北京 63.0%，太原 10%，其他 27.0%
	//东北|商品 J|92000 |大连 28%，辽宁 17.0%，其他 55.0%
	def main(args: Array[String]): Unit = {
		System.setProperty("HADOOP_USER_NAME", "hadoop")

		val sparkConf = new SparkConf().setMaster("local[*]").setAppName("PartAreaGoodsTop3")
		val spark = SparkSession.builder().enableHiveSupport().config(sparkConf).getOrCreate()

		spark.sql("use sparksql_test")

		// 查询基本数据
		spark.sql(
			"""
			  |select ci.area,
			  |       ci.city_name,
			  |       pi.product_name,
			  |       uva.click_product_id
			  |from user_visit_action uva
			  |         join city_info ci on uva.city_id = ci.city_id
			  |         join product_info pi on uva.click_product_id = pi.product_id
			  |where click_product_id != -1
			  |""".stripMargin).createOrReplaceTempView("t1")

		// 根据区域，商品进行数据聚合
		// 使用UDAF函数
		spark.udf.register("cityRemark", functions.udaf(new CityRemarkUDAF()))
		spark.sql(
			"""
			  |  select
			  |     area,
			  |     product_name,
			  |     count(*) as clickCnt,
			  |     cityRemark(city_name) as city_remark
			  |  from t1 group by area, product_name
			  |""".stripMargin).createOrReplaceTempView("t2")

		spark.sql(
			"""
			  | select *,
			  | 	   rank() over(partition by area order by clickCnt desc) as rank
			  |  from t2
			  |""".stripMargin).createOrReplaceTempView("t3")

		spark.sql(
			"""
			  |select area, product_name, clickCnt, city_remark
			  |from t3
			  |where rank <= 3
			  |""".stripMargin).show(false)


		spark.close()
	}

	case class Buff(var total: Long, var cityMap: mutable.Map[String, Long])

	// 自定义城市备注信息=> 北京 21.2%，天津 13.2%，其他 65.6%
	//IN:城市名称, Buff:(总点击数量，Map[(city, cnt), (city, cnt)]), OUT:备注信息
	class CityRemarkUDAF() extends Aggregator[String, Buff, String] {
		override def zero: Buff = {
			Buff(0, mutable.Map[String, Long]())
		}

		// 更新缓冲区数据
		override def reduce(buff: Buff, in_city: String): Buff = {
			buff.total += 1
			val newCnt: Long = buff.cityMap.getOrElse(in_city, 0L) + 1
			// 更新缓冲区
			buff.cityMap.update(in_city, newCnt)
			buff
		}

		// 合并缓冲区数据
		override def merge(b1: Buff, b2: Buff): Buff = {
			b1.total += b2.total

			val map1: mutable.Map[String, Long] = b1.cityMap
			val map2: mutable.Map[String, Long] = b2.cityMap

			// 两个map合并
			map2.foreach {
				case (city, cnt) => {
					val newCnt: Long = map1.getOrElse(city, 0L) + cnt
					map1.update(city, newCnt)
				}
			}

			b1.cityMap = map1
			b1
		}

		// 将统计结果生成字符串信息
		// 北京 21.2%，天津 13.2%，其他 65.6%
		override def finish(buff: Buff): String = {
			val remarkList: ListBuffer[String] = ListBuffer[String]()

			val totalCnt: Long = buff.total
			val cityMap: mutable.Map[String, Long] = buff.cityMap

			// 降序排列
			val cityCntList: List[(String, Long)] = cityMap.toList.sortWith(
				(left, right) => left._2 > right._2
			).take(2)

			val moreCity: Boolean = cityMap.size > 2
			var rateSum = 0d

			cityCntList.foreach {
				case (city, cnt) => {
					val rate = cnt.toDouble * 100 / totalCnt.toDouble
					// 格式化输出比率
					remarkList.append(f"$city%s $rate%.2f%%")
					rateSum += rate
				}
			}

			if (moreCity) {
				remarkList.append(f"其他 ${100 - rateSum}%.2f%%")
			}

			remarkList.mkString(",")
		}
		override def bufferEncoder: Encoder[Buff] = Encoders.product
		override def outputEncoder: Encoder[String] = Encoders.STRING
	}


}
