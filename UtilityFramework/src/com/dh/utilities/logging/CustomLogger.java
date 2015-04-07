package com.dh.utilities.logging;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CustomLogger {
	private static Logger logger = Logger.getLogger("CustomLogger");
	private static FileHandler fh=null;	
	private static void createFileHandler()
	{
		try{
			DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
	        Calendar cal = Calendar.getInstance();          
	    	String logFile="com.logging.utilities.CustomLogger_"+dateFormat.format(cal.getTime())+".log"; 
	    	//FileHandler 	    	
	    	File f = new File("logs");
	    	f.mkdir();
	    	fh = new FileHandler("logs"+"/"+logFile,true);        
	    	logger.addHandler(fh);
	    	SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally{
		//	fh.close();
		}
		
	}
	protected static void debugEntry(String message)
	{
		try{
			if(fh==null)
				createFileHandler();
			if(LoggingConfiguration.isDebugEnabled)
				logger.log(Level.SEVERE,message);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally{
		//	fh.close();
		}
	}
	protected static void logEntry(String message)
	{
		try{						
			if(fh==null)
				createFileHandler();
	        logger.log(Level.SEVERE, message);	        
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
}
