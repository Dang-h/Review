package chapter01

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object TestCollection {
	def main(args: Array[String]): Unit = {
		// 如何构建数组,可指定类型，在[]中
		val array = new Array[String](3)
		val ints1: Array[Int] = Array(1, 2, 3)
		val ints2 = new ArrayBuffer[Int]()
		val ints3: ArrayBuffer[Int] = ArrayBuffer(1, 2, 3, 4)
		val ints = new Array[Int](3)
		// 如何操作数组
		array(0) = "test"
		array(1) = "test"
		// 更新数组数据
		//		array.update(0, "testUpdate") == array(0)= "testUpdate"
		val array1: Array[Array[Int]] = Array.ofDim[Int](2, 2)
		//		array1.foreach((arr: Array[Int]) => arr.foreach(print))

		val stringToInt: mutable.Map[String, Int] = mutable.Map("1" -> 1, "2" -> 2, "3" -> 3)

		//		println(stringToInt.toArray.mkString(", "))

		val list = List(List(1, 2, 3), List(3, 4, 5))
		val ints4 = List(1, 2, 3)
		val ints5: List[Int] = ints4.map((_: Int) * 2)

		//		println(list.flatMap(datas => datas.map(_ * 2)))

		//		println(list.flatten)
		val strings = List("Hadoop", "Hive", "Spark", "Scala")
		val booleanToStrings: Map[Boolean, List[String]] = strings.groupBy(s => s.startsWith("H"))
		val stringToStrings: Map[String, List[String]] = strings.groupBy(s => s.substring(0, 1))

		val strings1: List[String] = strings.filter(s => s.startsWith("H"))
		val ints6: List[Int] = list.flatMap(list => list)
		val ints7: List[Int] = list.flatMap(list => list.filter(num => num % 2 == 0))
		//		println(ints7)
		// println(ints4.zipWithIndex)
		 println(ints4.zip(ints1))
		val ints8 = List(1, 2, 3, 4, 5, 6, 7, 8)
		val iterator: Iterator[List[Int]] = ints8.sliding(3, 4)

		// iterator.foreach(println)
		def test(i: Int, j: Int): Int = {
			i + j
		}

		val int: Int = ints8.foldLeft("")(_ + _).toInt
		ints8.reduce(test)
		ints8.reduce((i: Int, j: Int) => {
			i + j
		})
		ints8.reduce((i: Int, j: Int) => i + j)
		ints8.reduce((i, j) => i + j)
		ints8.reduce(_ + _)

		val stringToInt1 = Map(
			("a", 1), ("b", 2), ("c", 3)
		)

		val stringToInt2: Map[String, Int] = stringToInt1.map {
			case (word, count) => (word, count * 2)
		}

		val User(name, age) = User("zhangsan", 10)
	}

	case class User(name: String, age: Int)

}
