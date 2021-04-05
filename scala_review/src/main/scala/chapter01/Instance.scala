package chapter01

object Instance {
	def main(args: Array[String]): Unit = {
		val user = new User11()
		// 多态：一个对象在不同场合下所表示的不同状态
		// 构建对象时，吐过省略类型，无法使用多态
		// 使用多态时类型省略

		// 构建对象一般采用new的方式，调用对象的构造方法
		// java中有默认的无参构造方法
		// Scala中类中声明了和类名一样的方法。这不是构造方法，只是一个普通方法。（名称相同不是构造方法）
		// Scala是函数时编程语言，万物皆函数，类也是一个函数。所有可以在类的后边声明参数列表，这时候就可以将类名当成函数名来调用
		// 括号也就是构造方法
		// 类在构建的时候完成初始化
		val user1 = new User12()
		println(user1)

		// 声明带参的构造方法
		val zhangsan = new User13()

		// 声明多个构造方法，用关键字“this”

	}

	class User11 {

	}

	class User12() {
		// 类体，函数体
		println("类的初始化")
		def this(name: String) {
			this()
		}
	}

	class User13(var name: String, val age: Int) {
		def this() {
			this("test", 10)
		}

		println(name)
	}

	abstract class Test{
		var abstractFiled:String
		def testAbstract():Unit
	}

	class testExtend extends Test{
		override def testAbstract(): Unit = {

		}
		var abstractFiled: String = _
	}

}
