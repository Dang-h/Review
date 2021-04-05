package sparkStreaming.req.bean

/**
 * 随即操作控制
 * @param value 随即操作
 * @param weight 权重
 * @tparam T
 */
case class RanOpt[T](value:T, weight:Int)
