package jdbc;

import org.apache.commons.dbutils.QueryRunner;
import utils.DruidUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author DangHao
 */
public class TestDurid {

	static Connection conn = DruidUtils.getConnection();
	static QueryRunner qr = new QueryRunner(DruidUtils.getDataSource());

	public static void main(String[] args) {
		addData();
	}

	private static int addData() {
		String sql = "INSERT INTO testcode.employee VALUES(?,?)";
		try {
			int count = 0;
			Object[] obj = {4, 123.4};
			qr.update(sql, obj);
			System.out.println("insert success!");
			return count;
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return 0;
	}


}
