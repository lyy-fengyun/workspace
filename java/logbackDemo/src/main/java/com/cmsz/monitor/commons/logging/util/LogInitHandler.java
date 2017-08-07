package com.cmsz.monitor.commons.logging.util;

import java.io.File;

import org.slf4j.LoggerFactory;

import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;


/**
 * 系统日志组件初始化
 * @author lijh,Jinchao
 *
 */
public class LogInitHandler {
    public static final String logbackConfig="/logback.xml";

    /**
     * 初始化logback配置(日志输出目录等).
     */
    public static void initLogback(String logRootPath) {
        try {
            File logbackFile = new File(logRootPath+logbackConfig);
            if(!logbackFile.exists() || !logbackFile.isFile() || !logbackFile.canRead()){
                System.out.println("###初始化日志文件失败！日志配置文件不存在或者不可读取!");
                return;
            }

            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            configurator.doConfigure(logbackFile);
            StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
        } catch (Exception e) {
            System.out.println("###初始化日志文件失败！日志目录和文件不能创建成功!"+e.getMessage());
        }
    }
}
