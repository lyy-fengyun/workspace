package com.cmsz.monitor.commons.util;

import java.util.Map;

/**
 * properties文件工具类
 * 注意：使用此类，属性文件必须由Spring管理才可以直接获取到属性值
 * @author JinChao
 *
 */
public class PropertiesUtil {
	public static Map<String, String> propertiesMap = null;
	/**
	 * 
	 * 
	 * 取得属性值
	 * @param perpertyName 属性名
	 * @return
	 */
	public static String getPropertyValue(String perpertyName){
		if (propertiesMap==null) {
			return "属性文件未初始化成功！";
		}else {
			return propertiesMap.get(perpertyName);
		}
	}
}
