import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/12/11 22:07
 **/
public class GetConnectionTest {
	public static void main(String[] args) throws Exception {
		InputStream is = GetConnectionTest.class.getClassLoader().getResourceAsStream("jdbc.properties");

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
