package base

import java.text.SimpleDateFormat
import java.util.Date

import base.in.Navigator
import com.sun.org.apache.bcel.internal.classfile.InnerClass

import scala.annotation.tailrec
import scala.collection.mutable
import scala.io.Source

class LearnBase {

	class Inner {
		// 被private修饰的成员只能在包含了成员定义的类或对象内部可见
		private def f() {
			println("Inner-f()")
		}

		class InnerOnIn {
			f()
		}

	}

	//	(new Inner).f()
}

class Super {
	// 被protected修饰的对象只能被定义其的类及其子类访问
	protected def f() {
		println("Super")
	}
}

class Sub extends Super {
	f()
}

class Other {
	//	(new Super).f()
}


object Test {
	def main(args: Array[String]): Unit = {
		new Sub
		new Navigator

		for (a <- 1 to 3) {
			println(a)
		}
		println("---------------------------")
		for (i <- 1 until 3) {
			println(i)
		}
		println("---------------------------")
		// 两层循环
		for (i <- 1 to 3; r <- 1 to 3) {
			println(i + "-" + r)

		}
		println("---------------------------")
		for (i <- 1 to 3) {
			for (r <- 1 to 3) {
				println(i + "-" + r)
			}
		}
		println("---------------------------")
		val list = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12)
		for (i <- list; if i % 2 == 0; if i > 4) {
			println(i)
		}
		println("---------------------------")
		// yield将for循环的返回值作为变量存储
		val par: List[Int] = for (i <- list; if i % 2 == 0; if i > 4) yield i
		println(par)
		println("---------------------------")
		delayed(time())
		delayed(time("2021-06-19"))
		println("---------------------------")

		val multiplier: Int => Int = (i: Int) => i * 10

		val buf = new StringBuilder
		val buffer = new StringBuffer // 线程安全
		buffer.append('a')
		//		buffer += "a"
		buffer.append("bcdef")

		buf += 'a'
		buf.append("c")
		buf ++= "bcdef"
		println("buf is : " + buf.toString);
		println("---------------------------")
		val strArr = new Array[String](3)
		strArr(0) = "A"
		strArr(1) = "B"
		strArr(2) = "C"
		val strArr2 = Array("0", "1", "2")
		println("---------------------------")
		// 多维数组,3x3的矩阵
		val dimArr: Array[Array[Int]] = Array.ofDim[Int](3, 3)

		for (i <- 0 to 2; j <- 0 to 2) {
			dimArr(i)(j) = j
			println("i=" + i)
			println("j=" + j)
		}
		println("---------------------------")
		for (i <- 0 to 2; j <- 0 to 2) {
			print(dimArr(i)(j) + "\t")
		}

		println("---------------------------")
		for (i <- 0 to 2) {
			for (j <- 0 to 2) {
				print(dimArr(i)(j) + "\t")
			}
			println()
		}

		//		println(new String("\u95c6".getBytes("GBK"), "utf-8"))

		val tabulate_ints: List[Int] = List.tabulate(3)(n => n * n)
		println(tabulate_ints)

		val ints = List(2, 1, 4, 5, 2, 6)
		val l1 = List(1, 2, 3, 4)
		val l2 = List(4, 5, 6, 7)
		println(ints.sorted)
		println(ints.max)

		val listIntersect: List[Int] = l1.intersect(l2)
		val set1: Set[Int] = l1.toSet
		val set2: Set[Int] = l2.toSet
		val set3 = Set(1, 2)

		println(set1 &~ set2)
		println(set2.diff(set1))

		println(diffToSet(set1, set2))
		println(set1 ++ set2)

		println(set1.product)
		println(set3.subsetOf(set1))

		val m1: Map[String, Char] = Map(
			"red" -> 'r',
			"blue" -> 'b',
			"green" -> 'g',
			"yellow" -> 'y'
		)

		val m2: Map[Char, String] = Map(
			('r', "red"),
			('b', "blue"),
			('g', "green"),
			('y', "yellow")
		)

		m1.keys.foreach(i => println("key:" + i + " value:" + m1(i)))

		val a: Option[Int] = Some(5)
		val b = None

		println(a.getOrElse(0))
		println(b.getOrElse(10))

		val mapItr: Iterator[(String, Char)] = m1.toIterator
		while (mapItr.hasNext) {
			println(mapItr.next)
		}
	}

	def diffToSet(s1: Set[Int], s2: Set[Int]) = {
		s1.diff(s2) ++ s2.diff(s1)
	}

	def time() = {
		println("3-获取当前时间,单位纳秒")
		System.nanoTime()
	}

	def time(time: String) = {
		val format = new SimpleDateFormat("yyyy-MM-dd")
		val date: Date = format.parse(time)
		date
	}

	/**
	 *
	 * @param t 传入一个名为t的函数，函数t无入参，返回Long类型
	 * @return
	 */
	def delayed(t: => Long) = {
		println("1-在delayed方法中")
		println("2-参数：" + t)
		t
	}

	def delayed(t: => Date) = {
		println("1-参数" + t)
		t
	}
}

trait Equal {
	def isEqual(x: Any): Boolean
	def isNotEqual(x: Any): Boolean = !isEqual(x)
}

class Point(xc: Int, yc: Int) extends Equal {
	var x: Int = xc
	var y: Int = yc
	override def isEqual(obj: Any): Boolean = {
		obj.isInstanceOf[Point] && obj.asInstanceOf[Point].x == x
	}
}

object TestTrait {
	def main(args: Array[String]): Unit = {
		val p1 = new Point(2, 3)
		val p2 = new Point(3, 4)
		val p3 = new Point(2, 3)
		val s = new Super

		println(p1.isNotEqual(p2))
		println(p1.isEqual(p3))
		println(p1.isEqual(s))
		println(p1.isNotEqual(2))

		println(matchTest(1))
		println(matchTest("two"))
		println(matchTest(2))
		println(matchTest("other"))

		val alice = new Person("Alice", 10)
		val tom: Person = Person("Tom", 12)
		val jerry: Person = Person("Jerry", 12)

		for (person <- List(alice, tom, jerry)) {
			person match {
				case Person("Alice", 10) => println("Hi~ Alice")
				case Person(name, age) => println("Age" + age + "Name" + name)
			}
		}
	}

	def matchTest(x: Any) = {
		x match {
			case 1 => "one"
			case "two" => 2
			case y: Int => "scala.Int"
			case _ => "many"
		}
	}

	case class Person(name: String, age: Int)

}

object FuncTest {
	def main(args: Array[String]): Unit = {

		// 定义一个匿名函数赋给变量f
		val f: String => Unit = (name: String) => println(name)

		// 定义一个函数，以函数作为参数
		def func(fc: String => Unit): Unit = {
			fc("Test Function")
		}

		func((name: String) => {
			println(name)
		})
		func((name: String) => println(name))
		func((name) => println(name))
		func(name => println(name))
		func(println(_))
		func(println)


		def addBy4(): Int => Int = {
			val a = 4

			def addB(b: Int): Int = a + b

			addB
		}

		println(addBy4()(2))


		def addByA1(a: Int): Int => Int = {
			def addByB(b: Int): Int = a + b

			addByB
		}

		def addByA(a: Int): Int => Int = a + _

		val addBy2: Int => Int = addByA(2)
		println(addBy2(10))

		def addByAB(a: Int)(b: Int) = a + b

		println(addByAB(1)(2))

		def fact(n: Int): Int = {
			if (n == 0) return 1
			fact(n - 1) * n
		}

		// TODO 尾递归,压栈覆盖，进一步防止占内存溢出
		def tailFact(n: Int): Int = {
			@tailrec
			def loop(n: Int, currResult: Int): Int = {
				if (n == 0) return currResult
				loop(n - 1, currResult * n)
			}

			loop(n, 1)
		}

		@tailrec
		def myWhile(condition: => Boolean)(op: => Unit): Unit = {
			if (condition) {
				op
				myWhile(condition)(op)
			}
		}

		var n = 10
		myWhile(n >= 1) {
			println(n)
			n = n - 1
		}

		val name = "DangHao"
		println(name.exists(_.isUpper))

		val lines: List[String] = Source.fromFile("data/wordCount.txt").getLines().toList

		def widthOfLength(s: String) = s.length.toString.length

		//		var maxWidth = 0
		//		for (line <- lines) {
		//			maxWidth = maxWidth.max(widthOfLength(line))
		//		}
		val longestLine: String = lines.reduceLeft(
			(a, b) => if (a.length > b.length) a else b
		)

		val maxWidth: Int = widthOfLength(longestLine)
		println(longestLine)
		println(maxWidth)
		for (line <- lines) {
			val numSpace: Int = maxWidth - widthOfLength(line)
			val padding: String = " " * numSpace
			println(padding + line.length + " | " + line)
		}
	}
}

class ChecksumAccumulator {
	private var sum = 0
	def add(b: Byte) = {
		sum += b
	}

	def checksum(): Int = {
		return ~(sum & 0xFF) + 1
	}
}

object ChecksumAccumulator {
	private val cache = mutable.Map.empty[String, Int]

	def calculate(s: String) = {
		if (cache.contains(s))
			cache(s)
		else {
			val acc = new ChecksumAccumulator
			for (c <- s) {
				acc.add(c.toByte)
			}
			val cs = acc.checksum()
			cache += (s -> cs)
			cs
		}
	}
}

object TestMethod {
	def main(args: Array[String]): Unit = {
		val i: Int = ChecksumAccumulator.calculate("Every value is an object.")
		val i2: Int = ChecksumAccumulator.calculate("Every value is an object.")
		println(i)
	}
}