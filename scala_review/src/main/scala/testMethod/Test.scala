package testMethod

trait Test {

	def start(name:String)(flag:Boolean=true)(op: =>Unit): Unit ={
		if (flag){
			println("Do it!")
		}
		println(name)
	}
}
