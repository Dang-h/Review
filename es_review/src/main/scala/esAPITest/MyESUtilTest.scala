package esAPITest

import io.searchbox.client.JestClientFactory
import io.searchbox.client.config.HttpClientConfig

object MyESUtilTest {

	private var factory: JestClientFactory = null

	def build() = {
		factory = new JestClientFactory
		factory.setHttpClientConfig(new HttpClientConfig.Builder("http://hadoop100:9200")
		  .multiThreaded(true)
		  .maxTotalConnection(20)
		  .connTimeout(10000)
		  .readTimeout(1000)
		  .build())
	}

	def getClient = {
		if (factory == null) {
			build()
		}

		factory.getObject
	}
}
