/**
 * Project Name:logbackDemo
 * File Name:LogbackDemo.java
 * Package Name:com.lyy.logbackdemo
 * Date:2017年5月11日下午9:47:01
 * Copyright (c) 2017, fengyun.lyy@foxmail.com All Rights Reserved.
 *
*/

package com.cmsz.monitor.logbackdemo;


import java.util.HashMap;

import com.cmsz.monitor.commons.logging.alarm.AlarmLogHandler;
import com.cmsz.monitor.commons.logging.system.SystemLogHandler;;

/**
 * ClassName:LogbackDemo <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:   TODO ADD REASON. <br/>
 * Date:     2017年5月11日 下午9:47:01 <br/>
 * @author   lenovo
 * @version
 * @since    JDK 1.7
 * @see
 */
public class LogbackDemo {


    public static final SystemLogHandler sysLogger = SystemLogHandler.getLogger(LogbackDemo.class);
    public static final AlarmLogHandler alarmLogger = AlarmLogHandler.getLogger(LogbackDemo.class);

    /**
     * 测试logback的一些用法
     * main
     *
     * @author lenovo
     * @param args
     * @since JDK 1.7
     */
    public void demo() {
        sysLogger.info("使用了Systermlog init log ");
        sysLogger.warn("warn message");
        sysLogger.error("error message ");

        alarmLogger.warn("alarm log warn");
        alarmLogger.error("this is a error message");

    }

    public static void main(String[] args) {
        LogbackDemo logbackDemo = new LogbackDemo();
        logbackDemo.demo();
    }
}
