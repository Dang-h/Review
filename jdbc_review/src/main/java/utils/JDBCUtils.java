package utils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/12/14 15:04
 **/
public class JDBCUtils {

	/**
	 * 通用数据库连接方法
	 *
	 * @return
	 */
	public static Connection getConnection() throws Exception {

		// 加载配置文件
		InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream("jdbc.properties");
		Properties properties = new Properties();
		properties.load(resourceAsStream);

		String user = properties.getProperty("user");
		String password = properties.getProperty("password");
		String url = properties.getProperty("url");
		String driverClass = properties.getProperty("driverClass");

		// 加载器驱动
		Class.forName(driverClass);

		// 获取连接
		Connection connection = DriverManager.getConnection(url, user, password);

		return connection;
	}

	/**
	 * 通用资源关闭
	 *
	 * @param connection 数据库连接
	 * @param statement  SQL声明资源
	 */
	public static void closeResource(Connection connection, Statement statement) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}

		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}
	}

	public static void closeResource(Connection connection, Statement statement, ResultSet resultSet) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}

		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
		}
	}

	/**
	 * @param sql
	 * @param args
	 */
	public static void update(String sql, Object... args) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();

			ps = connection.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}

			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResource(connection, ps);
		}
	}

	public static <T> T getInstance(Class<T> clazz, String sql, Object... args) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;

		try {
			// 创建连接
			connection = getConnection();

			// 预编译sql
			ps = connection.prepareStatement(sql);

			// 填充占位符
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}

			// 执行并返回结果集
			resultSet = ps.executeQuery();
			ResultSetMetaData rsmd = resultSet.getMetaData();

			// 获取列数
			int columnCount = rsmd.getColumnCount();

			// 处理结果集
			if (resultSet.next()) {
				T t = clazz.newInstance();
				for (int i = 0; i < columnCount; i++) {
					// 动态获取列值
					Object colValue = resultSet.getObject(i + 1);
					// 动态获取列名
					String colLabel = rsmd.getColumnLabel(i + 1);

					// 通过反射给指定的columnLabel属性赋值为columnValue
					Field field = clazz.getDeclaredField(colLabel);
					field.setAccessible(true);
					field.set(t, colValue);
				}
				return t;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭资源
			closeResource(connection, ps, resultSet);
		}

		return null;
	}

	public static <T> List<T> getInstances(Class<T> clazz, String sql, Object... args) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		ArrayList<T> list = null;
		try {
			// 创建连接
			connection = getConnection();

			// 预编译sql
			ps = connection.prepareStatement(sql);

			// 填充占位符
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}

			// 执行并返回结果集元数据
			resultSet = ps.executeQuery();
			ResultSetMetaData metaData = resultSet.getMetaData();

			// 获取列数
			int columnCount = metaData.getColumnCount();

			// 处理结果集
			list = new ArrayList<>();
			while (resultSet.next()) {
				T t = clazz.newInstance();
				for (int i = 0; i < columnCount; i++) {
					// 获取列值
					Object columnValue = resultSet.getObject(i + 1);
					// 获取列名
					String columnLabel = metaData.getColumnLabel(i + 1);

					// 给列名columnLabel赋值columnValue
					Field field = clazz.getDeclaredField(columnLabel);
					field.setAccessible(true);
					field.set(t, columnValue);

				}
				list.add(t);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭资源
			closeResource(connection, ps, resultSet);
		}

		return null;
	}

}
