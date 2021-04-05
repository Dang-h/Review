package sparkSQL

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Encoder, Encoders, Row, SparkSession, TypedColumn, functions}
import org.apache.spark.sql.expressions.{Aggregator, MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, LongType, StructField, StructType}
import org.apache.spark.util.AccumulatorV2
import org.apache.spark.SparkConf


object SparkSQL_UDAF {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("sparkSQLUDAF")
		val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
		import spark.implicits._

		// 计算平均年龄
		val data: RDD[(String, Int)] = spark.sparkContext.makeRDD(List(("zhangsan", 20), ("lisi", 30), ("wangw", 40)))
		val mapRDD: RDD[(Int, Int)] = data.map {
			case (name, age) => (age, 1)
		}
		val result: (Int, Int) = mapRDD.reduce {
			(t1, t2) => (t1._1 + t2._1, t1._2 + t2._2)
		}

		//println(result._1 / result._2)

		// TODO 使用累加器
		val acc = new MyAcc()
		spark.sparkContext.register(acc, "myACC")
		data.foreach(data => acc.add(data._2))
		//println(acc.value)

		// TODO 使用弱类型UDAF函数
		// 将RDD转成DataFrame
		val df: DataFrame = data.toDF("username", "age")
		//df.show()
		// 创建视图
		df.createOrReplaceTempView("user")
		// 注册函数
		spark.udf.register("aggAvg", new MyAvgUDAF())
		// 使用
		spark.sql("select aggAvg(age) as avgAge from user") //.show

		// TODO 使用强类型UDAF函数
		// 注册函数
		spark.udf.register("aggAvg", functions.udaf(new MyAvgUDAF_1))
		// 使用
		spark.sql("select aggAvg(age) from user")//.show

		// TODO 使用SQL & DSL(Domain-Specific-Language特定领域语言)解决早期版本无法使用强类型UDAF函数问题
		// 将df转换成ds(df中的cloName要和ds创建的样例类变量名保持一致)
		val ds: Dataset[User] = df.as[User]
		//将UDAF函数转换成查询的列对象
		val udafCol: TypedColumn[User, Long] = new MyAvgUDAF_2().toColumn

		ds.select(udafCol).show()

		spark.close()
	}

	// 自定义累加器
	class MyAcc extends AccumulatorV2[Int, Int] {

		var sum = 0
		var count = 0

		override def isZero: Boolean = sum == 0 && count == 0

		override def copy(): AccumulatorV2[Int, Int] = {
			val acc = new MyAcc
			acc.sum = this.sum
			acc.count = this.count
			acc
		}

		override def reset(): Unit = {
			sum = 0
			count = 0
		}
		override def add(v: Int): Unit = {
			sum += v
			count += 1
		}
		override def merge(other: AccumulatorV2[Int, Int]): Unit = {
			other match {
				case o: MyAcc =>
					sum += o.sum
					count += o.count
				case _ =>
			}
		}
		override def value: Int = sum / count
	}

	// 弱类型UDAF
	class MyAvgUDAF extends UserDefinedAggregateFunction {

		// 输入数据的结构
		override def inputSchema: StructType = {
			StructType(
				Array(
					StructField("age", LongType)
				)
			)
		}

		// 缓冲区数据的结构：Buffer
		override def bufferSchema: StructType = {
			StructType(
				Array(
					StructField("total", LongType),
					StructField("count", LongType)
				)
			)
		}

		// 函数计算结果类型
		override def dataType: DataType = LongType

		// 函数的稳定性
		override def deterministic: Boolean = true

		// 缓冲区初始化
		override def initialize(buffer: MutableAggregationBuffer): Unit = {
			buffer.update(0, 0L)
			buffer.update(1, 0L)
		}

		// 根据输入的值更新缓冲区的数据
		// 输入的age相加，每输入一个age就+1
		override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
			buffer.update(0, buffer.getLong(0) + input.getLong(0))
			buffer.update(1, buffer.getLong(1) + 1)
		}

		// 缓冲区数据合并
		override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
			buffer1.update(0, buffer1.getLong(0) + buffer2.getLong(0))
			buffer1.update(1, buffer1.getLong(1) + buffer2.getLong(1))
		}

		// 计算平均值
		override def evaluate(buffer: Row): Any = buffer.getLong(0) / buffer.getLong(1)
	}

	// 强类型Buffer样例类
	case class Buff(var total: Long, var count: Long)
	// df转ds需要有样例类加持
	case class User(username:String, age:Long)

	// 强类型UDAF
	class MyAvgUDAF_1 extends Aggregator[Long, Buff, Long] {
		// 缓冲区初始化值
		override def zero: Buff = Buff(0L, 0L)

		// 根据输入的值更新缓冲区数据
		override def reduce(b: Buff, in: Long): Buff = {
			b.total = b.total + in
			b.count = b.count + 1
			b
		}

		// 合并缓冲区
		override def merge(b1: Buff, b2: Buff): Buff = {
			b1.total = b1.total + b2.total
			b1.count = b1.count + b2.count
			b1
		}

		// 计算结果
		override def finish(b: Buff): Long = b.total / b.count

		// 缓冲区编码操作
		override def bufferEncoder: Encoder[Buff] = Encoders.product

		// 输出的编码操作
		override def outputEncoder: Encoder[Long] = Encoders.scalaLong
	}

	// 强类型UDAF
	class MyAvgUDAF_2 extends Aggregator[User, Buff, Long] {
		// 缓冲区初始化值
		override def zero: Buff = Buff(0L, 0L)

		// 根据输入的值更新缓冲区数据
		override def reduce(b: Buff, in: User): Buff = {
			b.total = b.total + in.age
			b.count = b.count + 1
			b
		}

		// 合并缓冲区
		override def merge(b1: Buff, b2: Buff): Buff = {
			b1.total = b1.total + b2.total
			b1.count = b1.count + b2.count
			b1
		}

		// 计算结果
		override def finish(b: Buff): Long = b.total / b.count

		// 缓冲区编码操作
		override def bufferEncoder: Encoder[Buff] = Encoders.product

		// 输出的编码操作
		override def outputEncoder: Encoder[Long] = Encoders.scalaLong
	}



}


