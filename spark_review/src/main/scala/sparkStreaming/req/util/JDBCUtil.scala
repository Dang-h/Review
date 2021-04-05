package sparkStreaming.req.util

import com.alibaba.druid.pool.DruidDataSourceFactory

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.Properties
import javax.sql.DataSource

object JDBCUtil {
	// 初始化连接池
	var dataSource: DataSource = init()

	def init() = {
		val properties = new Properties()
		val config: Properties = PropertiesUtil.load("conf/SS_Req_config.properties")
		properties.setProperty("driverClassName", "com.mysql.jdbc.Driver")
		properties.setProperty("url", config.getProperty("jdbc.url"))
		properties.setProperty("username", config.getProperty("jdbc.user"))
		properties.setProperty("password", config.getProperty("jdbc.password"))
		properties.setProperty("maxActive", config.getProperty("jdbc.datasource.size"))
		DruidDataSourceFactory.createDataSource(properties)
	}

	// 获取MySQL连接
	def getConnection = {
		dataSource.getConnection()
	}

	// 执行SQL语句，单条数据插入
	def executeUpdate(conn: Connection, sql: String, param: Array[Any]) = {

		var rnt = 0
		var ps: PreparedStatement = null

		try {
			conn.setAutoCommit(false)
			ps = conn.prepareStatement(sql)

			if (param != null && param.length > 0) {
				for (i <- param.indices) {
					ps.setObject(i + 1, param(i))
				}
			}

			rnt = ps.executeUpdate()
			conn.commit()
			ps.close()
		} catch {
			case e: Exception => e.printStackTrace()
		}
		rnt
	}

	// 数据批量插入
	def executeBatchUpdate(connection: Connection, sql: String, paramsList: Iterable[Array[Int]]) = {
		var rnt: Array[Int] = null
		var ps: PreparedStatement = null

		try {
			connection.setAutoCommit(false)
			ps = connection.prepareStatement(sql)

			for (params <- paramsList) {
				if (params != null && params.length > 0) {
					for (i <- params.indices) {
						ps.setObject(i + 1, params(i))
					}
					ps.addBatch()
				}
			}
			rnt = ps.executeBatch()
			connection.close()
		} catch {
			case e: Exception => e.printStackTrace()
		}
		rnt
	}

	// 判断一条数据是否存在
	def isExist(connection: Connection, sql: String, params: Array[Any]) = {

		var flag: Boolean = false
		var ps: PreparedStatement = null
		try {
			ps = connection.prepareStatement(sql)
			for (i <- params.indices) {
				ps.setObject(i + 1, params(i))
			}

			flag = ps.executeQuery().next()
			ps.close()
		} catch {
			case exception: Exception => exception.printStackTrace()
		}
		flag
	}


	// 获取MySQL的一条数据
	def getDataFromMysql(connection: Connection, sql: String, param: Array[Any]) = {

		var result = 0L
		var ps: PreparedStatement = null

		try {
			ps = connection.prepareStatement(sql)
			for (i <- param.indices) {
				ps.setObject(i + 1, param(i))
			}
			val resultSet: ResultSet = ps.executeQuery()
			while (resultSet.next()) {
				result = resultSet.getLong(1)
			}

			resultSet.close()
			ps.close()
		} catch {
			case e: Exception => e.printStackTrace()
		}

		result
	}
}
