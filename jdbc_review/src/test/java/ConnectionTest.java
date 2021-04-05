import org.junit.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/12/10 14:06
 **/
public class ConnectionTest {

	@Test
	public void testConnection1() throws SQLException {
		Driver driver = new com.mysql.jdbc.Driver();

		String url = "jdbc:mysql://192.168.1.100:3306/test?useSSL=false";
		Properties info = new Properties();
		info.setProperty("user", "root");
		info.setProperty("password", "mysql");

		Connection connect = driver.connect(url, info);

		System.out.println(connect);
	}

	// 使用反射来获取连接
	@Test
	public void testConnection2() throws Exception {

		Class mysqlDriverClass = Class.forName("com.mysql.jdbc.Driver");
		Driver driver = (Driver) mysqlDriverClass.newInstance();

		Properties info = new Properties();
		info.setProperty("user", "root");
		info.setProperty("password", "mysql");

		String url = "jdbc:mysql://192.168.1.100:3306/test?useSSL=false";

		Connection connect = driver.connect(url, info);

		System.out.println(connect);

	}

	@Test
	public void testConnection3() throws Exception {

		Class mysqlDriverClass = Class.forName("com.mysql.jdbc.Driver");
		Driver driver = (Driver) mysqlDriverClass.newInstance();

		DriverManager.registerDriver(driver);

		String url = "jdbc:mysql://192.168.1.100:3306/test?useSSL=false";
		String user = "root";
		String password = "mysql";

		Connection connection = DriverManager.getConnection(url, user, password);

		System.out.println(connection);
	}

	@Test
	public void testConnection4() throws Exception {
		String url = "jdbc:mysql://192.168.1.100:3306/test?useSSL=false";
		String user = "root";
		String password = "mysql";

		Class.forName("com.mysql.jdbc.Driver");

		Connection connection = DriverManager.getConnection(url, user, password);

		System.out.println(connection);
	}

//	 通过加载配置文件获取连接
	@Test
	public void testConnection() throws Exception {
		InputStream is = ConnectionTest.class.getClassLoader().getResourceAsStream("jdbc.properties");

		Properties pros = new Properties();

		pros.load(is);

		String user = pros.getProperty("user");
		String password = pros.getProperty("password");
		String url = pros.getProperty("url");
		String driverClass = pros.getProperty("driverClass");

		Class.forName(driverClass);

		Connection connection = DriverManager.getConnection(url, user, password);

		System.out.println(connection);
	}
}
