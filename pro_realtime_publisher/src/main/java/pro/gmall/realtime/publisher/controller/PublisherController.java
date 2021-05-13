package pro.gmall.realtime.publisher.controller;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.gmall.realtime.publisher.service.impl.ESServiceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 发布接口数据
 *
 * @author DangHao
 */
@RestController
public class PublisherController {

	/**
	 * 访问路径：http://publisher:8070/realtime-total?date=2019-02-01<br>
	 * 响应数据：[{"id":"dau","name":"新增日活","value":1200},<br>
	 * {"id":"new_mid","name":"新增设备","value":233} ]<br>
	 *
	 * @param dt 日期
	 * @return
	 */
	@RequestMapping("/realtime-total")
	public Object realtimeTotal(@RequestParam("date") String dt) {
		ESServiceImpl esService = new ESServiceImpl();
		ArrayList<Map<String, Object>> rsList = new ArrayList<>();
		HashMap<String, Object> dauMap = new HashMap<>(16);

		dauMap.put("id", "dau");
		dauMap.put("name", "新增日活");

		Long dauTotal = esService.getDauTotal(dt);
		if (dauTotal == null) {
			dauMap.put("value", 0L);
		} else {
			dauMap.put("value", dauTotal);
		}
		rsList.add(dauMap);

		HashMap<String, Object> midMap = new HashMap<>(16);
		midMap.put("id", "new_mid");
		midMap.put("name", "新增设备");
		midMap.put("value", 1234);
		rsList.add(midMap);

		return rsList;
	}

	@RequestMapping("/realtime-hour")
	public Object realtimeHour(@RequestParam("id") String id, @RequestParam("date") String date) {
		ESServiceImpl esService = new ESServiceImpl();

		if ("dau".equals(id)) {
			HashMap<String, Object> rsMap = new HashMap<>(16);
			Map<String, Long> tdDau = esService.getDauHour(date);
			rsMap.put("today", tdDau);

			String yd = getYd(date);
			Map<String, Long> ydMap = esService.getDauHour(yd);
			rsMap.put("yesterday", ydMap);

			return rsMap;
		} //else if ("order_amount".equals(id)) {
//			HashMap<String, Object> amountMap = new HashMap<>(16);

//		}
			return null;

	}

	/**
	 * 获取昨天日期
	 *
	 * @param td 今天日期
	 * @return 昨天日期
	 */
	private String getYd(String td) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		String yd = null;
		try {
			Date tdDate = dateFormat.parse(td);
			Date ydDate = DateUtils.addDays(tdDate, -1);
			yd = dateFormat.format(ydDate);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException("日期转换失败");
		}
		return yd;
	}

	public static void main(String[] args) {
//		Object o = new PublisherController().realtimeTotal("2021-05-12");
//		System.out.println("o = " + o);
		PublisherController pc = new PublisherController();
		Object dau = pc.realtimeHour("dau", "2021-05-12");

		System.out.println("dau = " + dau);
	}
}
