package base.in

// Navigator类对在base包中的所有类和对象中可见
private[base] class Navigator {
	println("I'm Navigator")
	protected [in] def f1(){println("f1")}

	class InnerClass{
		private[Navigator] val distance = 100
		private[this] var speed = 20
	}
}
