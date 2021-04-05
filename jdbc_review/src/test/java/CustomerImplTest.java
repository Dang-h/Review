import bean.Customer;
import dao.CustomerDAOImpl;
import org.junit.Test;
import utils.JDBCUtils;

import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/12/26 22:10
 **/
public class CustomerImplTest {

	private CustomerDAOImpl dao = new CustomerDAOImpl();

	@Test
	public void testInsert() {
		Connection conn = null;
		try {
			conn = JDBCUtils.getConnection();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date date = dateFormat.parse("2020-12-26");
			Customer customer = new Customer(1, "测试插入", "test@gmail.com", new Date(date.getTime()));
			dao.insert(conn, customer);
			System.out.println("插入成功！");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeResource(conn, null);
		}

	}

}
