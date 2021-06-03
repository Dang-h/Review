package log.maker.logger.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import log.maker.logger.bean.CityInfo;
import log.maker.logger.bean.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
/**
 * 给kafka发送数据
 */
public class KafkaSender {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	private Gson gson = new GsonBuilder().create();

	@Value("${kafka.topic1}")
	private String topic1;

	/**
	 * 数据发往指定Topic的kafka
	 *
	 * @param topic
	 */
	public void sendMsg(String topic) {
		this.sendMsg(false, topic);
	}

	/**
	 * 生成模拟json发送个kafka <p>
	 * {"city":"2","temperature":12.3,"time":"2021-06-03 17:21:10"}
	 *
	 * @param flag 是否加时间
	 */
	public void sendMsg(Boolean flag, String topic) {
		Msg msg = new Msg();
		DecimalFormat df = new DecimalFormat("#.00");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (flag) {
			msg.setCity("" + (new Random().nextInt(5) + 1));
			msg.setTemperature(Double.valueOf(df.format((new Random().nextDouble()) * (100 - 1) + 1)));
			msg.setTime(dateFormat.format(new Date(System.currentTimeMillis())));
		} else {
			msg.setCity("" + (new Random().nextInt(5) + 1));
			msg.setTemperature(Double.valueOf(df.format((new Random().nextDouble()) * (100 - 1) + 1)));
		}

		System.out.println(gson.toJson(msg));
		kafkaTemplate.send(topic, gson.toJson(msg));
	}

	public void sendCityInfo(String topic) {
		CityInfo cityInfo = new CityInfo();
		Map<String, String> cities = new HashMap<>(5);
		cities.put("1", "北京");
		cities.put("2", "上海");
		cities.put("3", "昆明");
		cities.put("4", "杭州");
		cities.put("5", "深圳");

		int id = new Random().nextInt(5) + 1;

		cityInfo.setId("" + id);
		cityInfo.setName(cities.get("" + id));

		System.out.println(gson.toJson(cityInfo));
		kafkaTemplate.send(topic, gson.toJson(cityInfo));
	}

	public static void main(String[] args) {

		KafkaSender sender = new KafkaSender();
		for (int i = 0; i < 10; i++) {
			sender.sendCityInfo("test");
		}
	}
}
