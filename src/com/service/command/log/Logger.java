package com.service.command.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Log Class
 * 
 * @author sylyu
 * 
 */
public class Logger {

	public static final Log LOG_NOHUP = LogFactory.getLog("nohup");
	
	
	/**
	 * Exception printStackTrace 를 로그에 찍는다
	 * 
	 * @param e
	 * @throws IOException 
	 */
	public static void error(Exception e){
		
		ByteArrayOutputStream baos = null;
			
		try {
			if (e != null) {
				baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				String eMsg = baos.toString();
				LOG_NOHUP.error(eMsg);
			}
			baos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Message를 로그에 찍는다
	 * @param msg
	 */
	public static void debug(String msg) {
		if (!StringUtils.defaultString(msg,"").equals("")) {
			LOG_NOHUP.debug(msg);
		}
	}
	
}
