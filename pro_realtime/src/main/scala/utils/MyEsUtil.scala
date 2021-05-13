package utils

import io.searchbox.client.JestClientFactory
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.{Bulk, BulkResult, Index}

import java.util.Properties

object MyEsUtil {

	private val prop: Properties = MyPropertyUtil.load("config.properties")
	private val esUrl: String = prop.getProperty("es.url")

	var jestFactory: JestClientFactory = null

	/**
	 * 获取Jest客户端
	 *
	 * @return
	 */
	def getJsetClient() = {
		if (jestFactory == null) {
			// 创建Jest工厂类方法
			build()
		}
		jestFactory.getObject
	}

	/**
	 * 构建Jest客户端
	 */
	def build() = {
		jestFactory = new JestClientFactory
		jestFactory.setHttpClientConfig(new HttpClientConfig.Builder(esUrl)
		  .multiThreaded(true)
		  .maxTotalConnection(20)
		  .connTimeout(10000)
		  .readTimeout(1000).build())
	}

	/**
	 * 向ES中批量插入数据
	 *
	 * @param infoList  需插入的数据
	 * @param indexName index名称
	 */
	def bulkInsert(infoList: List[(String, Any)], indexName: String): Unit = {
		if (infoList != null && infoList.nonEmpty) {
			val jestClient = getJsetClient()
			val bulkBuilder = new Bulk.Builder()

			// 对批量操作的数据进行遍历
			for ((id, source) <- infoList) {
				val index: Index = new Index.Builder(source)
				  .index(indexName)
				  .id(id)
				  .`type`("_doc")
				  .build()

				bulkBuilder.addAction(index)
			}

			val bulk: Bulk = bulkBuilder.build()
			val bulkResult: BulkResult = jestClient.execute(bulk)

			println("向ES中插入" + bulkResult.getItems.size() + "条数据")
//			println(bulkResult.getJsonString)

			jestClient.close()
		}
	}

}
