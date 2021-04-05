package sparkStreaming.req.bean

/**
 * 城市信息表
 * @param city_id 城市编号
 * @param city_name 城市名称
 * @param area 区域
 */
case class CityInfo(city_id: Long,
					city_name: String,
					area: String)