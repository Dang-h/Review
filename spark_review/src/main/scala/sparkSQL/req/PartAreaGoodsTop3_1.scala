package sparkSQL.req

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object PartAreaGoodsTop3_1 {
	//热门商品是从点击量的维度来看的，
	// 计算各个区域前三大热门商品，并备注上每个商品在主要城市中的分布比例，超过两个城市用其他显示
	//地区|商品名称|点击次数|城市备注
	//华北|商品 A|100000|北京 21.2%，天津 13.2%，其他 65.6%
	//华北|商品 P|80200 |北京 63.0%，太原 10%，其他 27.0%
	//华北|商品 M|40000 |北京 63.0%，太原 10%，其他 27.0%
	//东北|商品 J|92000 |大连 28%，辽宁 17.0%，其他 55.0%
	def main(args: Array[String]): Unit = {
		System.setProperty("HADOOP_USER_NAME", "hadoop")

		val sparkConf = new SparkConf().setMaster("local[*]").setAppName("PartAreaGoodsTop3_DataPrep")
		val spark = SparkSession.builder().enableHiveSupport().config(sparkConf).getOrCreate()

		spark.sql("use sparksql_test")

		// 取得地区，商品，点击数 Top3
		spark.sql(
			"""
			 select *
			  |from (select *,
			  |             rank() over (partition by area order by clickCnt desc) as rank
			  |      from (select area,
			  |                   product_name,
			  |                   count(*) as clickCnt
			  |            from (select ci.area,
			  |                         ci.city_name,
			  |                         pi.product_name,
			  |                         uva.click_product_id
			  |                  from user_visit_action uva
			  |                           join city_info ci on uva.city_id = ci.city_id
			  |                           join product_info pi on uva.click_product_id = pi.product_id
			  |                  where click_product_id != -1
			  |                 ) t1
			  |            group by area, product_name) t2
			  |     ) t3
			  |where rank <= 3
            """.stripMargin)

		spark.sql("""select * from city_info""").show


		spark.close()
	}

}
