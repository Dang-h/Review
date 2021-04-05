package sparkCore.frameWork.service

import org.apache.spark.rdd.RDD
import sparkCore.frameWork.common.TService
import sparkCore.frameWork.dao.WordCountDao

class WordCountService() extends TService {
	private val wordCountDao = new WordCountDao()

	override def dataAnalysis() = {
		val datas: RDD[String] = wordCountDao.readFile("input/word.txt")
		val array: Array[(String, Int)] = datas.flatMap(_.split(" ")).map(word => (word, 1)).reduceByKey(_ + _).collect()
		array
	}
}
