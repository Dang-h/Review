package utils

import com.alibaba.fastjson.JSONObject

import java.sql.{Connection, DriverManager, ResultSet, ResultSetMetaData, Statement}
import scala.collection.mutable.ListBuffer

object PhoenixUtil {
	def main(args: Array[String]): Unit = {
		val list: List[JSONObject] = queryList("select * from test")
		println(list)
	}


	def queryList(sql: String): List[JSONObject] = {
		val resultList: ListBuffer[JSONObject] = new ListBuffer[JSONObject]()
		// 注册驱动
		Class.forName("org.apache.phoenix.jdbc.PhoenixDriver")
		// 建立连接
		val conn: Connection = DriverManager.getConnection("jdbc:phoenix:hadoop100,hadoop101,hadoop102:2181")
		// 创建数据库操作对象
		val stat: Statement = conn.createStatement
		// 执行sql
		val rs: ResultSet = stat.executeQuery(sql)
		val rsM: ResultSetMetaData = rs.getMetaData
//		println(sql)
		// 处理结果集
		while (rs.next) {
			val rowData = new JSONObject();
			for (i <- 1 to rsM.getColumnCount) {
				rowData.put(rsM.getColumnName(i), rs.getObject(i))
			}
			resultList += rowData
		}

		// 释放资源
		stat.close()
		conn.close()
		resultList.toList
	}
}
