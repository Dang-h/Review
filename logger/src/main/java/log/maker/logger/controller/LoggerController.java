package log.maker.logger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接收生成的模拟日志并处理
 */
@RestController
@Slf4j
public class LoggerController {
	@Autowired
	KafkaTemplate kafkaTemplate;

	@Value("${kafka.topic1}")
	private String topic1;

	@Value("${kafka.topic2}")
	private String topic2;

	@RequestMapping("/app_log")
	public String appLog(@RequestBody String mockLog) {
		System.out.println(mockLog);
		log.info(mockLog);


//		 根据日志类型发送到不同topic中
		JSONObject jsonObject = JSON.parseObject(mockLog);
		String topic1Json = jsonObject.getString(topic1);

		if (topic1Json != null) {
			kafkaTemplate.send(topic1, mockLog);
		} else {
			kafkaTemplate.send(topic2, mockLog);
		}

		return "success!";
	}
}
