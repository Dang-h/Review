package sparkCore.rddTest


import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}

import java.text.SimpleDateFormat
import java.util.Date


object TransformRDD {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("TransformRDDTest")
		val sc = new SparkContext(conf)

		// TODO map:将处理的数据逐条映射，转换数据的格式
		// 从服务器日志数据 apache.log 中获取用户请求 URL 资源路径
		val apacheLog: RDD[String] = sc.textFile("data/apache.log")
		val pathOfURL: RDD[String] = apacheLog.map(line => {
			val datas: Array[String] = line.split(" ")
			datas(6)
		})
		//		 pathOfURL.collect().take(10).foreach(println)
		apacheLog

		// TODO mapPartitions:以分区为单位处理数据,处理数据时会长时间占用内存，内存太小数据量太大容易溢出
		// 获取每个数据分区的最大值
		val valueList: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4), 2)
		val mapP: RDD[Int] = valueList.mapPartitions((itr: Iterator[Int]) => List(itr.max).iterator)
		// mapP.collect().foreach(println)

		def doubleMap(a: Int) = (a, a * 2)

		// TODO mapPartitionsWithIndex:在以分区为单位处理数据时可获取当前分区序号
		// 获取第二个数据分区的数据
		val value: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4))
		val mpwiRDD: RDD[Int] = value.mapPartitionsWithIndex(
			(index, iter: Iterator[Int]) => {
				println(index + "---" + iter.toList)
				if (index == 1) {
					println("index == 1")
					iter
				} else {
					println("else")
					Nil.iterator
				}
			}
		)
		// mpwiRDD.collect().foreach(println)

		val value1: RDD[(Int, Int)] = value.mapPartitionsWithIndex(
			(index, dataIter) => {
				dataIter.map(num => (index, num))
			}
		)
		//value1.collect().foreach(println)

		// TODO flatMap:将整体的数据拆开
		// 将 List(List(1,2),3,List(4,5))进行扁平化操作
		val value2: RDD[Any] = sc.makeRDD(List(List(1, 2), 3, List(4, 5)))
		val value3: RDD[Any] = value2.flatMap {
			case list: List[Int] => list
			case num => List(num)
		}
		//value3.collect().foreach(println)

		// TODO glom:将同一分区的数据直接转换成相同类型的内存数组
		val value4: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4), 2)
		val glomRDD: RDD[Array[Int]] = value4.glom()
		//glomRDD.collect().foreach((data: Array[Int]) => println(data.mkString(",")))

		// 取出每个分区的最大值
		val value5: RDD[Int] = glomRDD.map(array => array.max)
		//println(value5.collect().mkString("Array(", ", ", ")"))

		// TODO groupBy:根据指定的规则进行分组，分区默认不变，数据会被打乱重组，存在shuffle操作，极限情况下可能所有数据都分到同一分区，最终导致数据倾斜
		// 从服务器日志数据 apache.log 中获取每个时间段访问量
		val groupForHour: RDD[(String, Int)] = apacheLog.map(
			line => {
				val time: String = line.split(" ")(3)
				val sdf = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss")
				val date: Date = sdf.parse(time)
				val sdfHour = new SimpleDateFormat("HH")
				val hour: String = sdfHour.format(date)
				(hour, 1)
			}
		).groupBy(_._1).map {
			case (hour, iter) => (hour, iter.size)
		}
		//groupForHour.collect.foreach(println)
		//将 List("Hello", "hive", "hbase", "Hadoop")根据单词首写字母进行分组。
		val listRdd: RDD[String] = sc.makeRDD(List("Hello", "hive", "hbase", "Hadoop"))
		val group4H: RDD[String] = listRdd.groupBy(word => word.startsWith("H")).map { case (bool, iter) => iter.mkString(",") }
		//group4H.collect.foreach(println)
		val group4First: RDD[(Char, String)] = listRdd.groupBy(_.charAt(0)).map { case (c, iter) => (c, iter.mkString(",")) }
		//group4First.collect.foreach(println)

		// TODO groupBy的join中的优化使用
		val rdd111 = sc.parallelize(Array((1, 1), (1, 2), (2, 1), (3, 1), (1, 2), (2, 1), (3, 1))).partitionBy(new
			HashPartitioner(2)).persist()
		val rdd211 = sc.parallelize(Array((1, 'x'), (2, 'y'), (2, 'z'), (4, 'w'), (2, 'y'), (2, 'z'), (4, 'w')))

		val rdd111_1 = sc.parallelize(Array((1, 1), (1, 2), (2, 1), (3, 1), (1, 2), (2, 1), (3, 1)))
		val rdd211_1 = sc.parallelize(Array((1, 'x'), (2, 'y'), (2, 'z'), (4, 'w'), (2, 'y'), (2, 'z'), (4, 'w')))

		/** 有优化效果 ,rdd1 不再需要 shuffle */
		rdd111.join(rdd211).collect()
		rdd111_1.join(rdd211_1).collect()

		/** 有优化效果 ,rdd1 不再需要 shuffle */
		rdd111.join(rdd211, new HashPartitioner(2))

		/** 无优化效果 ,rdd1 需要再次 shuffle */
		rdd111.join(rdd211, new HashPartitioner(3))

		// TODO filter：根据指定规则进行过滤，符合规则保留，分区不变，但分区内数据不均匀，易发生数据倾斜
		// 从服务器日志数据 apache.log 中获取 2015 年 5 月 17 日的请求路径
		val filterByTime: RDD[String] = apacheLog.filter(
			line => line.split(" ")(3).startsWith("17/05/2015")
		)
		//filterByTime.collect.foreach(println)

		// TODO sample:根据指定规则从数据集中抽取数据
		// 三个参数：①：抽取是否放回，true（放回）false（不放回）
		// 		   ②：抽取不放回场合：数据源中每条数据被抽取的概率；抽取放回场合：数据源中每条数据被抽取的可能次数
		//		   ③：抽取数据时随机算法的种子：不传递则使用当前系统时间
		val numRdd: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
		//println(numRdd.sample(false, 0.5).collect.mkString(","))
		//println(numRdd.sample(true, 0.5).collect.mkString(","))

		// TODO distinct：去重
		val replicationNum: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4, 1, 2, 3, 4))
		//replicationNum.distinct().collect.foreach(print)
		// 不使用distinct如何去重
		//replicationNum.map(x => (x, 1)).reduceByKey((k,v) =>k).map(_._1).collect.foreach(println)

		// TODO coalesce：缩减分区，用于大数据集过滤后缩减分区以提高小数据集的执行效率，第二参数为true将开启shuffle，可将数据均衡
		replicationNum.coalesce(2)
		// TODO repartition：扩大分区数；底层调用的时coalesce，shuffle参数为true。扩大分区提高计算能力
		replicationNum.repartition(6)

		// TODO sortBy：按照函数 f 的结果进行排序，默认为升序；排序后产生新的RDD，分区数与原RDD一致。存在shuffle过程
		val value6: RDD[Int] = sc.makeRDD(List(3, 2, 12, 23, 124, 2, 1, 5), 4)
		//value6.sortBy(num => num).collect.foreach(print)
		//value6.sortBy(num => num,ascending = false,numPartitions = 2).saveAsTextFile("output")
		//value6.sortBy(num => num, ascending = false).saveAsTextFile("output")

		// TODO zip：拉链，将两个RDD中的元素以键值对的形式合并
		val list1: RDD[Int] = sc.makeRDD(List(1, 2, 3))
		val list2: RDD[Int] = sc.makeRDD(List(1, 2, 3))
		println(list1.zip(list2).collect.mkString("-"))
		// TODO intersection：交集
		// TODO union：并集
		// TODO subtract：差集

		// TODO partitionByKey:将数据按指定的Partitioner进行分区；默认的分区器为HashPartitioner

		// TODO reduceByKey：对相同Key数据的Value聚合；在shuffle前对分区内相同Key的数据进行预聚合，减少落盘的数据量；功能上包含分组和聚合;相同key的第一个数据不参与计算

		// TODO groupByKey：将数据源根据Key对Value进行分组；不存在预聚合，功能只有分组

		// TODO aggregateByKey:将数据根据不同规则对相同Key的Value进行分区内和分区间计算，会生成新的RDD，结果类型和初始值相同；初始值和第一个key的value进行分区内数据操作
		// 有两个参数列表：①表示初始值②传递两个参数1：分区内计算规则2：分区间计算规则
		// 取出每个分区内相同 key 的最大值然后分区间相加
		val rdd = sc.makeRDD(List(
			("a", 1), ("a", 2), ("b", 3),
			("b", 4), ("b", 5), ("a", 6)
		), 2)
		//val rdd: RDD[(String, Int)] = sc.makeRDD(List(("a", 1), ("a", 2), ("a", 3), ("a", 4)), 2)
		val aggreRdd = rdd.aggregateByKey(0)(
			(x, y) => {
				println("①: " + x, y)
				math.max(x, y)
			}, //分区内计算规则，分区内取出value的最大值
			(x, y) => {
				println("②: " + x, y)
				x + y
			} // 分区间计算规则，分区间对每个分区取出的最大值相加
		)
		aggreRdd.collect.foreach(println)
		//获取相同key的数据的平均值 => (a, 3),(b, 4)
		val aggreRDD = rdd.aggregateByKey((0, 0))(
			(t, v) => {
				println("①: " + t, v)
				(t._1 + v, t._2 + 1)
			},
			(t1, t2) => {
				println("②: " + t1, t2)
				(t1._1 + t2._1, t1._2 + t2._2)
			}
		).mapValues {
			case (num, count) => {
				println("③: " + num, count)
				num / count
			}
		}
		//aggreRDD.collect.foreach(println)

		// TODO aggregateByKey代替groupByKey
		val rddList = sc.makeRDD(List(("a", 88), ("b", 95), ("a", 91), ("b", 93), ("a", 95), ("b", 98)))

		//		rddList.groupByKey().collect()
		rddList.sample(false, 0.1).countByKey().foreach(println)

		rddList.aggregateByKey(List[Int]())((i: List[Int], j: Int) => j :: i,
			(resultList: List[Int], iList: List[Int]) => iList ::: resultList) //.collect()


		// TODO foldByKey:分区内和分区间计算规则相同；初始值和第一个key的value进行分区内的数据操作
		val foldByKeyRdd: RDD[(String, Int)] = rdd.foldByKey(0)(_ + _)
		//foldByKeyRdd.collect.foreach(println)

		// TODO combineByKey：将数据根据不同规则进行分区内和分区间计算，允许返回值类型和初始值类型不同；相同key的第一条数据进行函数处理转换结构
		// 三个参数：①将相同key的第一个参数进行结构转换②分区内计算规则③分区间计算规则
		// 将数据 List(("a", 88), ("b", 95), ("a", 91), ("b", 93), ("a", 95), ("b", 98))求每个 key 的平均值
		val combineByKeyRdd = rdd.combineByKey(
			v => {
				println("①: " + v, 1)
				(v, 1)
			},
			(t: (Int, Int), v) => {
				println("②: " + t, v)
				(t._1 + v, t._2 + 1)
			},
			(t1: (Int, Int), t2: (Int, Int)) => {
				println("③: " + t1, t2)
				(t1._1 + t2._1, t1._2 + t2._2)
			}
		)
		//combineByKeyRdd.collect.foreach(println)

		// wordCount
		//rdd.reduceByKey(_ + _).collect.foreach(println)
		//rdd.aggregateByKey(0)(_+_, _ + _).collect.foreach(println)
		//rdd.foldByKey(0)(_ + _).collect.foreach(println)
		rdd.combineByKey(
			v => v,
			(x: Int, y) => {
				println(x, y)
				x + y
			},
			(x: Int, y: Int) => {
				println(x, y)
				x + y
			}) //.collect.foreach(println)

		// TODO join：将两个不同的数据源相同的key的value连接在一起组成元组，如果相同key很多会出现笛卡尔积

		// TODO leftOuterJoin：类似HQL里的左连接

		// TODO cogroup：connect + group
		//		val rdd1 = sc.makeRDD(List(("a", 1), ("b", 2), ("c", 3)))
		//		val rdd2 = sc.makeRDD(List(("c", 1), ("b", 2), ("c", 3)))
		val rdd1 = sc.parallelize(Array((1, 1), (1, 2), (2, 1), (3, 1), (1, 2), (2, 1), (3, 1)))
		val rdd2 = sc.parallelize(Array((1, 'x'), (2, 'y'), (3, 'z'), (4, 'w')))
		val value7: RDD[(Int, (Int, Char))] = rdd1.join(rdd2)

		val rdd2Broadcast = sc.broadcast(rdd2.collectAsMap()).value
		val rdd1Map: RDD[(Int, (Int, Int))] = rdd1.map(data => (data._1, data))

		rdd1Map.mapPartitions(partitions => {
			for ((k, v) <- partitions if rdd2Broadcast.contains(k))
				yield (k, (v._2, rdd2Broadcast.getOrElse(k, "")))
		}) //.collect().foreach(println)


		rdd1.cogroup(rdd2).map(iter => (iter._1, (iter._2._1.sum + iter._2._2.sum))).collect.foreach(println)

		// TODO 使用Broadcast变量与map类算子实现join操作，进而完全规避掉shuffle类的操作
		val smallRDD = sc.parallelize(Array(
			("1", "zhangsan"),
			("2", "lisi"),
			("3", "wangwu")
		)).collectAsMap()

		val smallBroadCast = sc.broadcast(smallRDD)

		val bigRDD = sc.parallelize(Array(
			("1", "school1", "male"),
			("2", "school2", "female"),
			("3", "school3", "male"),
			("4", "school4", "male"),
			("5", "school5", "female")
		)).map(x => (x._1, x))

		val broadCastValue: collection.Map[String, String] = smallBroadCast.value

		bigRDD.mapPartitions(partitions => {

			for ((key, value) <- partitions
				 if (broadCastValue.contains(key)))
				yield (key, broadCastValue.getOrElse(key, ""), value._2, value._3)

		}) //.collect().foreach(println)


		sc.stop()
	}
}
