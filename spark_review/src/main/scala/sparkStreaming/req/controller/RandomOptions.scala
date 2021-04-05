package sparkStreaming.req.controller

import sparkStreaming.req.bean.RanOpt

import scala.collection.mutable.ListBuffer
import scala.util.Random

object RandomOptions {
	def apply[T](opts: RanOpt[T]*) = {
		val randomOpts = new RandomOptions[T]()
		for (opt <- opts) {
			randomOpts.totalWeight += opt.weight
			for (i <- 1 to opt.weight) {
				randomOpts.optsBuffer += opt.value
			}
		}
		randomOpts
	}
}

class RandomOptions[T](opts: RanOpt[T]*) {
	var totalWeight = 0
	var optsBuffer = new ListBuffer[T]

	def getRandomOpt: T = {
		val randomNum: Int = new Random().nextInt(totalWeight)
		optsBuffer(randomNum)
	}
}
