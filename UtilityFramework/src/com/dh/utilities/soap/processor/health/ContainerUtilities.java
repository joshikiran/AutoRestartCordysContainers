package com.dh.utilities.soap.processor.health;

import org.w3c.dom.Document;

import com.dh.utilities.logging.CustomLogger;

public class ContainerUtilities extends CustomLogger{
	private static String tableRowTag="<tr><td>$ORG</td><td>$SCNAME</td><td style=\"border:1px solid;color:black;background-color:$COLOR\">$STATUS</td></tr>";
	private static String htmlContentForSendMail="<!DOCTYPE html>"+
			"<html>"+
			" <head>"+
			"  <title>Container status of the environment</title>"+
			"  <style>"+
			"	td{"+
			"		padding-left:15px;		"+
			"		font-family: trebuchet ms;"+
			"		font-size: 1em;"+
			"		text-align : center;"+
			"		align-content : center;"+
			"		width : 150px;"+
			"	}"+
			"  </style>"+
			" </head> "+
			" <body>"+
			"  <table border=\"1px solid\">"+
			"$TABLEROW"+
			"  <table>"+
			" </body>"+
			"</html>";
	private static final  String STOPPED_COLOR = "#FF0000";
	private static final  String STARTING_COLOR = "#FF9933";
	private static final  String STARTED_COLOR = "#009933";
	//USE COLOR : FF0000 FOR STOPPED, FF9933 FOR STARTING AND 0066FF FOR STARTED
	private static Document listResponse=null;	
	protected static String getContainerInformation()
	{
		String getContainerListInfoSOAP="";
		String samlArt="";
		try{
			getContainerListInfoSOAP="<List xmlns=\"http://schemas.cordys.com/1.0/monitor\" />";
			
			CustomLogger.debugEntry("Getting SAML Art");
			samlArt=GetSAMLArt.getSAMLArt();
			CustomLogger.debugEntry("Response while fetching SAMLArt is "+samlArt);
			
			String responseString=ExecuteWebService.executeWebService(getContainerListInfoSOAP,samlArt,"");
			CustomLogger.debugEntry("Response while fetching container details are  "+responseString);
			
			if(responseString.contains("workerprocess"))
			{
				CustomLogger.debugEntry("Setting the response String of containers \n");
				setListResponse(responseString);
				return "Success";
			}
			else
			{
				CustomLogger.logEntry("Could not initialize the container information as the response doesn't contain even a singer container information.");
				return "Failed in Initializing the container information";
			}
		}
		catch(Exception ex)
		{
			CustomLogger.logEntry("ContainerUtilities : Exception while getting the containers information");
			return "Failed in retrieving the container information";
		}
		finally{
			
		}
	}
	private static void setListResponse(String listResponseStr)
	{
		listResponse=null;
		try{
			//parsing the listResponse and setting it to a static variable which will then be used to read the status of the container
			listResponse=XMLUtilities.parseDocument(listResponseStr);
			CustomLogger.debugEntry("Writing the response to a static variable with the node infromation as "+listResponse);
		}
		catch(Exception ex)
		{
			listResponse=null;
			CustomLogger.logEntry("ContainerUtilities : Exception while setting the list response with details "+ex.getMessage());
		}
		finally{
			
		}		
	}
	protected static String returnStatusOfContainer(String containerName)
	{
		//Before this function be called one has to initially call setListResponse method to have the list response
		String containerStatus="";
		
		try{
			CustomLogger.debugEntry("Trying to fetch container information with the name as "+containerName);					
			containerStatus = XMLUtilities.getNodeValue(".//workerprocess[name='"+containerName+"']/status/text()", listResponse);
			if(containerStatus==null)
				containerStatus="EXCEPTION";
			if(containerStatus.equalsIgnoreCase(""))
				containerStatus="STOPPED";
			return containerStatus;
		}
		catch(Exception ex)
		{
			containerStatus="Failed to retrieve status";
			CustomLogger.logEntry("ContainerUtilities : Exception while getting the status of the container for "+containerName);
		}
		finally{
			
		}
		return containerStatus;
	}
	protected static String restartContainer(String containerDN)
	{
		//Restart the container
		String operationStatus="";
		String restartSOAP="";
		String responseString="";
		try{			
			restartSOAP="<Restart xmlns=\"http://schemas.cordys.com/1.0/monitor\"><dn>"+containerDN+"</dn></Restart>";
			CustomLogger.debugEntry("Restart request is "+restartSOAP);
			
			responseString=ExecuteWebService.executeWebService(restartSOAP,GetSAMLArt.getSAMLArt(),"");
			CustomLogger.debugEntry("Response from restart container is "+responseString);
			operationStatus="Success in sending a restart command to the monitor";
			CustomLogger.debugEntry("Success in sending a restart command to the monitor");			
						
		}
		catch(Exception e){			
			CustomLogger.logEntry("Exception while restarting the container");
			return "Exception while restarting the container";
		}
		finally{
			
		}
		return operationStatus;
	}
	private static String getOrgNameFromContainerDN(String containerDN)
	{
		String orgName="";
		if("".equals(containerDN))
			return "";
		try{
			int oIndex=containerDN.indexOf("o=");
			if(oIndex>=0)
			{
				orgName = containerDN.substring(oIndex+2);
				orgName = orgName.substring(0, orgName.indexOf(","));
			}
			return orgName;
		}
		catch(Exception e){
			CustomLogger.logEntry("ContainerUtilities : Application encountered exception while fetching organization name from container DN with details "+e.getMessage());			
			return "";
		}
	}
	private static String getSCNameFromContainerDN(String containerDN)
	{
		String scName="";
		if("".equals(containerDN))
			return "";
		try{
			int oIndex=containerDN.indexOf("cn=");
			if(oIndex>=0)
			{
				scName = containerDN.substring(3, containerDN.indexOf(","));
			}
			return scName;
		}
		catch(Exception e){
			CustomLogger.logEntry("ContainerUtilities : Application encountered exception while fetching organization name from container DN with details "+e.getMessage());			
			return "";
		}
	}
	protected static String getContainerStatusForHTML(String propFileName)
	{
		String htmlTableContent=htmlContentForSendMail;
		String tableRow=tableRowTag;
		String orgName="";
		String scName="";
		String status="";
		StringBuffer sbTableRowData = null;
		try{
			//Load the property file if it is not loaded
			if(!UtilityConfiguration.propertiesLoaded)
			{
				CustomLogger.debugEntry("Loading the properties again");
				UtilityConfiguration.loadProperties(propFileName);
			}
			//Call the getContainerInformation again so that List service will get triggered
			status = getContainerInformation();
			CustomLogger.debugEntry("Called the list function to get the details of the containers");
			
			if(!"SUCCESS".equalsIgnoreCase(status))
			{
				CustomLogger.logEntry("Failed in resetting the container configuration.");
				return "FAILURE";
			}
			sbTableRowData = new StringBuffer("");
			//Run through each loop and get the status of the container
			for(String containerDN:UtilityConfiguration.containerArray)
			{
				String tabRow = new String(tableRow);
				CustomLogger.debugEntry("checking the status of container DN "+containerDN);
				
				//Get the orgName
				orgName=getOrgNameFromContainerDN(containerDN);
				tabRow = tabRow.replaceAll("\\$ORG", orgName); //Replacing the orgName with the original orgName
				scName=getSCNameFromContainerDN(containerDN);
				tabRow = tabRow.replaceAll("\\$SCNAME", scName); //Replacing the serviceContainer Name with the original SCName
				
				status=ContainerUtilities.returnStatusOfContainer(containerDN);
				CustomLogger.debugEntry("Status of the container is "+status);
				if("EXCEPTION".equalsIgnoreCase(status))
				{
					CustomLogger.logEntry("Exception while retrieving status of the container, hence will not send a request for restart.");
					return "FAILURE";
				}
				else if("STARTED".equalsIgnoreCase(status))
				{
					tabRow = tabRow.replaceAll("\\$COLOR", STARTED_COLOR);
					tabRow = tabRow.replaceAll("\\$STATUS", "STARTED");
				}
				else if("STOPPED".equalsIgnoreCase(status))
				{
					tabRow = tabRow.replaceAll("\\$COLOR", STOPPED_COLOR);
					tabRow = tabRow.replaceAll("\\$STATUS", "STOPPED");					
				}
				else if("STARTING".equalsIgnoreCase(status)){
					tabRow = tabRow.replaceAll("\\$COLOR", STARTING_COLOR);
					tabRow = tabRow.replaceAll("\\$STATUS", "STARTING");					
				}
				else
				{
					tabRow = tabRow.replaceAll("\\$COLOR", STARTING_COLOR);
					tabRow = tabRow.replaceAll("\\$STATUS", "UNKNOWN");					
				}
				
				//Append to the table Row String buffer
				sbTableRowData.append(tabRow);
			}
			CustomLogger.debugEntry("Table row data is "+sbTableRowData.toString());
			if(sbTableRowData!=null && !sbTableRowData.toString().equals(""))
			{
				//Block to create the html page and respond
				htmlTableContent = htmlTableContent.replaceAll("\\$TABLEROW", sbTableRowData.toString());
				CustomLogger.logEntry("Writing entire HTML as "+htmlTableContent);
				return htmlTableContent;
			}
			else
			{
				CustomLogger.logEntry("Unexpected result as there is no data in table row.");
				return "SUCCESS";
			}
		}
		catch(Exception e)
		{
			CustomLogger.logEntry("ContainerUtilities : getContainerStatusForHTML: Application encountered exception while retrieving HTML content for containers with details "+e.getMessage());
			return "FAILURE";
		}
		finally{
			
		}
	}
	public static String sendMailUsingCordys(String bodyContent)
	{
		String sendMailString="<SendMail xmlns=\"http://schemas.cordys.com/1.0/email\">$TOADDRESS<subject>$SUBJECT</subject><body type='html'>$BODY</body><from><emailAddress>$FROMEMAILADDRESS</emailAddress></from></SendMail>";		
		try{
			//Replace the body Content
			bodyContent = bodyContent.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
			sendMailString = sendMailString.replaceAll("\\$BODY", bodyContent);
			sendMailString = sendMailString.replaceAll("\\$TOADDRESS", UtilityConfiguration.cordysMailToAddressString);
			sendMailString = sendMailString.replaceAll("\\$SUBJECT",UtilityConfiguration.cordysMailSubject);
			sendMailString = sendMailString.replaceAll("\\$FROMEMAILADDRESS", UtilityConfiguration.cordysMailFromAddress);			
			CustomLogger.debugEntry("Email SOAP Envelope formed is "+sendMailString);
			
			//Triggering the email service
			CustomLogger.debugEntry("Getting SAML Art");
			String samlArt=GetSAMLArt.getSAMLArt();
			CustomLogger.debugEntry("Response while fetching SAMLArt is "+samlArt);
			String responseString=ExecuteWebService.executeWebService(sendMailString,samlArt,UtilityConfiguration.getSAMLEndPoint());
			CustomLogger.debugEntry("Response while fetching container details are  "+responseString);			
		}
		catch(Exception e)
		{
			CustomLogger.logEntry("Exception while sending email using cordys with details "+e.getMessage());
			return "FAILURE";
		}
		finally{
			
		}
		return "SUCCESS";
	}
}
