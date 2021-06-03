package utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;

public class HBaseUtils {

	public static Admin getHBaseAdmin() {

		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "hadoop100,hadoop101,hadoop102");

		Connection conn = null;
		try {
			conn = ConnectionFactory.createConnection(conf);
			Admin admin = conn.getAdmin();
			return admin;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
