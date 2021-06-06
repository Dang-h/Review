package bean

/**
 *
 * @param id 订单明细ID
 * @param order_id 订单ID
 * @param sku_id 商品ID
 * @param order_price 订单价格
 * @param sku_num 商品数量
 * @param sku_name 商品名称
 * @param create_time 创建时间
 * @param spu_id 商品款式ID
 * @param tm_id
 * @param category3_id 第三品类ID
 * @param spu_name
 * @param tm_name
 * @param category3_name 第三品类名称
 */
case class OrderDetail(
						id: Long,
						order_id:Long,
						sku_id: Long,
						order_price: Double,
						sku_num:Long,
						sku_name: String,
						create_time: String,

						var spu_id: Long,     //作为维度数据 要关联进来
						var tm_id: Long,
						var category3_id: Long,
						var spu_name: String,
						var tm_name: String,
						var category3_name: String
					  )
