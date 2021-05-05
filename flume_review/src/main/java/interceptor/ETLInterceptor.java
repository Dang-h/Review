package interceptor;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import utils.JsonUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 过滤掉Json格式不对的数据
 */
public class ETLInterceptor implements Interceptor {
	@Override
	public void initialize() {

	}

	@Override
	public Event intercept(Event event) {
		byte[] body = event.getBody();
		String log = new String(body, StandardCharsets.UTF_8);

		if (JsonUtil.validJson(log)) {
			return event;
		}

		return null;
	}

	@Override
	public List<Event> intercept(List<Event> list) {

		list.removeIf(next -> intercept(next) == null);
		/*
		Interator<Event> iterator = list.iterator();
		while (iterator.hasNext()) {
			Event next = iterator.next();
			if (intercept(next) == null) {
				iterator.remove();
			}
		}*/

		return list;
	}

	public static class Builder implements Interceptor.Builder {

		@Override
		public Interceptor build() {
			return new ETLInterceptor();
		}

		@Override
		public void configure(Context context) {

		}
	}

	@Override
	public void close() {

	}
}
