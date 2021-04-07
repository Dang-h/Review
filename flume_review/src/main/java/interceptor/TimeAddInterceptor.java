package interceptor;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimeAddInterceptor implements Interceptor {

	private ArrayList<Event> events = new ArrayList<>();

	@Override
	public void initialize() {

	}

	@Override
	public Event intercept(Event event) {
		Map<String, String> headers = event.getHeaders();
		String ts = "" + System.currentTimeMillis();

		headers.put("timestamp", ts);
		return event;
	}

	@Override
	public List<Event> intercept(List<Event> list) {
		events.clear();
		for (Event event : list) {
			events.add(intercept(event));
		}
		return events;
	}

	@Override
	public void close() {

	}

	public static class Builder implements Interceptor.Builder {
		@Override
		public Interceptor build() {
			return new TimeAddInterceptor();
		}

		@Override
		public void configure(Context context) {

		}
	}

}
