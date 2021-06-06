package utils

import java.util.Properties

import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.{Bulk, BulkResult, Index}

object EsUtils {

	private val prop: Properties = PropUtils.load("cnf.properties")
	val esUrl = prop.getProperty("es.url")

	val jestFactory: JestClientFactory = null

	def build() = {
		val factory = new JestClientFactory
		factory.setHttpClientConfig(new HttpClientConfig.Builder(esUrl)
		  .multiThreaded(true)
		  .maxTotalConnection(20)
		  .connTimeout(10000)
		  .readTimeout(1000)
		  .build()
		)
	}

	def getJestClient() = {
		if (jestFactory == null) {
			build()
		}

		jestFactory.getObject
	}

	/**
	 * 向ES中插入数据
	 * @param info (id, datas)
	 * @param indexName
	 */
	def bulkInsert(info: List[(String, Any)], indexName: String) = {
		if (info != null && info.nonEmpty) {
			val client: JestClient = getJestClient()
			val bulkBuilder = new Bulk.Builder()

			for ((id, source) <- info) {
				val index = new Index.Builder(source)
				  .index(indexName)
				  .id(id)
				  .`type`("_doc")
				  .build()

				bulkBuilder.addAction(index)
			}

			val bulk: Bulk = bulkBuilder.build()
			val result: BulkResult = client.execute(bulk)

			println("Insert into ES: " + result.getItems.size())

			client.close()
		}
	}
}
