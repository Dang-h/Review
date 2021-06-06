package utils

import java.util.Properties

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

object RedisUtil {

	var jedisPool:JedisPool = null

	def build() = {
		val prop: Properties = PropUtils.load("cnf.properties")
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
		val client: Jedis = getRedisClient
		println(client.ping())
	}

}
