package sparkSQL.req.movieAnaly.application

import org.apache.commons.dbutils.QueryRunner
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import sparkSQL.req.movieAnaly.bean.{Movies, Rating}
import sparkSQL.req.movieAnaly.dao.Top10MovieByRating
import sparkSQL.req.movieAnaly.util.JDBCUtil
import utils.Env.makeSS

object MovieAnalyApp {





	// 需求3：查找被评分次数较多的电影Top10


	val moviesSchema: StructType = StructType(List(
		StructField("movieId", StringType, nullable = false),
		StructField("title", StringType, nullable = false),
		StructField("category", StringType, nullable = false)
	))

	val ratingsSchema = StructType(List(
		StructField("userId", StringType, nullable = false),
		StructField("movieId", StringType, nullable = false),
		StructField("rating", StringType, nullable = false),
		StructField("timestamp", StringType, nullable = false)
	))


	def main(args: Array[String]): Unit = {
		// 实例化SparkSession
		val spark: SparkSession = makeSS("movieAnaly")


		// 获取数据
		val movieDF: DataFrame = spark.read.format("csv").option("header", "true").schema(moviesSchema).load("data/movies.csv")
		val ratingsDF: DataFrame = spark.read.format("csv").option("header", "true").schema(ratingsSchema).load("data/ratings.csv")

		// 需求1：查找电影评分个数超过5000，且平均分较高的Top10电影名称及其对应评分
		val top10Movie = new Top10MovieByRating
		//top10Movie.run(movieDF, ratingsDF, spark)

		// 需求2：查找每个电影类别及其对应的平均评分
		val categoryAndRating = new perCategoryAndAvgRating
			categoryAndRating.run(movieDF, ratingsDF, spark)

		spark.close()
	}

}
