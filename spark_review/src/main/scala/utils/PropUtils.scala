package utils

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Properties

object PropUtils {
	def load(propName: String) = {
		val prop = new Properties()
		prop.load(new InputStreamReader(Thread.currentThread().getContextClassLoader.getResourceAsStream(propName), StandardCharsets.UTF_8))
		prop
	}
}
