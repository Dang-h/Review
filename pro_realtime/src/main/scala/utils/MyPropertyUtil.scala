package utils

import java.io.{FileInputStream, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.util.Properties

object MyPropertyUtil {

//	def main(args: Array[String]): Unit = {
//		val prop: Properties = load("config.properties")
//		println(prop.getProperty("kafka.broker.list"))
//	}

	/**
	 * 获取配置文件
	 * @param propertiesName 配置文件名
	 * @return
	 */
	def load(propertiesName: String) = {
		val prop = new Properties()

		// 加载指定配置文件
		prop.load(new InputStreamReader(Thread.currentThread().getContextClassLoader.getResourceAsStream
		(propertiesName),StandardCharsets.UTF_8))
		prop
	}

}
