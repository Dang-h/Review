package sparkStreaming.req.service

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import sparkStreaming.req.bean.AdClickData
import sparkStreaming.req.util.JDBCUtil

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer

object AdBlackList {

	// 实现实时的动态黑名单机制：将每天对某个广告点击超过 100 次的用户拉黑

	// 从kafka读取数据，和MySQL中黑名单数据做校验
	// 通过校验的继续校验当前点击数是否超过阈值,超过阈值的加入黑名单
	// 未超过阈值的更新当天点击量
	def main(args: Array[String]): Unit = {
		val sparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkStreaming")
		val ssc = new StreamingContext(sparkConf, Seconds(3))

		// kafka配置
		val kafkaPara: Map[String, Object] = Map[String, Object](
			ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop100:9092,hadoop100:9092,hadoop100:9092",
			ConsumerConfig.GROUP_ID_CONFIG -> "spark",
			"key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
			"value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer"
		)

		// 使用Driect模式从kafka中取数据
		val kafkaData: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
			ssc,
			LocationStrategies.PreferConsistent,
			ConsumerStrategies.Subscribe[String, String](Set("sparkStreaming"), kafkaPara)
		)

		val adClickData: DStream[AdClickData] = kafkaData.map(
			dataFromKafka => {
				val response: String = dataFromKafka.value()
				val datas: Array[String] = response.split(" ")
				AdClickData(datas(0), datas(1), datas(2), datas(3), datas(4))
			}
		)

		// 对通过黑名单初筛的用户点广告击数进行统计
		// 需要周期行操作数据，使用transform
		val filteredUserData: DStream[((String, String, String), Int)] = adClickData.transform(
			rdd => {
				// 通过JDBC周期性获取数据
				val blackList = ListBuffer[String]()

				val conn: Connection = JDBCUtil.getConnection
				val ps: PreparedStatement = conn.prepareStatement("SELECT userid FROM testcode.black_list")

				val resultSet: ResultSet = ps.executeQuery()
				while (resultSet.next()) {
					blackList.append(resultSet.getString(1))
				}

				resultSet.close()
				ps.close()
				conn.close()

				// 判断点击用户是否在黑名单中
				val filterUser: RDD[AdClickData] = rdd.filter(
					data => !blackList.contains(data.user)
				)

				// 如果用户不在黑名单当中，进行数量计数（每个采集周期）
				filterUser.map(
					data => {
						val sdf = new SimpleDateFormat("yyyy-MM-dd")
						val day: String = sdf.format(new Date(data.ts.toLong))
						val user: String = data.user
						val ad: String = data.ad

						((day, user, ad), 1)
					}
				).reduceByKey(_ + _)
			}
		)

		//
		filteredUserData.foreachRDD(
			rdd => {
				rdd.foreach {
					case ((day, user, ad), clinkCnt) => {
						println(s"${day} ${user} ${ad} ${clinkCnt}")

						// 判断点击数是否>=30，否则加入黑名单
						if (clinkCnt >= 30) {
							val conn1: Connection = JDBCUtil.getConnection
							val ps: PreparedStatement = conn1.prepareStatement(
								"""
								  |insert into testcode.balck_list (userid) values (?)
								  |on duplicate key
								  |update userid = ?
								  |""".stripMargin)

							ps.setString(1, user)
							ps.setString(2, user)

							ps.executeQuery()

							ps.close()
							conn1.close()
						} else {  // 未超过，更新当天广告点击数

							// 获取有点击数据的用户
							val conn2: Connection = JDBCUtil.getConnection
							val ps1: PreparedStatement = conn2.prepareStatement(
								"""
								  |select *
								  |from testcode.user_ad_count
								  |where dt = ? and userid = ? and adid = ?
								  |""".stripMargin)

							ps1.setString(1, day)
							ps1.setString(2, user)
							ps1.setString(3, ad)

							val resultSet1: ResultSet = ps1.executeQuery()
							// 判断该用户是否已经有数据存在，如果存在数据，更新
							if (resultSet1.next()) {
								val ps2 = conn2.prepareStatement(
									"""
									  | update testcode.user_ad_count
									  | set count = count + ?
									  | where dt = ? and userid = ? and adid = ?
                                    """.stripMargin)
								ps2.setInt(1, clinkCnt)
								ps2.setString(2, day)
								ps2.setString(3, user)
								ps2.setString(4, ad)

								ps2.executeUpdate()
								ps2.close()

								// 判断更新后点击数是否超过阈值，超过，加入黑名单
								val ps3 = conn2.prepareStatement(
									"""
									  |select
									  |    *
									  |from user_ad_count
									  |where dt = ? and userid = ? and adid = ? and count >= 30
                                    """.stripMargin)
								ps3.setString(1, day)
								ps3.setString(2, user)
								ps3.setString(3, ad)

								val resultSet3 = ps3.executeQuery()
								if (resultSet3.next()) {
									val ps3 = conn2.prepareStatement(
										"""
										  |insert into black_list (userid) values (?)
										  |on DUPLICATE KEY
										  |UPDATE userid = ?
                                        """.stripMargin)
									ps3.setString(1, user)
									ps3.setString(2, user)

									ps3.executeUpdate()
									ps3.close()
								}

								ps2.close()
								resultSet3.close()
							} else { // 如果用户数据不存在，新增

								val ps4 = conn2.prepareStatement(
									"""
									  | insert into user_ad_count ( dt, userid, adid, count ) values ( ?, ?, ?, ? )
                                    """.stripMargin)

								ps4.setString(1, day)
								ps4.setString(2, user)
								ps4.setString(3, ad)
								ps4.setInt(4, clinkCnt)

								ps4.executeUpdate()
								ps4.close()
							}

							resultSet1.close()
							conn2.close()
						}
					}
				}
			}
		)

		ssc.start()
		ssc.awaitTermination()
	}

}
