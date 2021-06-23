package base

import org.junit.Test

import scala.annotation.tailrec

class Rational(val n: Int, val d: Int) {
	//	println("Created " + n + "/" + d)
	// 前置条件
	require(d != 0)

	// 辅助构造函数
	def this(n: Int) = this(n, 1)

	private val g: Int = gcd(n.abs, d.abs)
	private val num: Int = n / g
	private val deno: Int = d / g

	@tailrec
	private def gcd(a: Int, b: Int): Int = {
		if (b == 0) a else gcd(b, a % b)
	}

	override def toString: String = s"$num/$deno"

	// 1/2 * 3/4 中，1/3为that，(1*4 + 3*2, 2*4) =(10, 8)
	def +(that: Rational): Rational = {
		new Rational(num * that.deno + that.num * deno, deno * that.deno)
	}

	def +(i: Int) = {
		new Rational(num + i * deno, deno)
	}

	def *(that: Rational) = {
		new Rational(num * that.num, deno * that.deno)
	}
	def *(i: Int) = {
		new Rational(i * num, deno)
	}

	def lessThan(that: Rational) = {
		this.num * that.deno < that.num * this.deno
	}

	def max(that: Rational) = {
		if (this.lessThan(that)) that else this
	}
}

class TestRational {
	@Test
	def testAdd() {
		val x = new Rational(1, 2)
		val y = new Rational(2, 3)
		println(x + y * x)
		println(x * y)

		implicit def intToRational(x:Int) = new Rational(x)

		val r = new Rational(2, 3)

		println(2 * r)
		println( r * 2)
	}
}