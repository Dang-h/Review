package pro.gmall.realtime.publisher.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author DangHao
 */
public class MyPropertiesUtil {

	/**
	 * 读取指定配置文件配置
	 *
	 * @param propertiesName 配置文件名
	 * @return 配置
	 */
	public Properties load(String propertiesName) {
		Properties prop = new Properties();

		try {
			prop.load(new InputStreamReader(
					Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesName)), StandardCharsets.UTF_8
			));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return prop;
	}

	public static void main(String[] args) {
		MyPropertiesUtil prop = new MyPropertiesUtil();
		String esUrls = prop.load("config.properties").getProperty("es.urls");
		String[] split = esUrls.split(",");
		List<String> strings = Arrays.asList(split);
		for (String string : strings) {
			System.out.println(string);
		}
	}
}
