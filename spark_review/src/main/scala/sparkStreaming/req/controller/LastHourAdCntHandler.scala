package sparkStreaming.req.controller

import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream
import sparkStreaming.req.bean.AdClickData

import java.text.SimpleDateFormat
import java.util.Date

object LastHourAdCntHandler {
	//近一小时广告点击量
	/*
	1）开窗确定时间范围；
	2）在窗口内将数据转换数据结构为((adid,hm),count);
	3）按照广告 id 进行分组处理，组内按照时分排序。
	 */

	private val sdf = new SimpleDateFormat("HH:mm")

	def getAdHourClickCnt(adClickData: DStream[AdClickData]) = {
		// 开窗，时间间隔1小时
		val windowAdClickData: DStream[AdClickData] = adClickData.window(Seconds(10),Seconds(10))

		// 转换数据结构((adid, hour), 1L),并统计((ad, hour), sum),再转换为(ad, (hour, sum))
		val adHourClickCnt: DStream[(String, (String, Long))] = windowAdClickData.map(data => {
			val hour: String = sdf.format(new Date(data.ts.toLong))
			((data.ad, hour), 1L)
		}).reduceByKey(_ + _).map {
			case ((ad, hour), sum) => (ad, (hour, sum))
		}

		// 按照ad分组,并按时间升序排列
		adHourClickCnt.groupByKey().mapValues(itr => itr.toList.sortWith(_._1 < _._1))

	}
}
