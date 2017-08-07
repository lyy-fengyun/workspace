package com.cmsz.monitor.commons.logging.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.cmsz.monitor.commons.util.DateUtil;
import com.cmsz.monitor.commons.util.PropertiesUtil;
/**
 * 日志基类
 *
 * @author lijh,Jinchao
 *
 */
public class BaseLogger {
    private static final String DEFAULT_HOSTIP = "0.0.0.0";
    //把VariableMap与线程绑定，当统一由dispatcher通用dubbo接口发起的调用，
    //可以不用传map下来，日志框架也会记录业务线等信息（从线程池中获取map）
    private static ThreadLocal<Map<String, String>> threadVariableMap = new ThreadLocal<>();
    public static void setThreadVariableMap(ThreadLocal<Map<String, String>> threadVariableMap) {
        BaseLogger.threadVariableMap = threadVariableMap;
    }
    public static ThreadLocal<Map<String, String>> getThreadVariableMap() {
        return threadVariableMap;
    }
    protected BaseLogger(Class<?> clazz) {
        this.initSystemParams(clazz);
    }

    // 日志级别
    public static final String LOG_LEVEL_DEBUG = "DEBUG";
    public static final String LOG_LEVEL_INFO = "INFO";
    public static final String LOG_LEVEL_WARNING = "WARNING";
    public static final String LOG_LEVEL_ERROR = "ERROR";

    // ${主机名}_${IP}_${PORT}	如： upayappa_172.16.59.8_8080，系统日志需要
    private String appId;

    // 子系统名称,“TXN”或“SETTLE”分别表示实时交易或清结算,告警日志需要
    private String subSystem ="jvm_monitor";

    // 模块名称
    private String module= "upayMonitor";
    // 实例名称，值：${IP}_${PORT} ,告警日志需要
    private String instance;
    // 主机IP
    private String hostIp;
    // 主机名
    private String hostName;
    // 类名
    private String classname;


    /**
     * 格式化报文日志
     *
     * @param p_loglevel
     * @param msgContext
     * @return
     */
    protected String convMessageLog(final String msgContext) {
        StringBuilder sb_tmp = new StringBuilder();
        sb_tmp.append(DateUtil.getCurrentTimeDescByPattern("yyyyMMddHHmmssSSS"));
        sb_tmp.append("#");
        sb_tmp.append(msgContext);
        return sb_tmp.toString();

    }

    /**
     * 日志后半部分,日志描述
     * @param msgContext
     * @return
     */
    protected String getFooterMessage(String msgContext) {
        StringBuilder sb_tmp = new StringBuilder();
        sb_tmp.append(msgContext);
        return sb_tmp.toString();
    }

    /**
     * 日志系统属性初始化, 设置类名，主机名 ip 端口号等 信息
     *
     * @param clazz
     */
    protected void initSystemParams(final Class<?> clazz) {
        // 设置包名+类名
        this.setClassname(clazz.getName());

        // 设置主机IP和主机名
        try {
            //hostIp = InetAddress.getLocalHost().getHostAddress();
            //应用部署ip，统一从资源文件取；
            hostIp = PropertiesUtil.getPropertyValue("app.ip");
            hostName = InetAddress.getLocalHost().getHostName();
            if (null == hostIp) {
                this.setHostIp(DEFAULT_HOSTIP);
            } else {
                this.setHostIp(hostIp);
            }
            if (null == hostName) {
                this.setHostName("hostName");
            } else {
                this.setHostName(hostName);
            }

            //端口
            String port =PropertiesUtil.getPropertyValue("app.port");

            instance =hostIp+"_"+port;

            //${主机名}_${IP}_${PORT}
            appId=hostName+"_"+hostIp+"_"+port;

        } catch (UnknownHostException e) {
            System.out.println("获取主机IP或主机名失败!" + e.getMessage());
        }
    }

    /**
     * 猜测： 函数作用是处理utf字符串
     * convert
     *
     * @author liyayong
     * @param utfString
     * @return
     * @since JDK 1.7
     */
    public String convert(String utfString) {
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int pos = 0;

        while ((i = utfString.indexOf("\\u", pos)) != -1) {
            sb.append(utfString.substring(pos, i));
            if (i + 5 < utfString.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(
                        utfString.substring(i + 2, i + 6), 16));
            }
        }

        return sb.toString();
    }


    public String getSubSystem() {
        return StringUtils.isBlank(subSystem) ? "" : subSystem;
    }

    public void setSubSystem(String subSystem) {
        this.subSystem = subSystem;
    }


    public String getModule() {
        return StringUtils.isBlank(module) ? "" : module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getInstance() {
        return StringUtils.isBlank(instance) ? "" : instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getAppId() {
        return appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }


}
