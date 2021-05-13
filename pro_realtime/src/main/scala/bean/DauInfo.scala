package bean

/**
 * 日活数据样例类
 *
 * @param mid 设备id
 * @param uid 用户id
 * @param ar  地区
 * @param ch  渠道
 * @param vc  版本
 * @param dt  日期
 * @param hr  小时
 * @param mi  分钟
 * @param ts  时间戳
 */
case class DauInfo(mid: String, //设备id
				   uid: String, //用户id
				   ar: String, //地区
				   ch: String, //渠道
				   vc: String, //版本
				   var dt: String, //日期
				   var hr: String, //小时
				   var mi: String, //分钟
				   ts: Long //时间戳
				  ) {

}
