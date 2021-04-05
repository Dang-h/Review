package kafka.interceptor;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * 实现一个简单的双 interceptor 组成的拦截链。<p>
 * 第一个 interceptor 会在消息发送前将时间戳信息加到消息 value 的最前部；<p>
 * 第二个 interceptor 会在消息发送后更新成功发送消息数或失败发送消息数。<p>
 * 统计发送消息成功和发送失败消息数，并在 producer 关闭时打印这两个计数器
 *
 * @author DangHao
 */
public class CounterInterceptor implements ProducerInterceptor<String, String> {
	private int successCounter = 0;
	private int errorCounter = 0;

	@Override
	public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
		return record;
	}

	/**
	 * 该方法会在消息从RecordAccumulator成功发动消息给Broker之后，或者发送过程中失败时调用
	 *
	 * @param metadata
	 * @param exception
	 */
	@Override
	public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
		if (exception != null) {
			errorCounter++;
		} else {
			successCounter++;
		}
	}

	@Override
	public void close() {
		System.out.println("Successful sent: " + successCounter);
		System.out.println("Failed sent: " + errorCounter);
	}

	@Override
	public void configure(Map<String, ?> configs) {

	}
}
