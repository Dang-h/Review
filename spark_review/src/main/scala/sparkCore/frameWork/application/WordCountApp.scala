package sparkCore.frameWork.application

import sparkCore.frameWork.common.TApplication
import sparkCore.frameWork.controller.WordCountController

object WordCountApp extends App with TApplication{

	start(appName = "wordCount"){
		val controller = new WordCountController()
		controller.dispatch()
	}
}
