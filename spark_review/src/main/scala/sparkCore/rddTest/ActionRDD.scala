package sparkCore.rddTest

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object ActionRDD {
	def main(args: Array[String]): Unit = {
		val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("ActionRDD")
		val sc = new SparkContext(conf)

		val data: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4))

		// TODO 行动算子：触发作业（Job）执行的方法
		// TODO reduce：聚合rdd内所有数据，先聚合分区内数据，再聚合分区间数据
		val reduceData: Int = data.reduce(_ + _)
		//println(reduceData)

		// TODO collect：将不同分区的数据按分区顺序采集到Driver端内存中，形成Array
		val collectData: Array[Int] = data.collect()
		//println(collectData.mkString("Array(", ", ", ")"))

		// TODO count：统计RDD中的个数
		val countData: Long = data.count()
		//println(countData)

		// TODO first:取数据源中第一个数据
		val firstData: Int = data.first()
		//println(firstData)

		// TODO take:获取N个数据,返回一个Array
		val takeData: Array[Int] = data.take(2)
		//println(takeData)

		// TODO takeOrdered:数据排序后取N个数据;(默认为升序)
		val takeOrderedData: Array[Int] = data.takeOrdered(3)(Ordering.Int.reverse)
		//println(takeOrderedData.mkString(","))

		// TODO aggregate:初始值会参与到分区内和分区间计算
		val data2: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4), 2)
		val aggregateData: Int = data2.aggregate(2)(_ + _, _ + _)
		//println(aggregateData)
	}

}
