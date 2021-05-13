package pro.gmall.realtime.publisher.service;

import java.util.Map;

/**
 * 从ES查询
 */
public interface ESService {
	/**
	 * 查询某天日活
	 *
	 * @param date 日期
	 * @return 日活
	 */
	public Long getDauTotal(String date);

	/**
	 * 查询某天某时段日活
	 *
	 * @param date 日期
	 * @return 时段，日活
	 */
	public Map<String, Long> getDauHour(String date);
}
