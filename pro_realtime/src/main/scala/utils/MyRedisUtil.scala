package utils

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

import java.util.Properties

object MyRedisUtil {

	var jedisPool: JedisPool = null

	/**
	 * 创建JedisPool连接池对象
	 */
	def build() = {
		val prop: Properties = MyPropertyUtil.load("config.properties")
		val host: String = prop.getProperty("redis.host")
		val port: String = prop.getProperty("redis.port")

		val redisConf = new JedisPoolConfig

		redisConf.setMaxTotal(100) // 最大连接数
		redisConf.setMaxIdle(20) // 最大空闲
		redisConf.setMinIdle(20) // 最小空闲
		redisConf.setBlockWhenExhausted(true) // 忙碌时是否等待
		redisConf.setMaxWaitMillis(5000) // 忙碌时等待时长，毫秒
		redisConf.setTestOnBorrow(true) // 每次获得连接进行测试

		jedisPool = new JedisPool(redisConf, host, port.toInt)
	}


	def getRedisClient() = {
		if (jedisPool == null) {
			build()
		}
		jedisPool.getResource
	}

	def main(args: Array[String]): Unit = {
		val jedis = getRedisClient()
		println(jedis.ping())

//		val jedis = new Jedis("192.168.1.100", 6379)
//		println(jedis.ping())
	}

}
