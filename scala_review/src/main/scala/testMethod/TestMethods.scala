package testMethod

object TestMethods {
	def main(args: Array[String]): Unit = {
		val intList = List(1, 3, 25, 3, 2)
//		println(intList.sortWith(_ < _))
		// until不包含最后一个元素
		for(i <- 0 until intList.size) {
			println(i)
		}
		for(i <- intList.indices) {
			println(i)
		}
		// to包含最后一个元素
		for(i <- 0 to intList.size) {
			println(i)
		}
	}
}
