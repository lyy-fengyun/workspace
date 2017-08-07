package com.cmsz.monitor.commons.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 日期、时间类
 * @author cmt
 *
 */
public class DateUtil {
	

	/**
	 * 日期转换 
	 * @author cmt
	 * @param time
	 * @param fmt:yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String formatTime(Timestamp time,String fmt) {
		if (time == null) {
			return "";
		}
		SimpleDateFormat myFormat = new SimpleDateFormat(fmt);
		return myFormat.format(time);
	}
	
	/**
	 * 日期转换 
	 * @author cmt
	 * @param time
	 * @param fmt:yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getformatDate(String time,String fmt) {
		if (time == null) {
			return "";
		}
		SimpleDateFormat myFormat = new SimpleDateFormat(fmt);
		return myFormat.format(time);
	}
	/**
	 * 获取系统当前时间（秒）
	 * @author cmt
	 * @return
	 */
	public static Timestamp getTime() {
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		String mystrdate = myFormat.format(calendar.getTime());
		return Timestamp.valueOf(mystrdate);
	}
	
	/**
	 * 获取当前日期(时间00:00:00)
	 * @author cmt
	 * @return
	 */
	public static Timestamp getDateFirst(){
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Calendar calendar = Calendar.getInstance();
		String mystrdate = myFormat.format(calendar.getTime());
		return Timestamp.valueOf(mystrdate);
	}
	
	/**
	 * 获取当前日期(时间23:59:59)
	 * @author cmt
	 * @return
	 */
	public static Timestamp getDateLast(){
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		Calendar calendar = Calendar.getInstance();
		String mystrdate = myFormat.format(calendar.getTime());
		return Timestamp.valueOf(mystrdate);
	}
	
	/**
	 * 获取当前日期
	 * @author cmt
	 * @return
	 */
	public static Date getDate(){
		Calendar calendar = Calendar.getInstance();
		return calendar.getTime();
	}
	/**
	 * 取得当前日期 20140807
	 * @return
	 */
	public static String getCurrDate(){
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		String mystrdate = myFormat.format(calendar.getTime());
		return mystrdate;
	}
	/**
	 * yyyy-MM-dd HH:mm:ss 转换成Timestamp
	 * @author cmt
	 * @param timeString
	 * @return
	 */
	public static Timestamp getTime(String timeString){
		return Timestamp.valueOf(timeString);
	}
	
	
	/**
	 * 自定义格式的字符串转换成日期
	 * @author cmt
	 * @param timeString
	 * @param fmt
	 * @return
	 * @throws Exception
	 */
	public static Timestamp getTime(String timeString,String fmt) throws Exception{
		SimpleDateFormat myFormat = new SimpleDateFormat(fmt);
		Date date= myFormat.parse(timeString);
		myFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return getTime(myFormat.format(date));
	}
	
	/**
	 * 格式化日期
	 * @author cmt
	 * @param date
	 * @param fmt
	 * @return
	 * @throws Exception
	 */
	public static String formatDate(Date date,String fmt){
		if (date == null) {
			return "";
		}
		SimpleDateFormat myFormat = new SimpleDateFormat(fmt);
		return myFormat.format(date);
	}
	
	/**
	 * 返回日期或者时间，如果传入的是日期，返回日期的00:00:00时间
	 * @author cmt
	 * @param timeString
	 * @return
	 * @throws Exception
	 */
	public static Timestamp getDateFirst(String timeString) throws Exception{
		if (null==timeString ||"".equals(timeString)) {
			return null;
		}
		if (timeString.length() > 10) {
			return getTime(timeString, "yyyy-MM-dd HH:mm:ss");
		} else {
			return getTime(timeString, "yyyy-MM-dd");
		}
	}
	
	
	/**
	 * 返回日期或者时间，如果传入的是日期，返回日期的23:59:59时间
	 * @author cmt
	 * @param timeString
	 * @return
	 * @throws Exception
	 */
	public static Timestamp getDateLast(String timeString) throws Exception{
		if (null==timeString ||"".equals(timeString)) {
			return null;
		}
		if (timeString.length() > 10) {
			return getTime(timeString, "yyyy-MM-dd HH:mm:ss");
		} else {
			return getTime(timeString +" 23:59:59", "yyyy-MM-dd HH:mm:ss");
		}
	}
	
	/**
	 * 获取本周 周一时间，返回 格式yyyy-MM-dd 00:00:00
	 * @author cmt
	 * @return
	 */
	public static Timestamp getMonday(){
		Calendar calendar= Calendar.getInstance(); 
		int dayofweek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayofweek == 0){
			dayofweek = 7;
			calendar.add(Calendar.DATE, -dayofweek + 1);
		}
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		String mystrdate = myFormat.format(calendar.getTime());
		return Timestamp.valueOf(mystrdate);
	}
	
	
	/**
	 * 获取本周 周日 时间，返回格式yyyy-MM-dd 23:59:59
	 * @author cmt
	 * @return
	 */
	public static Timestamp getSunday(){
		Calendar calendar= Calendar.getInstance(); 
		int dayofweek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		if (dayofweek == 0){
			dayofweek = 7;
			calendar.add(Calendar.DATE, -dayofweek + 7);
		}
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		String mystrdate = myFormat.format(calendar.getTime());
		return Timestamp.valueOf(mystrdate);
	}
	
	
	/**
	 * 增加天数
	 * @author cmt
	 * @param time
	 * @param day
	 * @return
	 */
	public static Timestamp addDay(Timestamp time,Integer day){
		Timestamp time2=new Timestamp(time.getTime()+day*1000l*60*60*24L);
		return time2;
	}
	
	/**
	 * 2个时间的相差天数
	 * @author cmt
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static Integer getDay(Timestamp time1,Timestamp time2){
		Long dayTime=(time1.getTime()-time2.getTime())/(1000*60*60*24);
		return dayTime.intValue();
	}
	
	/**
	 * 两个日期相减
	 * 格式 yyyyMMdd
	 * @param oldDate	
	 * @param newDate
	 * @return 相差的天数
	 * @throws ParseException 
	 */
	public static long getsubDate(String oldDate, String newDate) throws ParseException{
		SimpleDateFormat  sdf = new SimpleDateFormat("yyyyMMdd");
		Date d1 = sdf.parse(oldDate);
		Date d2= sdf.parse(newDate);
		return (d1.getTime() - d2.getTime()) / (3600L * 1000 * 24);
	}
	
	/**
	 * 两个日期相减
	 * 格式 yyyyMMdd
	 * @param oldDate	
	 * @param newDate
	 * @return 相差的天数
	 */
	public static long getsubDate(Date oldDate, Date newDate){
		return (oldDate.getTime() - newDate.getTime()) / (3600L * 1000 * 24);
	}
	
	/**
	 * 获取系统当前时间（分）
	 * @author cmt
	 * @return
	 */
	public static String getMinute() {
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMddHHmm");
		return myFormat.format(new Date());
	}
		
	
	/**
	 * 转换成时间 字符串格式必须为 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd
	 * @author cmt
	 * @return
	 * @throws ParseException 
	 */
	public static Date parseToDate(String val) throws ParseException{
		Date date = null;
		if(null!=val&& val.trim().length() != 0 && !"null".equals(val.trim().toLowerCase())){
			val = val.trim();
			if(val.length()>10){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				date = sdf.parse(val);
			}
			if(val.length() <= 10){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				date = sdf.parse(val);
			}
		}	
		return date;
	}
	
	/**
	 * 转换成时间 字符串格式
	 * @author yangquan
	 * @param String dateStr  the date of String
	 * @param String format   the format of date
	 * @return Date
	 * @throws ParseException 
	 */
	public static Date parseToDate(String dateStr,String format) throws ParseException{
		Date date = null;
		if(null!=dateStr  && dateStr.trim().length() != 0 && !"null".equals(dateStr.trim().toLowerCase())){
			dateStr = dateStr.trim();
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(dateStr);
		}	
		return date;
	}
	/**
	 * 获取上月的第一天yyyy-MM-dd 00:00:00和最后一天yyyy-MM-dd 23:59:59
	 * @author cmt
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Map<String,String> getPreMonth(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		Calendar cal = Calendar.getInstance();
		GregorianCalendar gcLast = (GregorianCalendar) Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		calendar.add(Calendar.MONTH, -1);
		Date theDate = calendar.getTime();
		gcLast.setTime(theDate);
		gcLast.set(Calendar.DAY_OF_MONTH, 1);
		String day_first_prevM = df.format(gcLast.getTime());
		StringBuilder str = new StringBuilder().append(day_first_prevM).append(
		" 00:00:00");
		day_first_prevM = str.toString(); //上月第一天

		calendar.add(cal.MONTH, 1);
		calendar.set(cal.DATE, 1);
		calendar.add(cal.DATE, -1);
		String day_end_prevM = df.format(calendar.getTime());
		StringBuilder endStr = new StringBuilder().append(day_end_prevM).append(
		" 23:59:59");
		day_end_prevM = endStr.toString();  //上月最后一天

		Map<String, String> map = new HashMap<String, String>();
		map.put("prevMonthFD", day_first_prevM);
		map.put("prevMonthPD", day_end_prevM);
		return map;
	}
	
	
	/**
	 * 获取上周 周一时间，返回 格式yyyy-MM-dd 00:00:00
	 * @author cmt
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Timestamp getPreMonday(){
		Calendar calendar= Calendar.getInstance(); 
		int dayofweek = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayofweek == 1){
			calendar.add(calendar.WEEK_OF_MONTH,-1); 
		}
		
		calendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
		calendar.add(calendar.WEEK_OF_MONTH,-1);
		
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		String mystrdate = myFormat.format(calendar.getTime());
		return Timestamp.valueOf(mystrdate);
	}
	
	/**
	 * 获取上周 周日时间，返回 格式yyyy-MM-dd 23:59:59
	 * @author cmt
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Timestamp getPreSunday(){
		Calendar calendar= Calendar.getInstance(); 
		int dayofweek = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayofweek != 1){
			calendar.add(calendar.WEEK_OF_MONTH,+1); 
		}
		
		calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY); 
		calendar.add(calendar.WEEK_OF_MONTH,-1); 

		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		String mystrdate = myFormat.format(calendar.getTime());
		return Timestamp.valueOf(mystrdate);
	}
	
	public static String getDateyyyyMMddHHmmssSSSStr() {
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Calendar calendar = Calendar.getInstance();
		return myFormat.format(calendar.getTime());
	}
	
	public static Date getDateyyyyMMddHHmmssSSS() throws ParseException {
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Calendar calendar = Calendar.getInstance();
		return myFormat.parse(calendar.getTime().toString());
	}

	
	public static String getDateyyyyMMddHHmmss() {
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		return myFormat.format(calendar.getTime());
	}

	public static String getDateyyyyMMdd() {
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		return myFormat.format(calendar.getTime());
	}
	/**
	 * 获取t天前日期，格式yyyyMM
	 * 
	 * @return
	 */
	public static String getDateyyyyMMdd(int t) {
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		 int day = calendar.get(Calendar.DAY_OF_YEAR);
		 calendar.set(Calendar.DAY_OF_YEAR, day - t);
		return myFormat.format(calendar.getTime());
	}
	
	/**
	 * 获取当前月份，格式yyyyMM
	 * 
	 * @return
	 */
	public static String getMonth() {
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMM");
		return myFormat.format(getDate());
	}
	
	/**
	 * 取当前时间，精确到秒， 格式为 yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getCurrentDateTime(){
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		return myFormat.format(calendar.getTime());
	}
	
	/**
	 * 取当前时间精确到毫秒 ，格式为 yyyy-MM-dd HH:mm:ss:SSS
	 * @return
	 */
	public static String getCurrentDateTimeMS(){
		SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		Calendar calendar = Calendar.getInstance();
		return myFormat.format(calendar.getTime());
	}
	
	/**
	 * 获取昨天天日期（T日） 格式为yyyyMMdd
	 * 
	 * @author: louiszhang
	 * @throws Exception
	 * @date: 2015-11-26下午3:07:23
	 */
	public static String getYesterday() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");// 设置日期格式
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		date = calendar.getTime();
		return df.format(date);
	}
	
    /**
     * 获取某个具体的日期所在月的最早时刻（具体日期所在月的第一天0时0分0秒0毫秒）
     * @param dateSource 某个具体日期
     * @param pattern 日期格式
     * @return  具体日期所在月的第一天0时0分0秒0毫秒
     * @throws ParseException
     */
    public static String getFirstDayOfMonth(String dateSource,String dateSourcePattern,String outPattern) throws ParseException {
    	
    	SimpleDateFormat simpleDateFormatSrc= new SimpleDateFormat(dateSourcePattern, Locale.getDefault());
    	
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(simpleDateFormatSrc.parse(dateSource));
       
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        
        calendar.set(Calendar.HOUR_OF_DAY, 0);//时
        calendar.set(Calendar.MINUTE, 0);//分
        calendar.set(Calendar.SECOND, 0);//秒
        calendar.set(Calendar.MILLISECOND, 0); //毫秒
        
        SimpleDateFormat simpleDateFormatOut= new SimpleDateFormat(outPattern, Locale.getDefault());


        return simpleDateFormatOut.format(calendar.getTime());
    }
    
    /**
     * 获取具体日期所在月的最后一天（具体日期所在月的最后一天23时59分59秒999毫秒）
     * @param dateSource 某个具体日期
     * @param dateSourcePattern 日期格式
     * @param outPattern 方法返回的日期格式
     * @return 具体日期所在月的最后一天23时59分59秒999毫秒
     * @throws ParseException
     */
    public static String getLastDayOfMonth(String dateSource,String dateSourcePattern,String outPattern) throws ParseException {
    	
    	SimpleDateFormat simpleDateFormatSrc= new SimpleDateFormat(dateSourcePattern, Locale.getDefault());
    	
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(simpleDateFormatSrc.parse(dateSource));

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        
        calendar.set(Calendar.HOUR_OF_DAY, 23);//时
        calendar.set(Calendar.MINUTE, 59);//分
        calendar.set(Calendar.SECOND, 59);//秒
        calendar.set(Calendar.MILLISECOND, 999); //毫秒

        SimpleDateFormat simpleDateFormatOut= new SimpleDateFormat(outPattern, Locale.getDefault());
        
        return simpleDateFormatOut.format(calendar.getTime());
    }
    
	/**
	 * 根据所需要的时间格式，获取系统当前时间的字符串表示形式
	 * @param pattern 时间格式
	 * @return 当前系统时间的字符串表示形式
	 */
	public static String getCurrentTimeDescByPattern(String pattern) {
		SimpleDateFormat myFormat = new SimpleDateFormat(pattern, Locale.getDefault());
		Calendar calendar = Calendar.getInstance();
		return myFormat.format(calendar.getTime());
	}
    
    
}
