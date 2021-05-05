package utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

public class JsonUtil {
	public static boolean validJson(String log) {
		long cnt = 0;
		try {
			JSON.parse(log);
			return true;
		} catch (JSONException e) {
			cnt += 1;
			System.out.println("cnt = " + cnt);
			e.printStackTrace();
			return false;
		}
	}
}
