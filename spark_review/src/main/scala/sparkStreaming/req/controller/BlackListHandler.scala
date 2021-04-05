package sparkStreaming.req.controller

import org.apache.spark.streaming.dstream.DStream
import sparkStreaming.req.bean.AdClickData
import sparkStreaming.req.util.JDBCUtil

import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date

object BlackListHandler {

	private val sdf = new SimpleDateFormat("yyyy-MM-dd")

	/**
	 * 将点击数超过阈值的用户加入黑名单
	 * @param filteredAdsClickData 未在黑名单中的用户数据
	 */
	def addBlackList(filteredAdsClickData: DStream[AdClickData]) = {
		// 统计当前批次中每个用户点广告的总数量
		val adClickCnt: DStream[((String, String, String), Long)] = filteredAdsClickData.map(
			data => {
				val date: String = sdf.format(new Date(data.ts.toLong))
				((date, data.user, data.ad), 1L)
			}
		).reduceByKey(_ + _)

		adClickCnt.foreachRDD(rdd => {
			rdd.foreachPartition(itr => {
				val conn: Connection = JDBCUtil.getConnection

				itr.foreach {
					case ((date, user, ad), clickCnt) => {
						val sql1 =
							"""
							  |INSERT INTO testcode.user_ad_count (dt,userid,adid,count)
							  |VALUES (?,?,?,?)
							  |ON DUPLICATE KEY
							  |UPDATE count=count+?
							  |""".stripMargin
						JDBCUtil.executeUpdate(conn, sql1, Array(date, user, ad, clickCnt, clickCnt))

						val sql2 =
							"""
							  |select count
							  |from testcode.user_ad_count
							  |where dt = ? and userid = ? and adid = ?
							  |""".stripMargin
						val adCLickCnt: Long = JDBCUtil.getDataFromMysql(conn, sql2, Array(date, user, ad))

						// 点击量超过阈值30，加入黑名单
						if (adCLickCnt >= 30) {
							val sql3 =
								"""
								  |insert into testcode.black_list (userid) values (?)
								  |on duplicate key
								  |update userid = ?
								  |""".stripMargin
							JDBCUtil.executeUpdate(conn, sql3, Array(user, user))
						}
					}
				}

				conn.close()
			})
		})
	}

	/**
	 * 筛选出没在黑名单中的用户
	 * @param adClickData 点击数据
	 * @return 未被加入黑名单的数据
	 */
	def filterByBlackList(adClickData: DStream[AdClickData])={
		adClickData.transform(rdd =>{
			rdd.filter( data =>{
				val conn: Connection = JDBCUtil.getConnection

				val sql =
					"""
					  |select *
					  |from testcode.black_list
					  |where userid = ?
					  |""".stripMargin
				val flag: Boolean = JDBCUtil.isExist(conn, sql, Array(data.user))

				conn.close()
				!flag
			})
		})
	}
}
