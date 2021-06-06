package log.maker.logger;

import log.maker.logger.controller.KafkaSender;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Random;

/**
 * @author DangHao
 */
@SpringBootApplication
public class LoggerApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(LoggerApplication.class, args);
		KafkaSender sender = run.getBean(KafkaSender.class);
		int n = 10;

		for (int i = 0; i < n; i++) {
			sender.sendMsg("msg");
			sender.sendCityInfo("city");
			try {
				Thread.sleep((new Random().nextInt(1000) + 200));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
