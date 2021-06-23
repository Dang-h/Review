package base

import java.io.{BufferedReader, File, FileNotFoundException, InputStreamReader}

import org.junit.Test

import scala.collection.immutable
import scala.util.control.Breaks.{break, breakable}

class ForTest {
	@Test
	def fileL() = {

		val filesHere: Array[File] = new File("src/main/scala/base").listFiles
		println(filesHere.mkString("Array(", ", ", ")"))

		def fileLines(file: java.io.File): Array[String] = {
			var fileArr: Array[String] = null
			try {
				fileArr = scala.io.Source.fromFile(file).getLines().toArray
			} catch {
				case ex: FileNotFoundException => ex.printStackTrace()
			}
			fileArr
		}

		//		def grep(pattern: String): Unit = {
		//			for (
		//				file <- filesHere
		//				if file.getName.endsWith(".scala");
		//				line <- fileLines(file)
		//				if line.trim.matches(pattern)
		//			) println(s"$file:${line.trim}")
		//		}

		def grep(pattern: String) = {
			for {
				file <- filesHere
				if file.getName.endsWith(".scala")
				line <- fileLines(file)
				trimmed = line.trim // 中途变量绑定
				if trimmed.matches(pattern)
			} println(s"$file:$trimmed")
		}

		grep(".*gcd.*")
	}

	@Test
	def testContinueBreak() = {
		val args: List[String] = List("-continueAndCreak.scala", "testContinueAndBreak.scala")

		def searchFrom(i: Int): Int = {
			if (i >= args.length) -1
			else if (args(i).startsWith("-")) searchFrom(i + 1)
			else if (args(i).endsWith(".scala")) i
			else searchFrom(i + 1)
		}

		println(searchFrom(0))
		println(searchFrom(1))

		val in = List("asd", "", "scas")
		var i = 0
		breakable {
			while (true) {
				println(in(i))
				if (in.contains("")) break
				i += 1
			}
		}
	}

	@Test
	def testMethod() = {
		// 以序列形式返回一行,助手函数
		def makeRowSeq(row: Int) = {
			for (col <- 1 to 10) yield {
				val prod: String = (row * col).toString
				val padding: String = " " * (4 - prod.length)
				padding + prod
			}
		}

		// 以字符形式返回一行，助手函数
		def makeRow(row: Int) = makeRowSeq(row).mkString

		// 以每行占一个文本行的字符串的形式返回表格
		def multiTable() = {
			val tableSeq: immutable.IndexedSeq[String] = for (row <- 1 to 10) yield makeRow(row)

			tableSeq.mkString("\n")
		}

		println(multiTable)
	}

}
