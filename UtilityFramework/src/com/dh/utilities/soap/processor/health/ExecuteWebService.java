package com.dh.utilities.soap.processor.health;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.dh.utilities.logging.CustomLogger;
public class ExecuteWebService extends CustomLogger{	
	//soapRequest is a string which is used for executing the web service
	@SuppressWarnings("deprecation")
	protected static String executeWebService(String soapRequest,String signature,String endPoint)
	{
		String responseString="";
		URL url=null;
		URLConnection urlC=null;
		DataOutputStream out =null;
		DataInputStream in = null;
		String str="";
		String hostName="";
		try{
			//Read the host information from UtilityConfiguration file
			hostName=(endPoint.equals(""))?UtilityConfiguration.getEndPoint():endPoint;
			
			//Validate the endPoint
			//Validate the endPoint here as well
			if("".equalsIgnoreCase(hostName))
				throw new RuntimeException("Cannot proceed with executing the web service as the end point is empty");
			
			//Validate the soap Request
			if("".equalsIgnoreCase(soapRequest))
				throw new RuntimeException("Cannot proceed with executing the web service as the soap request is invalid");
			
			//Get the header value if header is present then add SOAPHeader else trigger direct soap request
			if(signature==null)
				signature="";
			
			if(!"".equalsIgnoreCase(signature))
			{
				soapRequest = "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
						"<SOAP:Header><wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
	                    signature + "</wsse:Security></SOAP:Header><SOAP:Body>" + soapRequest + "</SOAP:Body></SOAP:Envelope>";
			}			
						
			//Both soap request and end point are valid if the control comes here
			//Initialize url and also urlConnection and all the related parameters. Hard coded few of them as 
			//they are rarely used in our application
			url = new URL(hostName);
			urlC = url.openConnection();
			urlC.setRequestProperty("Content-Type", "text/xml");
			urlC.setDoInput(true);
			urlC.setDoOutput(true);
			urlC.setUseCaches(false);
			out = new DataOutputStream(urlC.getOutputStream());
			out.writeBytes(soapRequest);
			in = new DataInputStream(urlC.getInputStream());
			responseString="";
			while(null!=((str=in.readLine())))
				responseString+= str+"\n";
		}
		catch(Exception e)
		{			
			CustomLogger.logEntry("Exception while executing the soap request with details "+e.getMessage());
		}
		finally{
			hostName=null;
			url=null;
			urlC=null;			
		}
		return responseString;
	}
}
