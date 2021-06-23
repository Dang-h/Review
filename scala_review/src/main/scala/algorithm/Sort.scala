package algorithm


object Sort {
	def main(args: Array[String]): Unit = {
		val ints = List(2, 1, 4, 5, 2, 6)
		println(quickSort(ints))
	}

	def quickSort(list: List[Int]): List[Int] = {
		list match {
			// 空List
			case Nil => Nil
			case List() => List()
			// x :: list ,将x加到list的头部
			case head :: tail => {
				// list1.partition(判断条件)，将list1根据判断条件分成两个可遍历集合
				val (left, right): (List[Int], List[Int]) = tail.partition(_ < head)
				println("left-" + left)
				println("right-" + right)
				// list ::: listB:连接listA和listB
				quickSort(left) ::: head :: quickSort(right)
			}
		}
	}

}
