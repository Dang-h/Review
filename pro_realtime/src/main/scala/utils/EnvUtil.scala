package utils

import org.apache.spark.streaming.StreamingContext

object EnvUtil {
	private val sscLocal = new ThreadLocal[StreamingContext]()

	def put(ssc: StreamingContext) = {
		sscLocal.set(ssc)
	}

	def take(): StreamingContext = {
		sscLocal.get()
	}

	def clear() = {
		sscLocal.remove()
	}
}
