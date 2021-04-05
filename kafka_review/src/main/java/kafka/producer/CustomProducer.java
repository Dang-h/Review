package kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class CustomProducer {
	public static void main(String[] args) {

		// 不带回调函数得API

		// 2
		Properties properties = new Properties();

		// 3 kafkaCluster
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop100:9092");
		// ack = -1
		properties.put(ProducerConfig.ACKS_CONFIG, "all");
		// 重试次数
		properties.put(ProducerConfig.RETRIES_CONFIG, 3);
		// 批次大小，16k
		properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		//等待时间
		properties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		// RecordAccumulator缓冲区大小:32M
		properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

		// 1, <k,v>生产者输入的类型
		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

		// 5 数据
		for (int i = 0; i < 10; i++) {
			// 4 发送数据
			producer.send(new ProducerRecord<>("first", "test---" + i));
		}

		// 6
		producer.close();

	}
}
