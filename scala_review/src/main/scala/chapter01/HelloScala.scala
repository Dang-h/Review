package chapter01

import scala.util.control.Breaks._

object HelloScala {
	def main(args: Array[String]): Unit = {
		println("Hello Scala")
		//		println(
		//			"""
		//			  |test
		//			  |test""".stripMargin)

		// f(10)(20)
		//		def f(i: Int): Int => Int = {
		//			def f1(j: Int): Int = {
		//				i * j
		//			}
		//
		//			f1
		//		}
		def f(i: Int)(j: Int): Int = {
			i * j
		}

		// test(10)(20)(_+_)
		def test(i: Int)(j: Int)(f: (Int, Int) => Int): Int = {
			f(i, j)
		}

		val t = () => "test"
		println(t())

		def test2(op: => Unit) = {
			op
		}

		test2(println("test2"))

		breakable {
			for (i <- 1 to 10) {
				if (i % 2 == 0) {
					break
				}
			}
		}

		def testLazy(): String = {
			println("1test.......")
			"tetLazy"
		}

		lazy val name = testLazy()

		println("2--------------")
		println("3--------------")
		println(name)

		implicit def f1(d:Double):Int ={
			d.toInt
		}

		1 to  5
		val num:Int = 2.1

		println(num)


	}

}

