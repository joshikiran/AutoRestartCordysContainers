package com.dh.utilities.soap.processor.health;

import com.dh.utilities.logging.CustomLogger;


public class GetSAMLArt extends CustomLogger{
	private static String samlArt="";
	private static boolean validateSAMLArt()
	{
		boolean isSAMLArtValid=false;
		String getUserDetailsSOAP="";
		String userDetailsResp="";
		try{
			getUserDetailsSOAP="<GetUserDetails xmlns=\"http://schemas.cordys.com/notification/workflow/1.0\"></GetUserDetails>";
			userDetailsResp=ExecuteWebService.executeWebService(getUserDetailsSOAP,samlArt,"");
			if(userDetailsResp.contains("GetUserDetailsResponse"))
				isSAMLArtValid=true;
		}
		catch(Exception ex)
		{
			CustomLogger.logEntry("Validating SAML Art : Invalid SAML Art");
			return false;
		}
		return isSAMLArtValid;
		
	}
	protected static String getSAMLArt()
	{
		String userName="";
		String password="";
		String samlArtStr="";
		String samlEndPoint="";
		int startSignatureIndex;
		int endSignatureIndex ;
		try{
			//Get the necessarey details from UtilityConfiguration class
			userName=UtilityConfiguration.getUserID();
			password=UtilityConfiguration.getUserPWD();
			samlEndPoint=UtilityConfiguration.getSAMLEndPoint();
			if(samlEndPoint==null)
				samlEndPoint="";
			CustomLogger.debugEntry("User Name is "+userName);
			CustomLogger.debugEntry("User Password is "+password);
			
			if(validateSAMLArt())
			{
				CustomLogger.debugEntry("SAML Art is already present with details "+samlArt+" and hence no trigger to get the SAMLArt again.");
				return samlArt;
			}
			String requestString =
	                "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
	                "<SOAP:Header>" +
	                "<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
	                "<wsse:UsernameToken xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
	                "<wsse:Username>" + userName + "</wsse:Username>" +
	                "<wsse:Password>" + password + "</wsse:Password>" +
	                "</wsse:UsernameToken>" +
	                "</wsse:Security>" +
	                "</SOAP:Header>" +
	                "<SOAP:Body>" +
	                "<samlp:Request xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" MajorVersion=\"1\" MinorVersion=\"1\">" +
	                "<samlp:AuthenticationQuery>" +
	                "<saml:Subject xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\">" +
	                "<saml:NameIdentifier Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">" + userName + "</saml:NameIdentifier>" +
	                "</saml:Subject>" +
	                "</samlp:AuthenticationQuery>" +
	                "</samlp:Request>" +
	                "</SOAP:Body>" +
	                "</SOAP:Envelope>";
		CustomLogger.debugEntry("SAML Art is not present hence the application is fetching SAMLArt again from the environment.");
		String responseString = ExecuteWebService.executeWebService(requestString,"",samlEndPoint);
		
		CustomLogger.debugEntry("SAML response from the environment is "+responseString);
		startSignatureIndex = responseString.indexOf("<Signature");
        endSignatureIndex = responseString.indexOf("</samlp:AssertionArtifact>");
        samlArtStr = responseString.substring(startSignatureIndex , endSignatureIndex+26);
        
        CustomLogger.debugEntry("SAML Art present in the response is "+samlArtStr);
        //Assign the samlArt variable to the static variable so that it need not fire again
        samlArt=samlArtStr;
		}
		catch(Exception ex)
		{
			CustomLogger.logEntry("Exception while executing the soap request with details "+ex.getMessage());
			samlArt="";
			return "";
		}
		finally{
			
		}
		return samlArt;
	}
}
