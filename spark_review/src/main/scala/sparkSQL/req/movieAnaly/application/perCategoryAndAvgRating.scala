package sparkSQL.req.movieAnaly.application

import org.apache.commons.dbutils.QueryRunner
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import sparkSQL.req.movieAnaly.util.JDBCUtil

class perCategoryAndAvgRating extends Serializable {


	def run(movieDF: DataFrame, ratingsDF: DataFrame, spark: SparkSession): Unit = {
		import spark.implicits._

		// 创建视图
		movieDF.createOrReplaceTempView("movies")
		ratingsDF.createOrReplaceTempView("ratings")

		// 执行操作
		val sql: String =
			"""
			  |WITH category_tmp AS (
			  |    SELECT movieId, title, category_name
			  |    FROM movies
			  |             LATERAL VIEW EXPLODE(SPLIT(category, "\\|")) tmp AS category_name
			  |   ),
			  |     rating_tmp AS (SELECT movieId, rating
			  |                    FROM ratings
			  |                    )
			  |SELECT  m.category_name, concat_ws('|',collect_set(r.rating)) rating,round(avg(rating),2) avg_rating
			  |FROM category_tmp        m
			  |         JOIN rating_tmp r
			  |              ON m.movieId = r.movieId
			  |GROUP BY  m.category_name
			  |""".stripMargin

		val sql2 = """
					 |WITH category_tmp AS (
					 |    SELECT movieId, title, category_name
					 |    FROM movies
					 |             LATERAL VIEW EXPLODE(SPLIT(category, "\\|")) tmp AS category_name
					 |),
					 |     rating_tmp AS (SELECT movieId, rating
					 |                    FROM rating
					 |     )
					 |SELECT m.category_name, ROUND(AVG(rating), 2) avg_rating
					 |FROM category_tmp        m
					 |         JOIN rating_tmp r
					 |              ON m.movieId = r.movieId
					 |GROUP BY m.category_name
					 |ORDER BY avg_rating""".stripMargin
		val result = spark.sql(sql).as[Category]
//		val result2: Dataset[CategoryAvgRating] = spark.sql(sql2).as[CategoryAvgRating]
//		result.show(20)

		val sql3 =
			"""
			  |SELECT  category_name
			  |    FROM movies
			  |             LATERAL VIEW EXPLODE(SPLIT(category, "\\|")) tmp AS category_name
			  |             group by category_name
			  |""".stripMargin

		spark.sql(sql3).show(100,false)
		// 导出数据
//		result.rdd.foreachPartition(itr => itr.foreach(insert2Mysql))
	}

	def insert2Mysql(result: Category): Unit = {
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


	def upsert(result: Category, connection: QueryRunner): Unit = {
		try {
			val sql: String =
				"""
				  |REPLACE INTO `category_detail`(
				  |		category,
				  |		`Rating`,
				  |		`avgRating`
				  |	)
				  |	VALUES (?,?,?)
				  """.stripMargin

			connection.update(sql, result.category_name, result.rating,result.avg_rating)
		} catch {
			case e: Exception => {
				e.printStackTrace()
				System.exit(-1)
			}
		}
	}
}
