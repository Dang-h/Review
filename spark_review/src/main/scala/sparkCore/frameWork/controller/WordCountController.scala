package sparkCore.frameWork.controller

import sparkCore.frameWork.common.TController
import sparkCore.frameWork.service.WordCountService

class WordCountController() extends TController{

	private val wordCountService = new WordCountService()

	override def dispatch(): Unit = {
		val array: Array[(String, Int)] = wordCountService.dataAnalysis()
		array.foreach(println)
	}
}
