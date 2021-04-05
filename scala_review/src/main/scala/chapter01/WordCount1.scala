package chapter01

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object WordCount1 {
	def main(args: Array[String]): Unit = {
		val dataList = List(
			("Hello Scala", 4), ("Hello Spark", 2)
		)

		//		val stringToInt: Map[String, Int] = dataList
		//		  .map(t => (t._1 + " ") * t._2)
		//		  .flatMap(words => words.split(" "))
		//		  .groupBy(s => s).map(t => (t._1, t._2.size))

		val stringToInt = dataList
		  .map { case (words, count) => (words + " ") * count }
		  .flatMap(words => words.split(" "))
		  .groupBy(s => s).map { case (word, wordList) => (word, wordList.size) }.toList
		  .sortBy(t => t._2)(Ordering.Int.reverse).mkString(",")

		//		println(stringToInt)
		val tuples: List[(List[String], Int)] = dataList.map { case (words, count) =>
			val list: List[String] = words.split(" ").toList
			(list, count)
		}

		val list = tuples.map {
			case (wordList, count) => {
				val buffer = ListBuffer[String]()
				for (elem <- wordList) {
					buffer.append((elem + " ") * count)
				}
				buffer.toList.mkString(",").split(" ").toList.flatMap(data => data.map(_ + ""))
			}
		}
		println(list)
	}

}
