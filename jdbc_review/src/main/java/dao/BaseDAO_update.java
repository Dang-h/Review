package dao;

import utils.JDBCUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0
 * @Description: 针对数据表的通用操作
 * 				 升级内容:获取父类泛型
 * 				 		①baseDAO中加上泛型参数
 * 				 		②子类继承时指定类
 * @Date 2020/12/26 9:56
 **/
public abstract class BaseDAO_update<T> {

	private Class<T> clazz = null;

	// 获取当前BaseDAO的子类的父类的泛型
	{
		Type genericSuperclass = this.getClass().getGenericSuperclass();
		ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
		// 获取子类的父类的泛型参数
		Type[] typeArguments = parameterizedType.getActualTypeArguments();
		Type typeArgument = (Class<T>)typeArguments[0];
	}

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
	public T getInstance(Connection conn, String sql, Object... args) {
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
	public List<T> getInstances(Connection conn, String sql, Object... args) {
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
