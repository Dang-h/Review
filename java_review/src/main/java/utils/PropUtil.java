package utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public class PropUtil {

	public static Properties load(String propName) throws IOException {
		Properties prop = new Properties();

		prop.load(new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(propName)), StandardCharsets.UTF_8));

		return prop;
	}
}
