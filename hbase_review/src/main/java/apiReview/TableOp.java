package apiReview;

import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import utils.HBaseUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TableOp {
	public static void main(String[] args) throws IOException {
		Admin admin = HBaseUtils.getHBaseAdmin();
		assert admin != null;
//		admin.deleteNamespace("testNamespace");
//		createNameSpace(admin);
		getAllNameSpace(admin);
		admin.close();
	}

	private static void getAllNameSpace(Admin admin) throws IOException {
		NamespaceDescriptor[] namespaceDescriptors = admin.listNamespaceDescriptors();

		for (NamespaceDescriptor namespaceDescriptor : namespaceDescriptors) {
			String name = namespaceDescriptor.getName();
			Map<String, String> confMap = namespaceDescriptor.getConfiguration();
			System.out.println(name + "--->" + confMap.entrySet());
		}
	}

	private static void createNameSpace(Admin admin) throws IOException {
		NamespaceDescriptor.Builder testNameSpace = NamespaceDescriptor.create("testNameSpace");

		Map<String, String> map = new HashMap<>(16);
		map.put("author", "test");
		map.put("desc", "test create namespace");

		testNameSpace.addConfiguration(map);

		NamespaceDescriptor build = testNameSpace.build();

		admin.createNamespace(build);
	}

}
