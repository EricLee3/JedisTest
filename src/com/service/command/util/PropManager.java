package com.service.command.util;

import java.io.FileInputStream;
import java.util.Properties;

import com.service.command.log.Logger;

public class PropManager {
	
	private Properties prop;
	private static PropManager propManager; 
	private PropManager() {}
	public static PropManager getInstances() {
		if(propManager == null) {
			propManager = new PropManager();
		}
		return propManager;
	}
	public Properties getProp(String propName) {
		try {
			prop = new Properties();
//			FileInputStream fis = new FileInputStream("D:/eclipseKepler/projects/API/props/"+propName+".properties");
			FileInputStream fis = new FileInputStream("/export/home/api/props/"+propName+".properties");
			prop.load(fis);
			
		} catch (Exception e) {
			Logger.error(e);
		}
		return prop;
	}
}
