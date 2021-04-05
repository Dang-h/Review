package sparkStreaming.req.controller

import org.apache.spark.streaming.dstream.DStream
import sparkStreaming.req.bean.AdClickData
import sparkStreaming.req.util.JDBCUtil

import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.Date

object AreaCityAdCountHandler {

	private val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
	//实时统计每天各地区各城市各广告的点击总流量，并将其存入 MySQL

	def saveAreaCityAdClickCnt2Mysql(adClickData: DStream[AdClickData]) = {
		// 各大区各城市广告总点击数
		val areaCityAdCnt: DStream[((String, String, String, String), Long)] = adClickData.map(data => {
			val timestamp: Long = data.ts.toLong
			val date: String = sdf.format(new Date(timestamp))
			((date, data.area, data.city, data.ad), 1L)
		}).reduceByKey(_ + _)

		// 单批次统计后导入mysql
		areaCityAdCnt.foreachRDD(rdd => {
			rdd.foreachPartition(itr => {
				val conn: Connection = JDBCUtil.getConnection

				itr.foreach {
					case ((date, area, city, ad), count) => {
						val sql =
							"""
							  |INSERT INTO testcode.area_city_ad_count (dt,area,city,adid,count)
							  |VALUES(?,?,?,?,?)
							  |ON DUPLICATE KEY
							  |UPDATE count=count+?
							  |""".stripMargin
						JDBCUtil.executeUpdate(conn, sql, Array(date, area, city, ad, count, count))
					}
				}
				conn.close()
			})
		})


	}
}
