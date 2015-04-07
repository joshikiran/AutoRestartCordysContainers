package com.dh.utilities.soap.processor.health;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.dh.utilities.logging.CustomLogger;


public class UtilityConfiguration extends CustomLogger{
	private static String userID="";
	private static String userPWD="";
	private static String samlEndPoint="";
	private static String endPoint="";
	private static boolean isDebugEnabled;
	private static String instanceDN="";
	private static String containerDNs="";
	private static String containersToBeRestarted=""; // ALL -- to restart all the configured containers, STOPPED -- To Restart only STOPPED configured containers
	public static ArrayList<String> containerArray=new ArrayList<String>();
	public static boolean propertiesLoaded=false;
	public static String mailSubject="";
	public static boolean isMailingEnabled=false;
	public static String mailingType="SMTP"; //Defaulted to SMTP explicitly can be mentioned to CORDYS in which case sendMail will be triggered
	public static String cordysMailToAddressString="";
	public static String cordysMailSubject="";
	public static String cordysMailFromAddress="";
	public static int retryCount=1;
	public static int waitTime=30000;
	
	public static String getUserID()
	{
		return userID;
	}
	public static String getUserPWD()
	{
		return userPWD;
	}
	public static String getSAMLEndPoint()
	{
		return samlEndPoint;
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
					samlEndPoint=properties.getProperty("samlEndPoint");
					endPoint=properties.getProperty("endPoint");
					isDebugEnabled=Boolean.valueOf(properties.getProperty("isDebugEnabled"));
					instanceDN=properties.getProperty("instanceDN");
					containerDNs=properties.getProperty("containerDNs");
					mailSubject=properties.getProperty("mailSubject");
					
					CustomLogger.debugEntry("Properties loaded with values as UserID = "+userID+" userPWD = "+userPWD+"SAMLEndPt is "+samlEndPoint+"  endPt is "+endPoint+" isDebugEnabled = "+isDebugEnabled+" instanceDN = "+instanceDN+" containerDNs "+containerDNs);
					
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
					
					//Read mailing parameters
					isMailingEnabled = Boolean.valueOf(properties.getProperty("mailingEnable"));
					if(isMailingEnabled)
					{
						//Read these properties only if the mailing is enabled for better performance.
						
						mailingType = properties.getProperty("mailingType");
						cordysMailSubject=properties.getProperty("cordysMail.mailSubject");
						cordysMailFromAddress=properties.getProperty("cordysMail.fromAddress");
						cordysMailToAddressString=properties.getProperty("cordysMail.toAddressString");
						CustomLogger.debugEntry("Loading other properties of mailing as isMailingEnabled = "+isMailingEnabled+" mailingType = "+mailingType+" cordysMailSubject = "+cordysMailSubject+
								" cordysMailFromAddress = "+cordysMailFromAddress+" cordysMailToAddressString = "+cordysMailToAddressString);
						//Get the toAddress format used for sendMail		
						cordysMailToAddressString = formCordysToAddressString(cordysMailToAddressString);
						//Getting the retry count for mailing part
						String retCount=properties.getProperty("retryCount");
						if(retCount==null)
							retryCount=0;
						else
							retryCount = Integer.parseInt(retCount);
						
						//Getting the wait time before retry
						String waitTimeStr=properties.getProperty("waitTime");
						if(waitTimeStr==null)
							waitTime=30000;
						else
							waitTime=Integer.parseInt(waitTimeStr);
						
						if(cordysMailFromAddress.equals(""))
						{
							CustomLogger.debugEntry("Exception in forming the email string and hence no email functionality will be given ");
							isMailingEnabled=false;
						}
					}
					propertiesLoaded=true;
				}
				catch(Exception ex){
					propertiesLoaded=false;
					CustomLogger.logEntry("Exception while reading the property file with details "+ex.getMessage());
				}
	}
	private static String formCordysToAddressString(String toAddressString)
	{
		StringBuffer toCompleteAddressString=new StringBuffer("<to>");
		String splitAddress[] = null;
		try{
			splitAddress = toAddressString.split(",");
			for(int i=0;i<splitAddress.length;i++)
			{
				toCompleteAddressString.append("<address><emailAddress>").append(splitAddress[i]).append("</emailAddress></address>");
			}
			toCompleteAddressString.append("</to>");
		}
		catch(Exception e)
		{
			CustomLogger.logEntry("Exception while forming to address from property file with details "+e.getMessage());
			return "";
		}
		return toCompleteAddressString.toString();
	}
}
