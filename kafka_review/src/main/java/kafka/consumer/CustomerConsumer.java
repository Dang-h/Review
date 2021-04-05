package kafka.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

/**
 * @author DangHao
 */
public class CustomerConsumer {
	private static Map<TopicPartition, Long> currentOffset = new HashMap<>();
	/*
	手动提交Offset
	手动提交 offset 的方法有两种：分别是 commitSync（同步提交）和 commitAsync（异步
	提交）。两者的相同点是，都会将次 本次 poll 的一批数据最高的偏移量提交；
	不同点：commitSync 阻塞当前线程，一直到提交成功，并且会自动失败重试（由不可控因素导致，
	也会出现提交失败）；commitAsync 则没有失败重试机制，故有可能提交失败。
	 */


	public static void main(String[] args) {
		Properties prop = new Properties();
		prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop100:9092");
		prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		prop.put(ConsumerConfig.GROUP_ID_CONFIG, "group_first");
		prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

		//commitSync(prop);
		commitAsync(prop);

	}

	/**
	 * 同步提交 offset 有失败重试机制，故更加可靠;但是会阻塞当前线程，吞吐会受到影响
	 *
	 * @param prop
	 */
	public static void commitSync(Properties prop) {

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);
		consumer.subscribe(Arrays.asList("first", "second"));

		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(100);
			for (ConsumerRecord<String, String> record : records) {
				System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
			}
			// 同步提交，当前线程会阻塞直到offset提交成功
			consumer.commitSync();
		}

	}

	/**
	 * 异步提交更常用
	 *
	 * @param prop
	 */
	public static void commitAsync(Properties prop) {
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);
		consumer.subscribe(Arrays.asList("first", "second"));

		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(100);
			for (ConsumerRecord<String, String> record : records) {
				System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
			}

			// 异步提交
			consumer.commitAsync(new OffsetCommitCallback() {
				@Override
				public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
					if (exception != null) {
						System.out.println("Commit failed for " + offsets);
					}
				}
			});
		}
	}

	/**
	 * 当有新的消费者加入消费者组、已有的消费者推出消费者组或者所订阅的主题的分区发
	 * 生变化，就会触发到分区的重新分配，重新分配的过程叫做 Rebalance。<p>
	 * 消费者发生 Rebalance 之后，每个消费者消费的分区就会发生变化。因此消费者要首先
	 * 获取到自己被重新分配到的分区，并且定位到每个分区最近提交的 offset 位置继续消费。
	 * @param prop
	 */
	public static void customerStoreOffset(Properties prop) {
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);

		// 消费者订阅主题
		consumer.subscribe(Arrays.asList("first"), new ConsumerRebalanceListener() {
			/**
			 * 在Rebalance之前调用
			 * @param partitions
			 */
			@Override
			public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
				commitOffset(currentOffset);
			}

			/**
			 * 在rebalance之后调用
			 * @param partitions
			 */
			@Override
			public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
				currentOffset.clear();
				for (TopicPartition partition : partitions) {
					// 定位到最近提交的offset位置继续消费
					consumer.seek(partition, getOffset(partition));
				}
			}
		});

		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(100);
			for (ConsumerRecord<String, String> record : records) {
				System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
			}

			// 异步提交数据
			commitOffset(currentOffset);
		}
	}

	/**
	 * 获取某个分区的最新Offset
	 *
	 * @param partition
	 * @return
	 */
	private static long getOffset(TopicPartition partition) {
		return 0;
	}

	/**
	 * 提交消费者所有分区的Offset
	 *
	 * @param currentOffset
	 */
	private static void commitOffset(Map<TopicPartition, Long> currentOffset) {

	}
}
