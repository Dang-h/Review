package kafka.producer;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

public class CustomProducerWithCallback {
	public static void main(String[] args) {
		// 带回调函数得API
		// 回调函数会在producer收到ack是调用，异步调用
		// 分别是：RecordMetadata和Exception
		// isNull(Exception) ? success : failure

		Properties prop = new Properties();

		prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop100:9092");
		prop.put(ProducerConfig.ACKS_CONFIG, "all");
		prop.put(ProducerConfig.RETRIES_CONFIG, 1);
		// 批次大小， 16k
		prop.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		// 等待时间，1ms
		prop.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		// RecordAccumulator缓冲区大小，32M
		prop.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");
		prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");

		KafkaProducer<String, String> producer = new KafkaProducer<>(prop);

		for (int i = 0; i < 10; i++) {
			producer.send(new ProducerRecord<String, String>("first",
							Integer.toString(i), Integer.toString(i)),
					new Callback() {

						// 回调函数，该方法会在Producer接收到ack是调用
						@Override
						public void onCompletion(RecordMetadata metadata, Exception exception) {
							if (exception == null) {
								System.out.println("Success --" + metadata.offset());
							} else {
								exception.printStackTrace();
							}
						}
					});
		}

		producer.close();
	}
}
