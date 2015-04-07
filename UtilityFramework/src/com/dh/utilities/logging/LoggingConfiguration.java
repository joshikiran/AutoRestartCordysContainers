package com.dh.utilities.logging;

import java.io.FileInputStream;
import java.util.Properties;
public class LoggingConfiguration {
	protected static boolean isDebugEnabled=false;
	private static Properties properties = new Properties();
	static{
		String currentWorkingDirectory="";
		try{
			currentWorkingDirectory=LoggingConfiguration.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			//Loading the properties from logging configuration property file
			properties.load(new FileInputStream(currentWorkingDirectory+"/LoggingConfiguration.properties"));
			isDebugEnabled=Boolean.valueOf(properties.getProperty("isDebugEnabled"));
		}
		catch(Exception e){
			CustomLogger.logEntry("Exception while fetching the configuration properties of LoggingConfiguration "+e.getMessage());
		}
		finally{
			currentWorkingDirectory=null;
		}
	}
	
}
