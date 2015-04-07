package com.dh.utilities.mailing;

import java.io.FileInputStream;
import java.util.Properties;

import com.dh.utilities.logging.CustomLogger;
import com.dh.utilities.logging.LoggingConfiguration;

public class MailConfiguration extends CustomLogger{
	protected static String mailingHost="";
	protected static String mailUser="";
	protected static String mailPassword="";
	protected static String mailPort="";
	protected static String mailingFromAddress="";
	protected static String mailingToAddress="";//Split by , so that it can be sent to many people
	protected static String mailingTransportType=""; //Reading the transport type of the mail, by default it would be SSL
	protected static boolean debugEnabled=false;
	private static Properties properties = new Properties();
	static{
		String currentWorkingDirectory="";
		try{
			currentWorkingDirectory=LoggingConfiguration.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			//Loading the properties from logging configuration property file
			properties.load(new FileInputStream(currentWorkingDirectory+"/MailingConfiguration.properties"));
			mailingHost=properties.getProperty("mail.smtp.host");
			mailUser=properties.getProperty("mail.smtp.user");
			mailPassword=properties.getProperty("mail.smtp.password");
			mailPort=properties.getProperty("mail.smtp.port");
			mailingFromAddress=properties.getProperty("fromAddress");
			mailingToAddress=properties.getProperty("toAddressString");
			mailingTransportType=properties.getProperty("mail.transportType");
			String debugProp =properties.getProperty("mail.debugEnabled");
			if(debugProp==null || debugProp.equals(""))
				debugEnabled=false;
			else
				debugEnabled=Boolean.valueOf(debugProp);
			
			if(mailingTransportType==null || "".equalsIgnoreCase(mailingTransportType))
				mailingTransportType="SSL";
			
			if(mailingHost==null || mailUser==null ||mailPassword==null ||mailPort == null|| mailPort == null|| mailingFromAddress == null || mailingToAddress == null)
				CustomLogger.logEntry("All the properties of mailing configuration are not loaded successfully.");
		}
		catch(Exception e){
			CustomLogger.logEntry("Exception while fetching the configuration properties of LoggingConfiguration "+e.getMessage());
		}
		finally{
			currentWorkingDirectory=null;
		}
	}
}
