package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbXMLNSC;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;


public class TO_SYSTEM4_JavaCompute extends MbJavaComputeNode {
	
	public MbMessage createBaseMessage(MbMessage inMessage, String operationType) throws MbException, DecoderException{
		
		MbMessage outMessage = new MbMessage();
		
		MbElement outRoot = outMessage.getRootElement();
		MbElement mqmd = outRoot.createElementAsLastChild("MQHMD");
		char[] correlId = "414D512045534244455630312E514D20B98A765705F2102F".toCharArray();
		
		byte[] array = Hex.decodeHex(correlId);
		mqmd.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Format", "MQHRF2");
		mqmd.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "CorrelId", array);
		
		MbElement xmlBody = outRoot.createElementAsLastChild(MbXMLNSC.PARSER_NAME);
		MbElement rf2 = xmlBody.createElementBefore("MQHRF2");
		//MbElement xmlBody = outRoot.getLastChild();
		//MbElement rf2 = outRoot.createElementAsLastChild("MQRFH2");
		
		
		String digestStr = (String)inMessage.evaluateXPath("string(//*[local-name()='Digest'])");
		MbElement rf2Format = rf2.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Format", ""); 
		MbElement rf2Usr = rf2.createElementAsLastChild(MbElement.TYPE_NAME, "usr", null); 
		MbElement rfDigest = rf2Usr.createElementAsLastChild(MbElement.TYPE_NAME, "Digest", digestStr); 

		MbElement xmlDecl =	 xmlBody.createElementAsLastChild(MbXMLNSC.XML_DECLARATION);  

		xmlDecl.createElementAsFirstChild(MbXMLNSC.ATTRIBUTE, "version", "1.0"); 
		xmlDecl.createElementAsFirstChild(MbXMLNSC.ATTRIBUTE, "encoding", "UTF-8");  			
	
		String versionStr = (String)inMessage.evaluateXPath("string(//*[local-name()='Version'])");
		
		String operationNum = null;
		if(operationType.equalsIgnoreCase("Debit")) 
			operationNum = (String)inMessage.evaluateXPath("string(//*[local-name()='Debit'])");
		else
			operationNum = (String)inMessage.evaluateXPath("string(//*[local-name()='Credit'])");
				
		String messageIdStr = (String)inMessage.evaluateXPath("string(//*[local-name()='MessageId'])");
		String sourceSystemStr = (String)inMessage.evaluateXPath("string(//*[local-name()='Source'])");
		String orderNoStr = (String)inMessage.evaluateXPath("string(//*[local-name()='OrderNo'])");
		String objectLastChangedStr = (String)inMessage.evaluateXPath("string(//*[local-name()='ValueDate'])");
		String amountdStr = (String)inMessage.evaluateXPath("string(//*[local-name()='RubAmt'])");
							
		MbElement payment = xmlBody.createElementAsLastChild(MbXMLNSC.FOLDER, "Payment", null);
		
		MbElement msgDecl = payment.createElementAsLastChild(MbXMLNSC.NAMESPACE_DECLARATION, "msg", "http://message.system4.com");
		MbElement hdDecl = payment.createElementAsLastChild(MbXMLNSC.NAMESPACE_DECLARATION, "hd", "http://header.system4.com");
		MbElement bdDecl = payment.createElementAsLastChild(MbXMLNSC.NAMESPACE_DECLARATION, "bd", "http://body.system4.com");
		msgDecl.setNamespace("xmlns");		
		bdDecl.setNamespace("xmlns");		
		hdDecl.setNamespace("xmlns");		
		payment.setNamespace("http://message.system4.com");
		
		MbElement header = payment.createElementAsFirstChild(MbXMLNSC.FOLDER, "Header", null);
		header.setNamespace("http://header.system4.com");
		MbElement messageId = header.createElementAsLastChild(MbXMLNSC.FIELD, "MessageId", messageIdStr);
		messageId.setNamespace("http://header.system4.com");
		MbElement type = header.createElementAsLastChild(MbXMLNSC.FIELD, "Type", operationType);
		type.setNamespace("http://header.system4.com");
		MbElement version = header.createElementAsLastChild(MbXMLNSC.FIELD, "Version", versionStr);
		version.setNamespace("http://header.system4.com");
		MbElement sourceSystem = header.createElementAsLastChild(MbXMLNSC.FIELD, "SourceSystem", sourceSystemStr);
		sourceSystem.setNamespace("http://header.system4.com");
		MbElement body = payment.createElementAsLastChild(MbXMLNSC.FOLDER, "Body", null);
		body.setNamespace("http://message.system4.com");
		MbElement orderNo = body.createElementAsLastChild(MbXMLNSC.FIELD, "OrderNo", orderNoStr);
		orderNo.setNamespace("http://body.system4.com");
		MbElement orderDate = body.createElementAsLastChild(MbXMLNSC.FIELD, "OrderDate", parceYearDateMonth(objectLastChangedStr));
		orderDate.setNamespace("http://body.system4.com");
		MbElement creditAccount = body.createElementAsLastChild(MbXMLNSC.FIELD, "Account", operationNum);
		creditAccount.setNamespace("http://body.system4.com");
		MbElement amount = body.createElementAsLastChild(MbXMLNSC.FIELD, "Amount", amountdStr);
		amount.setNamespace("http://body.system4.com");
		return outMessage;
	}

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssemblyCredit = null;
		MbMessageAssembly outAssemblyDebit = null;
					
		try {
			
			
			MbMessage creditMessage = createBaseMessage(inMessage, "Credit");
			MbMessage debitMessage = createBaseMessage(inMessage, "Debit");

			
			outAssemblyCredit = new MbMessageAssembly(inAssembly, creditMessage);
			outAssemblyDebit = new MbMessageAssembly(inAssembly, debitMessage);
			/*
			MbElement curElement = outAssembly.getLocalEnvironment().getRootElement();
			MbElement varElement = curElement.getFirstElementByPath("/Destination");
			if (varElement == null)
				curElement = curElement.createElementAsFirstChild(MbElement.TYPE_NAME, "Destination", null);
			else
				curElement = varElement;
			curElement = curElement.createElementAsFirstChild( MbElement.TYPE_NAME, "MQ", null ); 
			MbElement destination1 = curElement.createElementAsLastChild(MbElement.TYPE_NAME, "DestinationData", null);
			MbElement destination2 = curElement.createElementAsLastChild(MbElement.TYPE_NAME, "DestinationData", null);
			destination1.createElementAsFirstChild( MbElement.TYPE_NAME_VALUE, "queueName", "SYSTEM4.DEBIT" );
			destination2.createElementAsFirstChild( MbElement.TYPE_NAME_VALUE, "queueName", "SYSTEM4.CREDIT" );
			
			*/
	//	} catch (MbException e) {
	//		throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}
		out.propagate(outAssemblyCredit);
		alt.propagate(outAssemblyDebit);

	}
	
	public MbElement createLastChild(MbElement parent, String name, Object value, String namespace) throws MbException  { 
		MbElement element = parent.createElementAsLastChild(MbXMLNSC.FIELD, name, value); 
		element.setNamespace(namespace); 
		return element; 
	}
	
	public String parceYearDateMonth(String source) {
		SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat resultFormat = new SimpleDateFormat("yy-dd-MM");
		try {
			Date date = originalFormat.parse(source);
			return resultFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "error while date parcing";
	}

}
