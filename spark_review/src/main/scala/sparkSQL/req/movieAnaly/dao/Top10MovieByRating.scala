package sparkSQL.req.movieAnaly.dao

import org.apache.commons.dbutils.QueryRunner
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import sparkSQL.req.movieAnaly.bean.Top10ByRating
import sparkSQL.req.movieAnaly.util.JDBCUtil

class Top10MovieByRating extends Serializable {

	def run(movieDF: DataFrame, ratingsDF: DataFrame, spark: SparkSession): Unit = {
		import spark.implicits._

		// 创建视图
		movieDF.createOrReplaceTempView("movies")
		ratingsDF.createOrReplaceTempView("rating")

		val sql1: String =
			"""|WITH r_cnt AS (SELECT movieId, COUNT(*) rating_cnt, AVG(rating) rating_avg
			   |               FROM rating
			   |               GROUP BY movieId
			   |               HAVING COUNT(*) >= 5000),
			   |     r_cnt_10 AS (SELECT movieId, ROUND(rating_avg, 2) avgRating
			   |                  FROM r_cnt
			   |                  ORDER BY rating_avg DESC
			   |                  LIMIT 10)
			   |SELECT r.movieId, m.title, avgRating
			   |FROM r_cnt_10        r
			   |         JOIN movies m
			   |              ON m.movieId = r.movieId
			   |              ORDER BY avgRating DESC""".stripMargin

		// 执行操作
		val resultDS: Dataset[Top10ByRating] = spark.sql(sql1).as[Top10ByRating]
		resultDS.show(10)


		// 数据导出
		resultDS.rdd.foreachPartition(itr => {
			itr.foreach(insert2Mysql)
		})
	}


	def insert2Mysql(result: Top10ByRating): Unit = {
		lazy val conn: Option[QueryRunner] = JDBCUtil.getQueryRunner()

		conn match {
			case Some(connection) => {
				upsert(result, connection)
			}
			case None => {
				println("MySQL连接失败")
				System.exit(-1)
			}
		}
	}


	def upsert(result: Top10ByRating, connection: QueryRunner): Unit = {
		try {
			val sql: String =
				"""
				  |REPLACE INTO `top10MovieByRating`(
				  |		movieId,
				  |		title,
				  |		avgRating
				  |	)
				  |	VALUES (?,?,?)
				  """.stripMargin

			connection.update(sql, result.movieId, result.title, result.avgRating)
		} catch {
			case e: Exception => {
				e.printStackTrace()
				System.exit(-1)
			}
		}
	}


}
