import com.xkzhangsan.time.calendar.CalendarWrapper;
import com.xkzhangsan.time.calendar.DayWrapper;
import org.junit.jupiter.api.Test;
import project.team.DateInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.xkzhangsan.time.calculator.DateTimeCalculatorUtil.*;
import static com.xkzhangsan.time.calendar.CalendarUtil.generateCalendar;
import static com.xkzhangsan.time.holiday.Holiday.getLocalHoliday;
import static utils.DateUtils.convertString2Date;

public class TestDateUtils {

	@Test
	public void testGetCalendar() {
		String date_id = "2019-05-01";

		Date date = convertString2Date(date_id, "yyyy-MM-dd");
		String week_id = "" + weekOfYear(date);
		String week_day = "" + getDayOfWeek(date);
		String day = "" + getDayOfMonth(date);
		String month = "" + getMonth(date);
		String quarter = "" + getQuarter(date);
		String year = "" + getYear(date);
		String is_workday = isWeekend(date) ? "1" : "0";
		String holiday_id = getLocalHoliday(date).isEmpty() ? "0" : "1";

		System.out.println(date_id + "\t"
				+ week_id + "\t"
				+ week_day + "\t"
				+ day + "\t"
				+ month + "\t"
				+ quarter + "\t"
				+ year + "\t"
				+ is_workday + "\t"
				+ holiday_id);
	}

	@Test
	public void testGenerateCalendar() {
		CalendarWrapper calendarWrapper = generateCalendar(2021);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (DayWrapper dayWrapper : calendarWrapper.getDayList()) {
			Date date = dayWrapper.getDate();

			String date_id = sdf.format(date);
			String week_id = "" + weekOfYear(date);
			String week_day = "" + getDayOfWeek(date);
			String day = "" + getDayOfMonth(date);
			String month = "" + getMonth(date);
			String quarter = "" + getQuarter(date);
			String year = "" + getYear(date);
			String is_workday = isWeekend(date) ? "1" : "0";
			String holiday_id = getLocalHoliday(date).isEmpty() ? "0" : "1";
			System.out.println(date_id + "\t"
					+ week_id + "\t"
					+ week_day + "\t"
					+ day + "\t"
					+ month + "\t"
					+ quarter + "\t"
					+ year + "\t"
					+ is_workday + "\t"
					+ holiday_id );
		}


	}

	@Test
	public void testGenerate() throws Exception {
		DateInfo dateInfo = new DateInfo();
		dateInfo.generateDateInfo(2021);
	}


}
