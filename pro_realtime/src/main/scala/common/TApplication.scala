package common

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import utils.EnvUtil

trait TApplication {
	/**
	 *
	 * @param master
	 * @param appName
	 * @param intervalTime
	 * @param op
	 * @param isTest
	 */
	def start(master: String = "local[*]", appName: String = "Application", intervalTime: Int = 5, isTest: Boolean =
	true)(op: => Unit): Unit = {
		val conf: SparkConf = new SparkConf().setMaster(master).setAppName(appName)
		val ssc = new StreamingContext(conf, Seconds(intervalTime))

		EnvUtil.put(ssc)

		try {
			op
		} catch {
			case ex => println(ex.getMessage)
		}

		if (isTest) {
			ssc.start()
			ssc.awaitTermination()
		}
		ssc.start()
		EnvUtil.clear()
	}
}
