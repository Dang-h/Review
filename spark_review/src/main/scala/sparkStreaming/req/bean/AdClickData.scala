package sparkStreaming.req.bean

/**
 * 广告点击数据
 * @param ts 时间戳
 * @param area 区域
 * @param city 城市
 * @param user 用户
 * @param ad 点击广告
 */
case class AdClickData(ts:String, area:String, city:String, user:String, ad:String)
