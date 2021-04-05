package kafka.interceptor;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class CustomInterceptor {
	public static void main(String[] args) throws IOException {
		// 设置配置信息
		Properties prop = new Properties();
		FileInputStream conf = new FileInputStream("conf/kfk_conf.properties");
		prop.load(conf);

		// 构建拦截器链路
		ArrayList<String> inteceptors = new ArrayList<>();
		inteceptors.add("kafka.interceptor.TimeInterceptor");
		inteceptors.add("kafka.interceptor.CounterInterceptor");

		prop.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, inteceptors);

		KafkaProducer<String, String> producer = new KafkaProducer<>(prop);

		for (int i = 0; i < 10; i++) {
			ProducerRecord<String, String> record = new ProducerRecord<>("first", "msg - " + i);
			producer.send(record);
		}

		// 关闭producer才会调用interceptor的close()
		producer.close();

	}
}
