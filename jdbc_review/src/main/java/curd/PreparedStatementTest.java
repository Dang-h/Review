package curd;

import bean.Customer;
import org.junit.jupiter.api.Test;
import utils.JDBCUtils;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @version 1.0
 * @Description: 使用preparedStatement实现数据库的增删改
 * @Date 2020/12/14 14:36
 **/
public class PreparedStatementTest {

	@Test
	public void testGetConn() throws Exception {
		System.out.println(JDBCUtils.getConnection());
	}

	// 向数据表中添加一条记录
	@Test
	public void testInsert() {
		Connection conn = null;
		PreparedStatement pS = null;

		try {
			conn = JDBCUtils.getConnection();

			String sql = "insert  into customers(name, email, birth) values (?,?,?)";
			pS = conn.prepareStatement(sql);
			pS.setString(1, "测试2");
			pS.setString(2, "test2@gmail.com");
			SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date date = spf.parse("2020-02-01");
			pS.setDate(3, new Date(date.getTime()));

			pS.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeResource(conn, pS);
		}
	}

	@Test
	public void testUpdate() {
		String sql = "update `order` set order_name = ? where order_id = ?";
		JDBCUtils.update(sql, "dd", "2");
	}

	@Test
	public void testCustomer4Query() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		try {
			connection = JDBCUtils.getConnection();
			String sql = "select id, name, email, birth from customers where id = ?";
			ps = connection.prepareStatement(sql);
			ps.setObject(1, 1);
			resultSet = ps.executeQuery();

			if (resultSet.next()) {
				int id = resultSet.getInt(1);
				String name = resultSet.getString(2);
				String email = resultSet.getString(3);
				Date birth = resultSet.getDate(4);

				Customer customer = new Customer(id, name, email, birth);
				System.out.println(customer);
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeResource(connection, ps, resultSet);
		}
	}

	@Test
	public void testGetInstance() {
		String sql = "select id, name, email, birth from customers where id = ?";
		Customer customer = JDBCUtils.getInstance(Customer.class, sql, 1);
		System.out.println(customer);
	}

	@Test
	public void testGetInstances() {
		String sql = "select name, email, birth from customers where id < ?";
		List<Customer> customerList = JDBCUtils.getInstances(Customer.class, sql, 20);
		customerList.forEach(System.out::println);
	}

	@Test
	public void testInsertBlob() throws Exception {
		Connection connection = JDBCUtils.getConnection();
		String sql = "insert into customers(name, email,birth, photo) values(?, ?, ?, ?) ";
		PreparedStatement ps = connection.prepareStatement(sql);

		ps.setObject(1, "测试");
		ps.setObject(2, "ceshi@outlook.com");
		ps.setObject(3, "2020-12-20");
		FileInputStream is = new FileInputStream(new File("src/test.jpg"));
		ps.setObject(4, is);
		ps.execute();

		JDBCUtils.closeResource(connection, ps);
	}

	@Test
	public void testGetBlob() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			connection = JDBCUtils.getConnection();
			String sql = "select id, name, email, birth, photo from customers where id = ?";

			ps = connection.prepareStatement(sql);
			ps.setObject(1, 21);

			resultSet = ps.executeQuery();

			if (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				String email = resultSet.getString("email");
				Date birth = resultSet.getDate("birth");

				Customer customer = new Customer(id, name, email, birth);
				System.out.println(customer);

				Blob photo = resultSet.getBlob("photo");
				is = photo.getBinaryStream();
				fos = new FileOutputStream("zhuyin.jpg");
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			JDBCUtils.closeResource(connection, ps, resultSet);
		}

	}
}
