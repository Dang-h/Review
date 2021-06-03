package sparkSQL.req.movieAnaly.util

import java.util.Properties

import com.alibaba.druid.pool.{DruidDataSource, DruidDataSourceFactory}
import org.apache.commons.dbutils.QueryRunner

object JDBCUtil {

	private val prop: Properties = PropUtils.load("druid.properties")

	private val dataSource: DruidDataSource = DruidDataSourceFactory.createDataSource(prop).asInstanceOf[DruidDataSource]

	def getQueryRunner() = {
		try {
			Some(new QueryRunner(dataSource))
		} catch {
			case e: Exception => e.printStackTrace
				None
		}
	}

}
