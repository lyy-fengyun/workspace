package com.cmsz.monitor.commons.logging.util;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * 此类主要功能是获取异常发送的行号、类名等信息，一般情况下请不要改动此类
 * @author u
 *
 */
public class LogLocation implements Serializable {
 
    private final static String LINE_SEP = System.getProperty("line.separator");
    private final static int LINE_SEP_LEN = LINE_SEP.length();
 
    /**
     * Caller's line number.
     */
    transient String lineNumber;
    /**
     * Caller's file name.
     */
    transient String fileName;
    /**
     * Caller's fully qualified class name.
     */
    transient String className;
    /**
     * Caller's method name.
     */
    transient String methodName;
    /**
     * All available caller information, in the format
     * <code>fully.qualified.classname.of.caller.methodName(Filename.java:line)</code>
     */
    private String fullInfo;
 
    private static StringWriter sw = new StringWriter();
    private static PrintWriter pw = new PrintWriter(sw);
 
    /**
     * When location information is not available the constant
     * <code>NA</code> is returned. Current value of this string
     * constant is <b>?</b>.
     */
    private final static String NA = "";
 
    private static final long serialVersionUID = -1325822038990805636L;
 
 
    // Check if we are running in IBM's visual age.
    private static boolean inVisualAge = false;
 
    static {
        try {
            Class dummy = Class.forName("com.ibm.uvm.tools.DebugSupport");
            inVisualAge = true;
        } catch (Throwable e) {
            // nothing to do
        }
    }
 
    /**
     * Instantiate location information based on a Throwable. We
     * expect the Throwable <code>t</code>, to be in the format
     * <p/>
     * <pre>
     * java.lang.Throwable
     * ...
     * at org.apache.log4j.PatternLayout.format(PatternLayout.java:413)
     * at org.apache.log4j.FileAppender.doAppend(FileAppender.java:183)
     * at org.apache.log4j.Category.callAppenders(Category.java:131)
     * at org.apache.log4j.Category.log(Category.java:512)
     * at callers.fully.qualified.className.methodName(FileName.java:74)
     * ...
     * </pre>
     * <p/>
     * <p>However, we can also deal with JIT compilers that "lose" the
     * location information, especially between the parentheses.
     */
    public LogLocation(Throwable t, String fqnOfCallingClass) {
 
        if (t == null){
        	 return;
        }
        String s;
        // Protect against multiple access to sw.
        synchronized (sw) {
            t.printStackTrace(pw);
            s = sw.toString();
            sw.getBuffer().setLength(0);
        }
        //System.out.println("s is ["+s+"].");
        int ibegin, iend;
 
        // Given the current structure of the package, the line
        // containing "org.apache.log4j.Category." should be printed just
        // before the caller.
 
        // This method of searching may not be fastest but it's safer
        // than counting the stack depth which is not guaranteed to be
        // constant across JVM implementations.
        ibegin = s.indexOf(fqnOfCallingClass);
        if (ibegin == -1){
        	 return;
        	 
        }
        ibegin = s.indexOf(LINE_SEP, ibegin);
        if (ibegin == -1){
        	return;
        }
        ibegin += LINE_SEP_LEN;
 
        // determine end of line
        iend = s.lastIndexOf(LINE_SEP, ibegin);
        if (iend == -1){
        	 return;
        }
           
 
        // VA has a different stack trace format which doesn't
        // need to skip the inital 'at'
        if (!inVisualAge) {
            // back up to first blank character
            ibegin = s.lastIndexOf("at ", iend);
            if (ibegin == -1){
            	 return;
            }
            // Add 3 to skip "at ";
            ibegin += 3;
        }
        // everything between is the requested stack item
        this.fullInfo = s.substring(ibegin, iend);
    }
 
    /**
     * Return the fully qualified class name of the caller making the
     * logging request.
     */
    public String getClassName() {
        if (fullInfo == null) return NA;
        if (className == null) {
            // Starting the search from '(' is safer because there is
            // potentially a dot between the parentheses.
            int iend = fullInfo.lastIndexOf('(');
            if (iend == -1)
                className = NA;
            else {
                iend = fullInfo.lastIndexOf('.', iend);
 
                // This is because a stack trace in VisualAge looks like:
 
                //java.lang.RuntimeException
                //  java.lang.Throwable()
                //  java.lang.Exception()
                //  java.lang.RuntimeException()
                //  void test.test.B.print()
                //  void test.test.A.printIndirect()
                //  void test.test.Run.main(java.lang.String [])
                int ibegin = 0;
                if (inVisualAge) {
                    ibegin = fullInfo.lastIndexOf(' ', iend) + 1;
                }
 
                if (iend == -1)
                    className = NA;
                else
                    className = this.fullInfo.substring(ibegin, iend);
            }
        }
        return className;
    }
 
    /**
     * Return the file name of the caller.
     * <p/>
     * <p>This information is not always available.
     */
    public String getFileName() {
        if (fullInfo == null) return NA;
 
        if (fileName == null) {
            int iend = fullInfo.lastIndexOf(':');
            if (iend == -1)
                fileName = NA;
            else {
                int ibegin = fullInfo.lastIndexOf('(', iend - 1);
                fileName = this.fullInfo.substring(ibegin + 1, iend);
            }
        }
        return fileName;
    }
 
    /**
     * Returns the line number of the caller.
     * <p/>
     * <p>This information is not always available.
     */
    public String getLineNumber() {
        if (fullInfo == null) return NA;
 
        if (lineNumber == null) {
 
            int iend = fullInfo.lastIndexOf(')');
            int ibegin = fullInfo.lastIndexOf(':', iend - 1);
            if (ibegin == -1)
                lineNumber = NA;
            else
                lineNumber = this.fullInfo.substring(ibegin + 1, iend);
        }
        return lineNumber;
    }
 
    /**
     * Returns the method name of the caller.
     */
    public String getMethodName() {
        if (fullInfo == null) return NA;
        if (methodName == null) {
            int iend = fullInfo.lastIndexOf('(');
            int ibegin = fullInfo.lastIndexOf('.', iend);
            if (ibegin == -1)
                methodName = NA;
            else
                methodName = this.fullInfo.substring(ibegin + 1, iend);
        }
        return methodName;
    }
}