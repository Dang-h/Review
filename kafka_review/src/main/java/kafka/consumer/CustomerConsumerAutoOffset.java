package kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public class CustomerConsumerAutoOffset {
	public static void main(String[] args) {
		// 自动提交Offset
		/*
		KafkaConsumer：需要创建一个消费者对象，用来消费数据
		ConsumerConfig：获取所需的一系列配置参数
		ConsumerRecord：每条数据都要封装成一个 ConsumerRecord 对象
		 */

		Properties prop = new Properties();

		prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop100:9092");
		prop.put(ConsumerConfig.GROUP_ID_CONFIG, "group_first");
		prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		prop.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");
		prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);

		consumer.subscribe(Arrays.asList("first"));

		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(100);
			for (ConsumerRecord<String, String> record : records) {
				System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
			}
		}
	}
}
