package dao;

import bean.Customer;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/12/26 21:45
 **/
public class CustomerDAOImpl_update extends BaseDAO_update<Customer> implements CustomerDAO {
	@Override
	public void insert(Connection conn, Customer customer) {
		String sql = "insert into customers(name, email, birth) values(?, ?, ?)";
		update(conn, sql, customer.getName(), customer.getEmail(), customer.getBirth());
	}

	@Override
	public void deleteById(Connection connection, int id) {
		String sql = "delete from customers where id = ?";
		update(connection, sql, id);
	}

	@Override
	public void updateById(Connection connection, Customer customer) {
		String sql = "update customers set name = ?, email = ?, birth = ? where = ?";
		update(connection, sql, customer.getName(), customer.getEmail(), customer.getBirth(), customer.getId());
	}

	@Override
	public Customer getCustomerById(Connection connection, int id) {
		String sql = "select id, name, email, birth from customers where id = ?";
		Customer customer = getInstance(connection,  sql, id);
		return customer;
	}

	@Override
	public List<Customer> getAll(Connection connection) {
		String sql = "select id, name, email, birth from customers";
		List<Customer> customers = getInstances(connection,  sql);
		return customers;
	}

	@Override
	public Long getCount(Connection connection) {
		String sql = "select count(*) count from customers";
		return getValue(connection, sql);
	}

	@Override
	public Date getMaxBirth(Connection connection) {
		String sql = "select max(birth) max_birth from customers";
		return getValue(connection, sql);
	}
}
