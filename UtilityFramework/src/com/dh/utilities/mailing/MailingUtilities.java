package com.dh.utilities.mailing;

import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.dh.utilities.logging.CustomLogger;


public class MailingUtilities extends CustomLogger{
	private static Properties props = System.getProperties();
	private static Session session = null;
	private static MimeMessage message =null ;	
	static{
		try{
		props.put("mail.smtp.starttls.enable", true); // added this line
		props.put("mail.smtp.host", MailConfiguration.mailingHost);
		CustomLogger.debugEntry("Mailing host is "+MailConfiguration.mailingHost);		
		props.put("mail.smtp.user", MailConfiguration.mailUser);
		CustomLogger.debugEntry("Mailing user is "+MailConfiguration.mailUser);
		props.put("mail.smtp.password", MailConfiguration.mailPassword);
		CustomLogger.debugEntry("Mailing Password is "+MailConfiguration.mailPassword);
		props.put("mail.smtp.port", MailConfiguration.mailPort);
		CustomLogger.debugEntry("Mailing port is "+MailConfiguration.mailPort);
		props.put("mail.smtp.timeout",30000);
		props.put("mail.smtp.auth", false);
		
		
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("java.security.egd", "file:///dev/urandom");
		props.put("oracle.net.tns_admin","/opt/oracle/client/home/network/admin");
		
		if(MailConfiguration.mailingTransportType.equalsIgnoreCase("SSL"))
		{
			session = Session.getInstance(props);
		}
		else
		{
			session = Session.getInstance(props,
					  new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(MailConfiguration.mailUser, MailConfiguration.mailPassword);
						}
					  });
			session.setDebug(MailConfiguration.debugEnabled);
		}
		
		CustomLogger.debugEntry("Added session props");
		//session.setDebug(true);
		message =  new MimeMessage(session);
		}
		catch(Exception e){
			CustomLogger.logEntry("MailingUtilities : Static : Application encountered exception while initializing static block with exception details "+e.getMessage());
		}
	}
	public static void main(String args[]){
		sendEmail("Mailing Configuration","<!DOCTYPE html><html> <head>  <title>Sample HTML Page</title> </head> <body>  <table>   <tr><td style='border:1px solid;color:#FF0000;background-color:#FF0000'>A</td><td style='padding-left:15px;'>Aborted</td></tr>   <tr><td style='border:1px solid;color:#FF9933;background-color:#FF9933'>W</td><td style='padding-left:15px;'>Waiting</td></tr>   <tr><td style='border:1px solid;color:#0066FF;background-color:#0066FF'>R</td><td style='padding-left:15px;'>Running</td></tr>  <table> </body></html>");
	}
	public static void sendEmailUsingTSL(String subject,String htmlBodyContent)
	{
		try{
			
		}
		catch(Exception e){
			CustomLogger.logEntry("MailingUtilities : SendEmailUsingTSL : Exception while sending mail using TSL with details "+e.getMessage());
		}
	}
	@SuppressWarnings("static-access")
	public static void sendEmail(String subject,String htmlBodyContent){
		 
		// Create the email addresses involved
		Transport transport =null;
		try {			
			CustomLogger.debugEntry("Adding the from field "+MailConfiguration.mailingFromAddress);
			InternetAddress from = new InternetAddress(MailConfiguration.mailingFromAddress);
			message.setSubject(subject);
			message.setFrom(from);
			CustomLogger.debugEntry("Adding the recipients "+MailConfiguration.mailingToAddress);
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(MailConfiguration.mailingToAddress));
			Multipart multipart = new MimeMultipart("alternative");
			BodyPart messageBodyPart = new MimeBodyPart();
			CustomLogger.debugEntry("Creating message body part for custom mail ");
			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(htmlBodyContent, "text/html");
			CustomLogger.debugEntry("Adding message body part");
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);
			//Check the transport Type and send the mail accordingly
			if(MailConfiguration.mailingTransportType.equalsIgnoreCase("SSL"))
			{
				CustomLogger.debugEntry("Getting the smtp tranport control from the session details ");
				transport = session.getTransport("smtp");
				CustomLogger.debugEntry("MailingUtilities : SendEmail : Sending message to all the recipients");
				transport.send(message,MailConfiguration.mailUser,MailConfiguration.mailPassword);				
			}
			else
			{
				transport.send(message);
			}

		} catch (AddressException e) {
			CustomLogger.logEntry("MailingUtilities : SendEmail : Application encountered address exception with details  "+e.getMessage());
		} catch (MessagingException e) {
			e.printStackTrace();
			CustomLogger.logEntry("MailingUtilities : SendEmail : Application encountered messaging exception with details "+e.getMessage());
		}catch(Exception e)
		{
			CustomLogger.logEntry("MailingUtilities : SendEmail : Application encountered exception with details "+e.getMessage());
		}
		finally{
			CustomLogger.debugEntry("MailingUtilities : Send Email : Closing the connection.");
			try{
				transport.close();
			}
			catch(Exception ex){
				CustomLogger.debugEntry("MailingUtilities : Send Email : Application encountered exception while closing the transport connection.");
			}
		}
	}	
}
