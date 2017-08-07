package com.cmsz.monitor.commons.util;

/**
 * 字符串或数字补齐操作等
 * 
 *
 * @author yaoQingCan
 * @version 创建时间：2016年1月28日 下午3:19:21
 */
public class DataCompleteUtil {
	
	
	/**
	 * ##
	 * 
	 * @Title: turnNumberToXLengthString
	 * @Description: 将输入数字整理成长度为Length的字符串，数字右对齐，左边高位用‘0’补齐
	 * @param number
	 *            被整形的数字
	 * @param Length
	 *            整形后的字符长度
	 * @return：String 整形后的字符串
	 * @Date:Dec 21, 2015 4:36:39 PM
	 * @Author:LeucotheaShi
	 */
	public static String turnNumberToXLengthString(int number, int Length) {

		String resStr = number + "";
		for (int j = 0; j < Length - (number + "").length(); j++) {
			resStr = "0" + resStr;
		}// for

		return resStr;
	}// getXLengthString

	/**
	 * ##
	 * 
	 * @Title: getXLengthString
	 * @Description: 将输入数字整理成长度为Length的字符串，数字右对齐，左边高位用‘0’补齐
	 * @param number
	 *            被整形的数字
	 * @param Length
	 *            整形后的字符长度
	 * @return：String 整形后的字符串
	 * @Date:Dec 21, 2015 4:36:39 PM
	 * @Author:LeucotheaShi
	 */
	public static String turnStringToXLengthString(String str, int Length) {

		String resStr = str;
		for (int j = 0; j < Length - str.length(); j++) {
			resStr = "0" + resStr;
		}// for

		return resStr;
	}// getXLengthString

}
