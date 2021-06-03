package sparkSQL.req.movieAnaly.util

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Properties

object PropUtils {
	def load(propertiesName: String) = {
		val prop = new Properties()

		prop.load(new InputStreamReader(Thread.currentThread().getContextClassLoader.getResourceAsStream(propertiesName), StandardCharsets.UTF_8))

		prop
	}

}
