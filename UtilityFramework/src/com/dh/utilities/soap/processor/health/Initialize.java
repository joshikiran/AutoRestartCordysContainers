package com.dh.utilities.soap.processor.health;

import java.util.ArrayList;

import com.dh.utilities.logging.CustomLogger;
import com.dh.utilities.mailing.MailingUtilities;


public class Initialize extends CustomLogger{

	public static void main(String[] args) throws InterruptedException {
		CustomLogger.debugEntry("Logging the number of parameters "+args.length);
		if(args.length>0){
			CustomLogger.debugEntry("Logging the parameters "+args[0]);
			UtilityConfiguration.loadProperties(args[0]);
		}
		restartConfigContainers();
		CustomLogger.debugEntry("Application successful in sending restart command.");		
		//Block for sending email only if mailing is enabled
		if(UtilityConfiguration.isMailingEnabled)
		{
			//Getting the retryCount
			int retryCount = UtilityConfiguration.retryCount;
			int numberOfRetrys=0;
			int waitTime = UtilityConfiguration.waitTime;
			do{
				CustomLogger.debugEntry("Notification module for "+numberOfRetrys+"th iteration.");
				//Get the html content		
				String htmlContent = ContainerUtilities.getContainerStatusForHTML(args.length>0?args[0]:"");
				//sending email with this html Content
				CustomLogger.debugEntry("Responded with the details "+htmlContent+" now sending the mail of the same");
				//Get the email type if it is SMTP then trigger using SMTP else use SendMail of cordys
				if(UtilityConfiguration.mailingType.equalsIgnoreCase("SMTP"))
				{
					MailingUtilities.sendEmail(UtilityConfiguration.mailSubject, htmlContent);
					CustomLogger.debugEntry("Application had triggered the send mail using SMTP way.");			
				}
				else
				{
					//Block to trigger send mail of cordys
					ContainerUtilities.sendMailUsingCordys(htmlContent);
					CustomLogger.debugEntry("Application had triggered the send mail using Cordys.");
				}
				//break the loop if the mail content doesn't have starting or stopped containers
				if(!htmlContent.contains("STARTING</td>") && !htmlContent.contains("STOPPED</td>"))
				{
					CustomLogger.debugEntry("Since the job is successfully done, going with retry module doesn't make any sense. Hence the job exits.");
					break;
				}
				//waiting compulsory???
				boolean doIHaveToWait = (numberOfRetrys==retryCount)?false:true;
				if(doIHaveToWait)
				{
					CustomLogger.debugEntry("Waiting for "+waitTime+" milliseconds. You can change this from configuration.");
					Thread.sleep(waitTime);
				}
				else
					CustomLogger.debugEntry("Waiting is not needed at "+numberOfRetrys+"th iteration.");
			}while(numberOfRetrys++<retryCount);
		}
	}
	public static void restartConfigContainers()
	{
		//Trigger the list service information to get the status of the containers
		CustomLogger.debugEntry("Calling Container Utilities getContainerConfiguration");
		String status = ContainerUtilities.getContainerInformation();
		CustomLogger.debugEntry("Response from getContainerInformation : "+status);
		String configuredStatus=UtilityConfiguration.getStatusConfigured();
		//Get the list of the containers
		ArrayList<String> containers = UtilityConfiguration.containerArray; 
		for(String containerDN:containers)
		{
			CustomLogger.debugEntry("checking the status of container DN "+containerDN);
			status=ContainerUtilities.returnStatusOfContainer(containerDN);
			CustomLogger.debugEntry("Status of the container is "+status);
			if("EXCEPTION".equalsIgnoreCase(status))
			{
				CustomLogger.logEntry("Exception while retrieving status of the container, hence will not send a request for restart.");
				return;
			}
			if(status.equalsIgnoreCase(configuredStatus) || "ALL".equalsIgnoreCase(configuredStatus))
			{
				ContainerUtilities.restartContainer(containerDN);
				CustomLogger.logEntry("Sent a request for restart container of "+containerDN);
			}
		}
	}
}
