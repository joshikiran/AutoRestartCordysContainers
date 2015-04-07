package com.dh.utilities.soap.GetProcessesForInterface;

import org.w3c.dom.NodeList;

import com.dh.utilities.logging.CustomLogger;

public class GetProcessForInterface extends CustomLogger{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UtilityConfiguration.loadProperties("UtilityConfiguration.properties");
		String branchId="68172948-CECE-11E4-F43E-47DD1872F221";
		String operationId="73f280ab-0f87-11e4-f2e3-12e9f112db05";
		String IDString = GetBindingsFromWebServiceOperation(branchId,operationId);
		System.out.println("--------"+IDString);
		GetProcessInformationUsingIDs(branchId,IDString);
	}
	private static String GetBindingsFromWebServiceOperation(String branchID,String operationID)
	{
		String res="";
		String soapRequest="<GetDocumentByID xmlns=\"http://schemas.cordys.com/xds/1.0\"><Document detail=\"true\" xmlns=\"http://schemas.cordys.com/cws/1.0\"><Branch>"+branchID+"</Branch><ID>"+operationID+"</ID></Document></GetDocumentByID>";
		String samlArt="";
		StringBuffer IDString=new StringBuffer();
		NodeList serviceName;
		try{
			samlArt=GetSAMLArt.getSAMLArt();
			//System.out.println(soapRequest);
			res=ExecuteWebService.executeWebService(soapRequest, samlArt);
			//System.out.println("Response of soap Request is "+res);
			serviceName = XMLUtilities.selectXMLNodes(".//Document//System/Name", XMLUtilities.parseDocument(res));
			CustomLogger.logEntry("APP101 -- ServiceName is "+serviceName);
			//System.out.println("Service Name is "+serviceName.item(0).getTextContent());
			NodeList IDs = XMLUtilities.selectXMLNodes("//uri", XMLUtilities.parseDocument(res));
			
			//Getting the IDs
			for(int i=0;i<IDs.getLength();i++)
			{
				IDString.append("<ID>").append(IDs.item(i).getAttributes().item(0).getNodeValue()).append("</ID>");
			}
			res = IDString.toString();
		}
		catch(Exception e){
			System.out.println("Exception while getting binding ids from web service operation with details ");
			e.printStackTrace();
		}
		
		return res;
	}
	private static String GetProcessInformationUsingIDs(String branchID,String IDs)
	{
		String res="";
		String soapRequest = "<GetDocumentByID xmlns=\"http://schemas.cordys.com/xds/1.0\"><Document detail=\"true\" xmlns=\"http://schemas.cordys.com/cws/1.0\"><Branch>"+branchID+"</Branch>"+IDs+"</Document></GetDocumentByID>";
		String samlArt="";
		String processName="";
		try{
			samlArt = GetSAMLArt.getSAMLArt();
			res = ExecuteWebService.executeWebService(soapRequest, samlArt);
			System.out.println(res);
			NodeList ProcessInformation = XMLUtilities.selectXMLNodes("//ExecuteProcess/processProperties/messages/input/message", XMLUtilities.parseDocument(res));
			System.out.println(ProcessInformation.getLength());
			System.out.println(ProcessInformation.item(0).getAttributes().item(0).getNodeValue());
			CustomLogger.logEntry("APP101 -- ServiceName is "+processName);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}
}
