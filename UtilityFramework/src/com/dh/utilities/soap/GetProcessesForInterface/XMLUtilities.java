package com.dh.utilities.soap.GetProcessesForInterface;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.dh.utilities.logging.CustomLogger;


public class XMLUtilities extends CustomLogger{
	private static DocumentBuilder builderObj=null;
	private static XPathFactory xpathfactory=null;
	private static XPath xpath=null;
	static{
		try{
			builderObj = getDocumentBuilderObject();
			xpathfactory = XPathFactory.newInstance();
		    xpath = xpathfactory.newXPath();
		}
		catch(Exception e){
			CustomLogger.logEntry("Exception in static block of XMLUtilities class.");
		}
	}
	protected static Document parseDocument(String xmlSource){
		Document doc=null;
		InputSource in = null;
		try{
			CustomLogger.debugEntry("XMLUtilities : parsingDocument with builderObject details as "+builderObj);
			in = new InputSource(new StringReader(xmlSource));
			doc = builderObj.parse(in);
		}
		catch(Exception e){
			CustomLogger.logEntry("Exception while parsing the xml source "+xmlSource+" with the details "+e.getMessage());
		}
		return doc;
	}
	public static DocumentBuilder getDocument()
	{
		return builderObj;
	}
	private static DocumentBuilder getDocumentBuilderObject()
	{
		DocumentBuilder builder=null;
		try{
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(false); // never forget this!
	        builder = factory.newDocumentBuilder();
		}
		catch(Exception e){
			CustomLogger.logEntry("Exception in building document factory");
		}
		return builder;
	}
	public static String getNodeValueByStringResponse(String xpathToBeCompiled,String xmlObjectString)
	{		
		try{
			Document doc = parseDocument(xmlObjectString);
			return getNodeValue(xpathToBeCompiled,doc);
		}
		catch(Exception e)
		{
			CustomLogger.logEntry("Exception while gettingNodeValue by String response : "+e.getMessage());
			return null;
		}
	}
	public static String getNodeValue(String xpathToBeCompiled,Document xmlObject){
		XPathExpression expr=null;
		NodeList nodes = null;
		Object result=null;
		String nodeValue="";
		try{
			CustomLogger.debugEntry("Logging the method parameters "+xpathToBeCompiled+" and the document object as "+xmlObject);
			expr = xpath.compile(xpathToBeCompiled);
			result = expr.evaluate(xmlObject, XPathConstants.NODESET);
			nodes = (NodeList) result;
			CustomLogger.debugEntry("Logging the number of nodes "+nodes.getLength());
			if(nodes.getLength()==0)
				return "";
			nodeValue = nodes.item(0).getNodeValue();
		}
		catch(Exception e){
			CustomLogger.logEntry("Exception in getting matching nodes with details "+e.getMessage());
			return null;
		}
		return nodeValue;
	}
	public static NodeList selectXMLNodes(String xpathToBeCompiled,Document xmlObject){
		XPathExpression expr=null;
		NodeList nodes = null;
		Object result=null;
		try{
			CustomLogger.debugEntry("Logging the method parameters "+xpathToBeCompiled+" and the document Object is "+xmlObject);
			expr = xpath.compile(xpathToBeCompiled);
			result = expr.evaluate(xmlObject, XPathConstants.NODESET);
			nodes = (NodeList) result;
			return nodes;
		}
		catch(Exception e)
		{
			CustomLogger.logEntry("Exception in getting matching nodes with details "+e.getMessage());
			return null;
		}
	}
	public static NodeList selectXMLNodes(String xpathToBeCompiled,String xmlObjectString)
	{
		try{
			Document doc = parseDocument(xmlObjectString);
			return selectXMLNodes(xpathToBeCompiled,doc);
		}
		catch(Exception e){
			CustomLogger.logEntry("Exception in getting matching nodes with details "+e.getMessage());
			return null;
		}
	}
}
