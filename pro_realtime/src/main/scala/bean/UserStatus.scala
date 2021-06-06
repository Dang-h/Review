package bean

/**
 *
 * @param userId 用户Id
 * @param ifConsumed 是否消费过；0:首单; 1:非首单
 */
case class UserStatus(
					   userId: String, //用户 id
					   ifConsumed: String //是否消费过 0 首单 1 非首单
					 )
