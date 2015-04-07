package com.dh.utilities.soap.GetProcessesForInterface;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.dh.utilities.logging.CustomLogger;


public class UtilityConfiguration extends CustomLogger{
	private static String userID="";
	private static String userPWD="";
	private static String endPoint="";
	private static boolean isDebugEnabled;
	private static String instanceDN="";
	private static String containerDNs="";
	private static String containersToBeRestarted=""; // ALL -- to restart all the configured containers, STOPPED -- To Restart only STOPPED configured containers
	public static ArrayList<String> containerArray=new ArrayList<String>();
	public static boolean propertiesLoaded=false;
	public static String mailSubject="";
	public static String getUserID()
	{
		return userID;
	}
	public static String getUserPWD()
	{
		return userPWD;
	}
	public static String getEndPoint()
	{
		return endPoint;
	}
	public static boolean getIsDebugEnabled()
	{
		return isDebugEnabled;
	}
	public static String getStatusConfigured()
	{
		return containersToBeRestarted;
	}
	public static String getCurrentWorkingDirectory()
	{
		return UtilityConfiguration.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}
	protected static void loadProperties(String propertyFileName)
	{
				propertiesLoaded=false;
		//Block to fetch user details from any properties file
				Properties properties=new Properties();
				String curWorkingDir="";
				String containerDNsplit[]=null;
				String orgSplit[]=null;
				String containerDN="";
				String containerDNString="";
				String orgName="";
				try{
					curWorkingDir = UtilityConfiguration.class.getProtectionDomain().getCodeSource().getLocation().getPath();
					CustomLogger.debugEntry("Current working directory is "+curWorkingDir);

					properties.load(new FileInputStream(curWorkingDir+"/"+propertyFileName));	
					CustomLogger.debugEntry("Properties loaded ");
					
					userID=properties.getProperty("userID");
					userPWD=properties.getProperty("userPWD");
					endPoint=properties.getProperty("endPoint");
					isDebugEnabled=Boolean.valueOf(properties.getProperty("isDebugEnabled"));
					instanceDN=properties.getProperty("instanceDN");
					containerDNs=properties.getProperty("containerDNs");
					mailSubject=properties.getProperty("mailSubject");
					
					CustomLogger.debugEntry("Properties loaded with values as UserID = "+userID+" userPWD = "+userPWD+" endPt is "+endPoint+" isDebugEnabled = "+isDebugEnabled+" instanceDN = "+instanceDN+" containerDNs "+containerDNs);
					
					containersToBeRestarted= properties.getProperty("containersToBeRestarted");
					
					//ContainerDNString should be in this format Org1:ContainerName1$ContainerName2$ContainerName3#Org2:ContainerName1$ContainerName2 etc.,
					//Split the containerDNs string with # which will give you entire organizations containers
					orgSplit = containerDNs.split("#");
					//For each organization form the container DNs
					for (String orgInfo : orgSplit) {
						CustomLogger.debugEntry("Organization information is "+orgInfo);
						orgName=orgInfo.substring(0, orgInfo.indexOf(":"));
						
						CustomLogger.debugEntry("Organization Name is "+orgName);
						containerDNString=orgInfo.substring(orgInfo.indexOf(":")+1);
						
						containerDNsplit=containerDNString.split("\\$");
						
						for(String cn:containerDNsplit){
							cn=cn.substring(0, cn.length());				
							containerDN=cn+",cn=soap nodes,o="+orgName+","+instanceDN;
							CustomLogger.debugEntry("Adding container "+containerDN+" to array");
							containerArray.add(containerDN);
						}
					}
					propertiesLoaded=true;
				}
				catch(Exception ex){
					propertiesLoaded=false;
					CustomLogger.logEntry("Exception while reading the property file with details "+ex.getMessage());
				}
	}
}
