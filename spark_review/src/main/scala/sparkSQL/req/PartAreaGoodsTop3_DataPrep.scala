package sparkSQL.req

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object PartAreaGoodsTop3_DataPrep {
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

		// 准备数据
		spark.sql(
			"""
			  |CREATE TABLE `user_visit_action`(
			  |  `date` string,
			  |  `user_id` bigint,
			  |  `session_id` string,
			  |  `page_id` bigint,
			  |  `action_time` string,
			  |  `search_keyword` string,
			  |  `click_category_id` bigint,
			  |  `click_product_id` bigint,
			  |  `order_category_ids` string,
			  |  `order_product_ids` string,
			  |  `pay_category_ids` string,
			  |  `pay_product_ids` string,
			  |  `city_id` bigint)
			  |  comment "用户浏览行为"
			  |row format delimited fields terminated by '\t'
            """.stripMargin)

		spark.sql(
			"""
			  |load data local inpath 'data/user_visit_action.txt' into table sparksql_test.user_visit_action
            """.stripMargin)

		spark.sql(
			"""
			  |CREATE TABLE `product_info`(
			  |  `product_id` bigint,
			  |  `product_name` string,
			  |  `extend_info` string)
			  |  comment "商品信息"
			  |row format delimited fields terminated by '\t'
            """.stripMargin)

		spark.sql(
			"""
			  |load data local inpath 'data/product_info.txt' into table sparksql_test.product_info
            """.stripMargin)

		spark.sql(
			"""
			  |CREATE TABLE `city_info`(
			  |  `city_id` bigint,
			  |  `city_name` string,
			  |  `area` string)
			  |  comment "城市信息"
			  |row format delimited fields terminated by '\t'
            """.stripMargin)

		spark.sql(
			"""
			  |load data local inpath 'data/city_info.txt' into table sparksql_test.city_info
            """.stripMargin)

		spark.sql("""select * from city_info""").show


		spark.close()
	}

}
