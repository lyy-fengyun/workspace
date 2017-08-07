package com.cmsz.monitor.commons.util;

/**
 *
 * 特殊字符串处理类，加cmup是为了与commons-lang的StringUtils区分
 * @author JinChao
 * 
 * @date 2015年11月20日 上午11:02:07
 * 
 *
 */
public class StringUtilsCmup {
	
	/**
	 * 把null值返回为“”
	 * @param s
	 * @return
	 */
	public static String trimNull2Blank(String s) {
		return (s == null) ? "" :s;
	}
}
