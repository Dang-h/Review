package chapter01

import scala.io.{BufferedSource, Source}

object WordCount {
	def main(args: Array[String]): Unit = {
		val source: BufferedSource = Source.fromFile("input/word.txt")
//		val dataLine: List[String] = source.getLines().toList
//		val words: List[String] = dataLine.flatMap(line => line.split(" "))
//		val wordListMap: Map[String, List[String]] = words.groupBy(word => word)
//		val wordCountMap: Map[String, Int] = wordListMap.map(kv => {
//			val word: String = kv._1
//			val list: List[String] = kv._2
//			(word, list.size)
//		})
//		println(wordCountMap.toList.sortBy(t => t._2)(Ordering.Int.reverse).take(3).mkString(","))
		println(source.getLines().toList.flatten(line => line.split(" ")).groupBy(word => word).map(kv => {
			val word: String = kv._1
			val list: List[String] = kv._2
			(word, list.size)
		}).toList.sortBy(t => t._2)(Ordering.Int.reverse).take(3).mkString(","))
		source.close()
	}
}
