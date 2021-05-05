package esAPITest


import io.searchbox.action.Action
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.{DocumentResult, Get, Index, Search, SearchResult}

import java.util
import scala.collection.JavaConverters.asScalaBufferConverter

object ESUtilTest {

	// 声明Jest客户端工厂
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

	def getClient() = {
		if (factory == null) {
			// 创建Jest客户端工厂对象
			build()
		}

		factory.getObject
	}


	def putIndex1() = {
		val client: JestClient = getClient()

		val source: String =
			"""
			  |{
			  |  "id":200,
			  |  "name":"operation meigong river",
			  |  "doubanScore":8.0,
			  |  "actorList":[
			  |	    {"id":3,"name":"zhang han yu"}
			  |	  ]
			  |}
			  |""".stripMargin

		val index: Index = new Index.Builder(source)
		  .index("test_index_new")
		  .`type`("movie")
		  .id("1")
		  .build()

		client.execute(index)
		client.close()
	}


	def putIndex2() = {
		val client: JestClient = getClient()

		val actList: util.List[util.Map[String, Any]] = new util.ArrayList[util.Map[String, Any]]()

		val actorMap: util.Map[String, Any] = new util.HashMap[String, Any]()
		actorMap.put("id", 301)
		actorMap.put("name", "测试")

		actList.add(actorMap)

		val movie: Movie = Movie(300, "test", 9.0f, actList)

		val index: Index = new Index.Builder(movie)
		  .index("test_index_movie")
		  .`type`("movie")
		  .id("1")
		  .build()

		client.execute(index)
		client.close()
	}


	def queryIndexById() = {
		val client: JestClient = getClient()

		val get: Get = new Get.Builder("test_index_movie", "1").build()
		val result: DocumentResult = client.execute(get)
		println(result.getJsonString)
		client.close()
	}


	def queryByCondition1() = {
		val client: JestClient = getClient()

		val query: String =
			"""
			  |{
			  |  "query": {
			  |    "match_phrase": {
			  |      "actorList.name": "zhang han yu"
			  |    }
			  |  }
			  |}
			  |""".stripMargin

		val search: Search = new Search.Builder(query).addIndex("test_index_new").build()

		val result: SearchResult = client.execute(search)
		val list: util.List[SearchResult#Hit[util.Map[String, Any], Void]] = result.getHits(classOf[util.Map[String, Any]])
		println(result.getJsonString)
		val resList: List[util.Map[String, Any]] = list.asScala.map(_.source).toList
		println(resList.mkString("\n"))
		client.close()
	}


	def main(args: Array[String]): Unit = {
		//		putIndex1()
		//		putIndex2()
//		queryIndexById()
		queryByCondition1()
	}
}

case class Movie(id: Long, name: String,
				 doubanScore: Float,
				 actList: java.util.List[java.util.Map[String, Any]])
