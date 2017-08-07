package com.w3cschool.ide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App
{
	private static final Logger LOGGER =  LoggerFactory.getLogger(App.class);

	public static void main( String[] args )
    {
		App objApp = new App();
		objApp.runMe("w3cschool");
        System.out.println( "Hello World!" );
    }

	public void runMe(String message) {
//		if(LOGGER.isDebugEnabled()){
//			LOGGER.debug("This is debug:"+message);
//		}
//
//		if (LOGGER.isInfoEnabled()){
//			LOGGER.info("This is info:"+ message);
//		}

		LOGGER.warn("this is warn:"+message);
		LOGGER.error("this is error:"+message);
	}




}
