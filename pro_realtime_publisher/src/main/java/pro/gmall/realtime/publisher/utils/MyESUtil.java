package pro.gmall.realtime.publisher.utils;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;

import java.io.IOException;

public class MyESUtil {

	JestClientFactory jestClientFactory = null;

	public JestClient getJestClient() {
		if (jestClientFactory == null) {
			build();
		}

		return jestClientFactory.getObject();
	}

	private void build() {
		String esUrl = new MyPropertiesUtil().load("config.properties").getProperty("es.url");

		jestClientFactory = new JestClientFactory();
		jestClientFactory.setHttpClientConfig(new HttpClientConfig.Builder(esUrl)
				.multiThreaded(true)
				.maxTotalConnection(20)
				.connTimeout(10000)
				.readTimeout(1000)
				.build()
		);
	}

	public void closeJestClient(JestClient jestClient) {
		if (jestClient != null) {
			try {
				jestClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		JestClient jedisClient = new MyESUtil().getJestClient();
		Get get = new Get.Builder("gmall_dau_info-query", "mid_112").build();
		try {
			DocumentResult execute = jedisClient.execute(get);
			System.out.println("execute = " + execute.getJsonString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			new MyESUtil().closeJestClient(jedisClient);
		}
	}
}
