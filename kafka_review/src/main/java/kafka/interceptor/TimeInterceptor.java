package kafka.interceptor;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * 实现一个简单的双 interceptor 组成的拦截链。<p>
 * 第一个 interceptor 会在消息发送前将时间戳信息加到消息 value 的最前部；<p>
 * 第二个 interceptor 会在消息发送后更新成功发送消息数或失败发送消息数。<p>
 * 增加时间拦截器，给每条消息加一个时间戳
 * @author DangHao
 */
public class TimeInterceptor implements ProducerInterceptor<String, String> {

	/**
	 * 在消息被序列化以及计算分区前调用<p>
	 * 可以在里边对消息做任何操作，但最好别修改消息的所属topic和所属分区
	 *
	 * @param record
	 * @return
	 */
	@Override
	public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
		// 创建一个新的record将时间戳写在消息的最前部
		ProducerRecord<String, String> newRecord = new ProducerRecord<>(
				record.topic(),
				record.partition(),
				record.timestamp(),
				record.key(),
				System.currentTimeMillis() + "-" + record.value().toString());

		return newRecord;
	}

	/**
	 * 该方法会在消息从RecordAccumulator成功发动消息给Broker之后，或者发送过程中失败时调用<p>
	 * 不要在此方法中放太重的逻辑以防拖慢producer发送消息的效率
	 *
	 * @param metadata
	 * @param exception
	 */
	@Override
	public void onAcknowledgement(RecordMetadata metadata, Exception exception) {

	}

	/**
	 * 关闭interceptor，清理资源
	 */
	@Override
	public void close() {

	}

	/**
	 * 获取配置信息，在初始化的时候调用
	 *
	 * @param configs
	 */
	@Override
	public void configure(Map<String, ?> configs) {

	}
}
