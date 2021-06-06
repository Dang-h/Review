package utils

import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object Env {

	def conf(appName: String) = {
		val sparkConf: SparkConf = new SparkConf().setAppName(appName).setMaster("local[*]")
		sparkConf
	}

	def makeSc(appName: String) = {
		val sc = new SparkContext(conf(appName))
		sc
	}

	def makeSS(appName: String) = {
		val sparkSession: SparkSession = SparkSession.builder.config(conf(appName)).getOrCreate()
		sparkSession
	}

	def makeSSC(appName: String, time: Long) = {
		val ssc = new StreamingContext(makeSc(appName), Seconds(time))
		ssc
	}
}
