package sparkStreaming.req.util

import java.io.{FileInputStream, InputStreamReader}
import java.util.Properties
import scala.reflect.io.File

object PropertiesUtil {
	def load(propertiesName: String): Properties = {
		val prop = new Properties()
		val stream = new FileInputStream(propertiesName)

		prop.load(new InputStreamReader(
			stream, "UTF-8"))
		prop
	}
}
