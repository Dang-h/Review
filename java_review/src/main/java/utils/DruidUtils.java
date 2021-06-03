package utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author DangHao
 */
public class DruidUtils {

	private static DruidDataSource ds;

	static {

		try {
			Properties prop = PropUtil.load("druid.properties");
			ds = (DruidDataSource) DruidDataSourceFactory.createDataSource(prop);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Connection getConnection() {
		DruidPooledConnection conn = null;
		try {
			conn = ds.getConnection();
			return conn;
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return null;
	}

	public static DruidDataSource getDataSource(){
		return ds;
	}
}
