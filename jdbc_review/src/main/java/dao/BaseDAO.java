package dao;

import utils.JDBCUtils;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0
 * @Description: 针对数据表的通用操作
 * @Date 2020/12/26 9:56
 **/
public abstract class BaseDAO {
	// 通用增删改操作
	public int update(Connection conn, String sql, Object... args) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}

			return ps.executeUpdate();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		} finally {
			JDBCUtils.closeResource(null, ps);
		}

		return 0;
	}

	// 通用查询操作
	public <T> T getInstance(Connection conn, Class<T> clazz, String sql, Object... args) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}

			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();

			int columnCount = rsmd.getColumnCount();

			if (rs.next()) {
				T t = clazz.newInstance();
				for (int i = 0; i < columnCount; i++) {
					Object columnValue = rs.getObject(i + 1);
					String columnLabel = rsmd.getColumnLabel(i + 1);

					Field field = clazz.getDeclaredField(columnLabel);
					field.setAccessible(true);
					field.set(t, columnValue);
				}
				return t;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeResource(null, ps, rs);
		}

		return null;
	}

	// 通用查询返回多条记录的操作
	public <T> List<T> getInstances(Connection conn, Class<T> clazz, String sql, Object... args) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<T> list = null;
		try {
			ps = conn.prepareStatement(sql);

			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}

			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();

			int columnCount = rsmd.getColumnCount();

			list = new ArrayList<>();
			while (rs.next()) {
				T t = clazz.newInstance();
				for (int i = 0; i < columnCount; i++) {
					Object columnValue = rs.getObject(i + 1);
					String columnLabel = rsmd.getColumnLabel(i + 1);

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
			JDBCUtils.closeResource(null, ps, rs);
		}

		return null;
	}

	/**
	 * 通用特殊值查询
	 *
	 * @param conn
	 * @param sql
	 * @param args
	 * @param <T>
	 * @return
	 */
	public <T> T getValue(Connection conn, String sql, Object... args) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				return (T) rs.getObject(1);
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		} finally {
			JDBCUtils.closeResource(null, ps, rs);
		}

		return null;
	}
}
