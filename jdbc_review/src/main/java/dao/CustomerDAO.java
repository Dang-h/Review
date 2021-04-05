package dao;

import bean.Customer;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;

/**
 * @version 1.0
 * @Description: 针对customer表的操作
 * @Date 2020/12/26 21:38
 **/
public interface CustomerDAO {
	/**
	 * 将customer对象添加到数据库中
	 *
	 * @param conn
	 * @param customer
	 */
	void insert(Connection conn, Customer customer);

	/**
	 * 根据ID删除表中一条记录
	 *
	 * @param connection
	 * @param id
	 */
	void deleteById(Connection connection, int id);

	/**
	 * 针对内存中customer对象，修改表中指定的记录
	 *
	 * @param connection
	 * @param customer
	 */
	void updateById(Connection connection, Customer customer);

	/**
	 * 根据id查询对应的customer对象
	 *
	 * @param connection
	 * @param id
	 * @return
	 */
	Customer getCustomerById(Connection connection, int id);

	/**
	 * 查询表中所有记录构成的集合
	 *
	 * @param connection
	 * @return
	 */
	List<Customer> getAll(Connection connection);

	/**
	 * 返回数据表中的记录数
	 *
	 * @param connection
	 * @return
	 */
	Long getCount(Connection connection);

	/**
	 * 返回数据表中最大的生日
	 *
	 * @param connection
	 * @return
	 */
	Date getMaxBirth(Connection connection);
}
