package sparkCore.frameWork.common

import org.apache.spark.{SparkConf, SparkContext}
import sparkCore.frameWork.util.EnvUtil

trait TApplication {
	def start(master: String = "local[*]", appName: String = "Application")(op: => Unit) = {
		val conf: SparkConf = new SparkConf().setMaster(master).setAppName(appName)
		val sc = new SparkContext(conf)
		EnvUtil.put(sc)

		try {
			op
		} catch {
			case ex => println(ex.getMessage)
		}

		sc.stop()
		EnvUtil.clear()
	}
}
