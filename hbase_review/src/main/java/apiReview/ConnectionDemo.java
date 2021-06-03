package apiReview;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

public class ConnectionDemo {
	public static void main(String[] args) throws IOException {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "hadoop100,hadoop101,hadoop102");
		Connection conn = ConnectionFactory.createConnection(conf);

		Table mytable = conn.getTable(TableName.valueOf("mytable"));

		Admin admin = conn.getAdmin();

		TableName[] tableNames = admin.listTableNames();

		for (TableName tableName : tableNames) {
			byte[] name = tableName.getName();
			System.out.println(new String(name));
		}

		conn.close();
	}
}
