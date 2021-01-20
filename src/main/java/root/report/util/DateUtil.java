package root.report.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

	// 日期格式
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_YYYY_MM = "yyyy-MM";
	public static final String FORMAT_YYYY = "yyyy";
	public static final String FORMAT_HH_MM = "HH:mm";
	public static final String FORMAT_HH_MM_SS = "HH:mm:ss";
	public static final String FORMAT_MM_SS = "mm:ss";
	public static final String FORMAT_MM_DD_HH_MM = "MM-dd HH:mm";
	public static final String FORMAT_MM_DD_HH_MM_SS = "MM-dd HH:mm:ss";
	public static final String FORMAT_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
	public static final String FORMAT_YYYY2MM2DD = "yyyy.MM.dd";
	public static final String FORMAT_YYYY2MM2DD_HH_MM = "yyyy.MM.dd HH:mm";
	public static final String FORMAT_MMCDD_HH_MM = "MM月dd日 HH:mm";
	public static final String FORMAT_MMCDD = "MM月dd日";
	public static final String FORMAT_YYYYCMMCDD = "yyyy年MM月dd日";

	public static final long ONE_DAY = 1000 * 60 * 60 * 24;

	public static final String[] weekDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
	public static final String[] weekDays2 = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

	//判断选择的日期是否是本周（分从周日开始和从周一开始两种方式）
	public static boolean isThisWeek(Date time) {
//        //周日开始计算
//      Calendar calendar = Calendar.getInstance();

		//周一开始计算
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);

		calendar.setTime(time);

		int paramWeek = calendar.get(Calendar.WEEK_OF_YEAR);

		if (paramWeek == currentWeek) {
			return true;
		}
		return false;
	}

	//判断选择的日期是否是今天
	public static boolean isToday(Date time) {
		return isThisTime(time, "yyyy-MM-dd");
	}

	//判断选择的日期是否是本月
	public static boolean isThisMonth(Date time) {
		return isThisTime(time, "yyyy-MM");
	}

	//判断选择的日期是否是本月
	public static boolean isThisYear(Date time) {
		return isThisTime(time, "yyyy");
	}

	//判断选择的日期是否是昨天
	public static boolean isYesterDay(Date time) {
		Calendar cal = Calendar.getInstance();
		long lt = time.getTime() / 86400000;
		long ct = cal.getTimeInMillis() / 86400000;
		if ((ct - lt) == 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 比较是否是当前时间
	 * @param date
	 * @param pattern
	 * @return
	 */
	private static boolean isThisTime(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);

		String param = sdf.format(date);//参数时间

		String now = sdf.format(new Date());//当前时间

		if (param.equals(now)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取某年某月有多少天
	 */
	public static int getDayOfMonth(int year, int month) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, 0); //输入类型为int类型
		return c.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获取两个时间相差的天数
	 *
	 * @return time1 - time2相差的天数
	 */
	public static int getDayOffset(long time1, long time2) {
		// 将小的时间置为当天的0点
		long offsetTime;
		if (time1 > time2) {
			offsetTime = time1 - getDayStartTime(getCalendar(time2)).getTimeInMillis();
		} else {
			offsetTime = getDayStartTime(getCalendar(time1)).getTimeInMillis() - time2;
		}
		return (int) (offsetTime / ONE_DAY);
	}

	public static Calendar getCalendar(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar;
	}

	public static Calendar getDayStartTime(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	/**
	 * 时间戳 转 明文
	 * @param time
	 * @return
	 */
	public static String getDurationInString(long time) {
		String durStr = "";
		if (time == 0) {
			return "0秒";
		}
		time = time / 1000;
		long hour = time / (60 * 60);
		time = time - (60 * 60) * hour;
		long min = time / 60;
		time = time - 60 * min;
		long sec = time;
		if (hour != 0) {
			durStr = hour + "时" + min + "分" + sec + "秒";
		} else if (min != 0) {
			durStr = min + "分" + sec + "秒";
		} else {
			durStr = sec + "秒";
		}
		return durStr;
	}

	/**
	 * 获取当前时间是星期几
	 *
	 * @param dt
	 * @return
	 */
	public static String getWeekOfDate(Date dt) {

		Calendar cal = Calendar.getInstance();

		cal.setTime(dt);
		int w = (cal.get(Calendar.DAY_OF_WEEK) +5) %7;
		return weekDays[w];
	}


	/**
	 * 将日期格式的字符串转换为长整型
	 *
	 * @param date
	 * @param format
	 * @return
	 */
	public static long convertToLong(String date, String format) {
		try {
			if (date!=null && 0<date.length()) {
				if (format ==null || format.length() == 0) {
					format = TIME_FORMAT;
				}
				SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
				return formatter.parse(date).getTime();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 将长整型数字转换为日期格式的字符串
	 *
	 * @param time
	 * @return
	 */
	public static String convertToString(long time) {
		return convertToString(time,TIME_FORMAT);
	}




	/**
	 * 将长整型数字转换为日期格式的字符串
	 *
	 * @param time
	 * @return
	 */
	public static String convertToString(long time,String format) {
		if (time > 0) {
			SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
			Date date = new Date(time);
			return formatter.format(date);
		}
		return "";
	}


	/**
	 * 获取当前周的第一天
	 * @return
	 */
	public static String getWeekFirstDayString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd", Locale. getDefault());
		Calendar cal = Calendar.getInstance();
		int day_of_week = cal.get(Calendar. DAY_OF_WEEK) - 1;
		if (day_of_week == 0 ) {
			day_of_week = 7 ;
		}
		cal.add(Calendar.DATE , -day_of_week + 1 );
		return simpleDateFormat.format(cal.getTime());
	}



	public static long getWeekFirstDaytime() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd", Locale. getDefault());
		Calendar cal = Calendar.getInstance();
		int day_of_week = cal.get(Calendar. DAY_OF_WEEK) - 1;
		if (day_of_week == 0 ) {
			day_of_week = 7 ;
		}
		cal.add(Calendar.DATE , -day_of_week + 1 );
		return cal.getTime().getTime();
	}

	//获取上周对应的日期
	public static String getLastWeekString(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		calendar.add(Calendar.DATE, -7);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd", Locale. getDefault());
		return simpleDateFormat.format(calendar.getTime());
	}

	//获取下周对应的日期
	public static String getNextWeekString(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		calendar.add(Calendar.DATE, 7);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd", Locale. getDefault());
		return simpleDateFormat.format(calendar.getTime());
	}


	/**
	 * start
	 * 本周开始时间戳 - 以星期一为本周的第一天
	 */
	public static Calendar getWeekStartTimeCalendar() {
		Calendar cal = Calendar.getInstance();
		int day_of_week = cal.get(Calendar. DAY_OF_WEEK) - 1;
		if (day_of_week == 0 ) {
			day_of_week = 7 ;
		}
		cal.add(Calendar.DATE , -day_of_week + 1 );
		return cal;
	}



	/**
	 * start
	 * 本周开始时间戳 - 以星期一为本周的第一天
	 */
	public static String getWeekStartTime() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyyMMdd", Locale. getDefault());
		Calendar cal = Calendar.getInstance();
		int day_of_week = cal.get(Calendar. DAY_OF_WEEK) - 1;
		if (day_of_week == 0 ) {
			day_of_week = 7 ;
		}
		cal.add(Calendar.DATE , -day_of_week + 1 );


		return simpleDateFormat.format(cal.getTime());
	}

	/**
	 * end
	 * 本周结束时间戳 - 以星期一为本周的第一天
	 */
	public static String getWeekEndTime() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyyMMdd", Locale. getDefault());
		Calendar cal = Calendar.getInstance();
		int day_of_week = cal.get(Calendar. DAY_OF_WEEK) - 1;
		if (day_of_week == 0 ) {
			day_of_week = 7 ;
		}
		cal.add(Calendar.DATE , -day_of_week + 7 );
		return simpleDateFormat.format(cal.getTime()) + "235959999";
	}

	/**
	 * 获取当前时间
	 * @return yyyy-MM-dd HH:mm:ss
	 */
	public static String getCurrentTimm(){
		return convertToString(System.currentTimeMillis());
	}


	public static String getMonthAbbr(int month){
		String abbr = "";
		switch(month){
			case 1: abbr = "JAN";break;
			case 2: abbr = "FEB";break;
			case 3: abbr = "MAR";break;
			case 4: abbr = "APR";break;
			case 5: abbr = "MAY";break;
			case 6: abbr = "JUN";break;
			case 7: abbr = "JUL";break;
			case 8: abbr = "AUG";break;
			case 9: abbr = "SEP";break;
			case 10: abbr = "OCT";break;
			case 11: abbr = "NOV";break;
			case 12: abbr = "DEC";break;
		}
		return abbr;
	}
	
	public static String getEnMonth(int month){
		String abbr = "";
		switch(month){
			case 1: abbr = "01";break;
			case 2: abbr = "02";break;
			case 3: abbr = "03";break;
			case 4: abbr = "04";break;
			case 5: abbr = "05";break;
			case 6: abbr = "06";break;
			case 7: abbr = "07";break;
			case 8: abbr = "08";break;
			case 9: abbr = "09";break;
			case 10: abbr = "10";break;
			case 11: abbr = "11";break;
			case 12: abbr = "12";break;
		}
		return abbr;
	}
	public static String getNextEnMonth(int month){
		String abbr = "";
		switch(month){
			case 1: abbr = "02";break;
			case 2: abbr = "03";break;
			case 3: abbr = "04";break;
			case 4: abbr = "05";break;
			case 5: abbr = "06";break;
			case 6: abbr = "07";break;
			case 7: abbr = "08";break;
			case 8: abbr = "09";break;
			case 9: abbr = "10";break;
			case 10: abbr = "11";break;
			case 11: abbr = "12";break;
			case 12: abbr = "01";break;
		}
		return abbr;
	}
}
