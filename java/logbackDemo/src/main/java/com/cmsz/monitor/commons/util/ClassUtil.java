package com.cmsz.monitor.commons.util;

import com.cmsz.monitor.commons.logging.system.SystemLogHandler;

/**
 *	类相关操作工具类
 *
 * @author JinChao
 * 
 * @date 2015年12月1日 下午3:44:05
 *
 */
public class ClassUtil {
	private static SystemLogHandler systemLogHandler = SystemLogHandler.getLogger(ClassUtil.class);

	/**
	 * 根据Class从包名中提取系统简称
	 * @param clazz
	 * @return
	 */
	public static String getSystemNameByClass(Class<?> clazz) {
		String packageName = clazz.getPackage().getName();
		String systemName = "";
		try{
			String[] packages = packageName.split("\\.");
			systemName = packages[3];
		}catch(Exception e){
			String message = "从类中提取系统简称出现异常，请检查你的包命名是否符合规范[com.cmsz.cmup.子系统简称.xxx]";
			systemLogHandler.error(message, e);
		}
		return systemName;
	}
}
