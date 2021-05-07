package project.team;

import com.xkzhangsan.time.calendar.CalendarWrapper;
import com.xkzhangsan.time.calendar.DayWrapper;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.xkzhangsan.time.calculator.DateTimeCalculatorUtil.*;
import static com.xkzhangsan.time.calendar.CalendarUtil.generateCalendar;
import static com.xkzhangsan.time.holiday.Holiday.getLocalHoliday;


public class DateInfo {

	public static void main(String[] args) {
		generateCalendar(2021);
	}
	/**
	 * 根据指定年份生成日期信息<br>
	 * date_id:日期<br>
	 * week_id:本年度第几周<br>
	 * week_day:本周第几天<br>
	 * day:本月第几天<br>
	 * month:本年度第几月<br>
	 * quarter:本年度第几季度<br>
	 * year:年份<br>
	 * is_workday:是否是周末；1:是<br>
	 * holiday_id:是否是节日；1:是<br>
	 *
	 * @param y 年份
	 */
	public void generateDateInfo(int y) throws Exception {
		CalendarWrapper calendarWrapper = generateCalendar(y);

		File outPutPath = new File("E:\\Develop\\Coding\\Java\\Review\\java_review\\src\\tmp\\dateInfo.txt");
		FileWriter fw = new FileWriter(outPutPath);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (DayWrapper dayWrapper : calendarWrapper.getDayList()) {
			Date date = dayWrapper.getDate();

			String date_id = sdf.format(date);
			int week_id = weekOfYear(date);
			int week_day = getDayOfWeek(date);
			int day = getDayOfMonth(date);
			int month = getMonth(date);
			int quarter = getQuarter(date);
			int year = getYear(date);
			int is_workday = isWeekend(date) ? 1 : 0;
			int holiday_id = getLocalHoliday(date).isEmpty() ? 0 : 1;

			String dateInfo = "";
			dateInfo = date_id + "\t"
					+ week_id + "\t"
					+ week_day + "\t"
					+ day + "\t"
					+ month + "\t"
					+ quarter + "\t"
					+ year + "\t"
					+ is_workday + "\t"
					+ holiday_id +"\n";
			fw.write(dateInfo);
		}
		fw.close();
	}
}
