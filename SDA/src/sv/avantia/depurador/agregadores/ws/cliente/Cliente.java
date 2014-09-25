package sv.avantia.depurador.agregadores.ws.cliente;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.encoding.XMLType;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.entidades.Pais;
import sv.avantia.depurador.agregadores.entidades.Parametros;
import sv.avantia.depurador.agregadores.jdbc.SessionFactoryUtil;

import com.cladonia.xml.webservice.soap.SOAPClient;
import com.cladonia.xml.webservice.wsdl.WSDLException;
import com.cladonia.xml.webservice.wsdl.XMLSupport;

public class Cliente {
		
	/* Get actual class name to be printed on */
	public static Logger logger = Logger.getLogger("avantiaLogger");
	 
	public static void main(String[] args) {
		//prueba();
		try {
			//WsdlParserXXX("http://localhost:8090/axis2/services/servicio_1?wsdl");
			prueba2();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Deprecated
	public static void prueba2(){
		Map<String, Object> definitionArgument = new HashMap<String, Object>();
		definitionArgument.put("parametroWeb1", "Edwin");
		
		WebServicesClient stub = new WebServicesClient();
		stub.setAddress("http://sv4012lap/SMG3_HTTP/DACallWSService");
		stub.setNamespaceURI("http://webservices.smg3.bbmass.com.sv/");
		stub.setReturnType(XMLType.XSD_STRING);//XMLType.XSD_STRING or Qname
		stub.setServiceName("DACallWSService");
		stub.setPortName("DACallWSPort");			
		stub.setDefinitionArgument(definitionArgument);
		stub.setOpertationNameInvoke("saludar"); //"DACallPREBURO"
		stub.setTimeOutSeconds(6000);
		
		try {
			System.out.println(stub.respuestaWebService());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void prueba(){
		try 
		{
			// nueva prueba
			Pais pais = new Pais();
			pais.setId(2);
			pais.setCodigo("502");
			pais.setNombre("Guatemala");
			pais.setEstado(1);
			
			//createData(pais);
			
			Agregadores agregador = new Agregadores();
			agregador.setEstado(1);
			agregador.setPais(pais);
			agregador.setId(5);
			agregador.setNombre_agregador("SMT");
			
			//createData(agregador);
			
			ParametrizarServicio servicio = new ParametrizarServicio();
			//agregador = wsdlparser.getServicesInfoFromFile("C:/Users/Edwin/Documents/documentacion Agregadores claro/SMT wsdl/WSDL Black Gray List_RolandoSkype/WSDL Black Gray List/SMT_blackgray_service_1_0_1.wsdl", agregador);
			agregador = servicio.getServicesInfo("http://localhost:8090/axis2/services/servicio_1?wsdl", agregador);
			
			for (Metodos operation : agregador.getMetodos()) {
				System.out.println("Operation Name: " 		+ operation.toString());
				System.out.println("getInputMessageName: "	+ operation.getInputMessageName());
				System.out.println("getInputMessageText: "	+ operation.getInputMessageText());
				System.out.println("getNamespaceURI: "		+ operation.getNamespaceURI());
				System.out.println("getSoapActionURI: "		+ operation.getSoapActionURI());
				System.out.println("getStyle: " 			+ operation.getStyle());
				System.out.println("getTargetMethodName: "	+ operation.getTargetMethodName());
				System.out.println("getTargetObjectURI(): "	+ operation.getTargetObjectURI());
				System.out.println("getTargetURL(): "		+ operation.getTargetURL());
				//System.out.println("getNombre: "		+ operation.getNombre());
				System.out.println();
				
				
				for (Parametros param : operation.getParametros()) {
					System.out.println(param.getNombre());
					//System.out.println(param.getTipo());
				}
				
				System.out.println("SOAP response: \n"+invokeOperation(operation));	
			}
			SessionFactoryUtil.closeSession();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Invoke a SOAP call passing in an operation instance
	 *
	 * @param operation The selected operation
	 *
	 * @return The response SOAP Envelope as a String
	 */
	public static String invokeOperation(Metodos operation) throws WSDLException
	{
		try{
			return invokeOperation(operation,null);
		}
		catch (Exception e)
		{
			// should log\trace this here
			throw new WSDLException(e);
		}
	}

	/**
	 * Invoke a SOAP call passing in an operation instance and attachments
	 *
	 * @param operation The selected operation
	 * @param attachements The required attachments
	 *
	 * @return The response SOAP Envelope as a String
	 */
	public static String invokeOperation(Metodos operation,File[] attachments)
	throws WSDLException
	{
		try{
		
		Document docRequest = XMLSupport.parse(operation.getInputMessageText());

		// create the saaj based soap client
		SOAPClient client = new SOAPClient(docRequest);

		// add any attachments if required
		if (attachments != null)
		{
			client.addAttachments(attachments);
		}

		// set the SOAPAction
		//System.out.println(operation.getSoapActionURI()); "loc:addGrayList"
		client.setSOAPAction(operation.getSoapActionURI());

		// get the url
		//System.out.println(operation.getTargetURL());End point "https://hub.americamovil.com/sag/services/blackgrayService"
		URL url = new URL(operation.getTargetURL());

		// send the soap message
		Document responseDoc = client.send(url);
		
		lecturaCompleta(responseDoc, "ns:return");
       // returns just the soap envelope part of the message (i.e no returned attachements will be
	   // seen)
		return XMLSupport.prettySerialise(responseDoc);
		
		}
		catch (Exception e)
		{
			// should log\trace this here
			throw new WSDLException(e);
		}
	}
	
	private static void lecturaCompleta(Document doc, String nodeNameToReader) {
		doc.getDocumentElement().normalize();
		if (doc.getDocumentElement().hasChildNodes()) {
			NodeList nodeList = doc.getDocumentElement().getChildNodes();
			readerList(nodeList, nodeNameToReader);
		}
	}

	private static void readerList(NodeList nodeList, String nodeNameToReader) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if(node.getNodeName().equals(nodeNameToReader))
					System.out.println(node.getTextContent());//esto debere guardar
				
				if (node.hasChildNodes())
					readerList(node.getChildNodes(), nodeNameToReader);
			}
		}
	}

	
	/*
	public static void testpropio(){
		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
	        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

	        String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:loc=\"http://www.csapi.org/schema/parlayx/blackgray/v1_0/local\"><soapenv:Header><wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"><wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><wsse:Username>PA00000737</wsse:Username><wsse:Password Type=\"...#PasswordDigest\">x9t/yLcnC3VYKCb6v0uezTwYJNk=</wsse:Password><wsse:Nonce>096459e93f20a2b39ab6c5ddd493e44f58bc3a91</wsse:Nonce><wsse:Created>2012-10-17T11:51:50.263Z</wsse:Created></wsse:UsernameToken></wsse:Security><tns:RequestSOAPHeader xmlns:tns=\"http://www.huawei.com.cn/schema/common/v2_1\"><tns:AppId>35000001000001</tns:AppId><tns:TransId>2014011716010012345</tns:TransId><tns:OA>50279451598</tns:OA><tns:FA>50279451598</tns:FA></tns:RequestSOAPHeader></soapenv:Header><soapenv:Body><loc:addGrayList><loc:version>1.0</loc:version><loc:grayList><grayee><msisdn>50279451598</msisdn>				            </grayee></loc:grayList></loc:addGrayList></soapenv:Body></soapenv:Envelope>";
	        
	        MessageFactory messageFactory = MessageFactory.newInstance();
	        SOAPMessage message = messageFactory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
	        
	        // Send SOAP Message to SOAP Server
	        String url = "https://hub.americamovil.com/sag/services/blackgrayService";
	        SOAPMessage soapResponse = soapConnection.call(message, url);

	        // print SOAP Response
	        System.out.print("Response SOAP Message:");
	        soapResponse.writeTo(System.out);

	        soapConnection.close();
			//invokeOperation(null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testLocal(){
		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
	        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

	        String xml= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cad=\"http://cadena.webservices.test.com\"><soapenv:Header/><soapenv:Body><cad:saludo><!--Optional:--><cad:texto>prueba</cad:texto></cad:saludo></soapenv:Body></soapenv:Envelope>";
	        
	        MessageFactory messageFactory = MessageFactory.newInstance();
	        SOAPMessage message = messageFactory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
	        
	        // Send SOAP Message to SOAP Server
	        String url = "http://192.168.0.100:8090/axis2/services/pruebaWsCadena.pruebaWsCadenaHttpSoap11Endpoint/";
	        SOAPMessage soapResponse = soapConnection.call(message, url);

	        // print SOAP Response
	        System.out.println("Response SOAP Message:");
	        soapResponse.writeTo(System.out);

	        soapConnection.close();
			//invokeOperation(null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws Exception
	{
		test1();
	}
	
	
	
	@SuppressWarnings("static-access")
	public static void test2() throws WSDLException{
	
		System.out.println("Starting the WSDL Parse..");
		Cliente wsdlparser = new Cliente();
		
		Metodos operation = new Metodos();
		operation.setInputMessageName("saludoRequest");
		operation.setInputMessageText("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <SOAP-ENV:Envelope xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><SOAP-ENV:Body xmlns=\"http://cadena.webservices.test.com\"><saludo><texto>EDWIN</texto></saludo></SOAP-ENV:Body></SOAP-ENV:Envelope>");
		operation.setNamespaceURI("http://cadena.webservices.test.com");
		operation.setSoapActionURI("urn:saludo");
		operation.setStyle("document");
		operation.setTargetMethodName("saludo");
		operation.setTargetObjectURI("");
		operation.setTargetURL("http://192.168.0.100:8090/axis2/services/pruebaWsCadena.pruebaWsCadenaHttpSoap11Endpoint/");
		System.out.println(wsdlparser.invokeOperation(operation));
	}
	
	public static void test1() throws WSDLException{
		// for testing purposes only

		Pais pais = new Pais();
		pais.setId(2);
		pais.setCodigo("502");
		pais.setNombre("Guatemala");
		pais.setEstado(1);
		
		//createData(pais);
		
		Agregadores agregador = new Agregadores();
		agregador.setEstado(1);
		agregador.setPais(pais);
		agregador.setId(5);
		agregador.setNombre_agregador("SMT");
		
		//createData(agregador);
		
		System.out.println("Starting the WSDL Parse..");
		Cliente wsdlparser = new Cliente();
		// http://www.xignite.com/xquotes.asmx?WSDL
		
		//agregador = wsdlparser.getServicesInfo("http://72.249.190.94/wssvamx/servicios.php?wsdl", agregador);
		
		for (Metodos operation : agregador.getMetodos()) {
			System.out.println("Operation Name: " 		+ operation.toString());
			System.out.println("getInputMessageName: "	+ operation.getInputMessageName());
			System.out.println("getInputMessageText: "	+ operation.getInputMessageText());
			System.out.println("getNamespaceURI: "		+ operation.getNamespaceURI());
			System.out.println("getSoapActionURI: "		+ operation.getSoapActionURI());
			System.out.println("getStyle: " 			+ operation.getStyle());
			System.out.println("getTargetMethodName: "	+ operation.getTargetMethodName());
			System.out.println("getTargetObjectURI(): "	+ operation.getTargetObjectURI());
			System.out.println("getTargetURL(): "		+ operation.getTargetURL());
			System.out.println();
			
			
			for (Parametros param : operation.getParametros()) {
				System.out.println(param.getNombre());
				System.out.println(param.getTipo());
			}
			
			//System.out.println("SOAP response: \n"+invokeOperation(operation));	
		}
		SessionFactoryUtil.closeSession();
	}
	
	
	
	@SuppressWarnings("unused")
	private static void createData(Object obj) {
		Session session = SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.save(obj);
			session.getTransaction().commit();
			//session.close();
		} catch (RuntimeException e) {
			if (session.getTransaction() != null && session.getTransaction().isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.debug("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}
		}
	}*/
	
	/*
	
	public static void WsdlParserXXX(String wsdlURL) throws javax.wsdl.WSDLException {

		WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

		wsdlReader.setFeature("javax.wsdl.verbose", false);
		wsdlReader.setFeature("javax.wsdl.importDocuments", true);

		Definition definition = wsdlReader.readWSDL(wsdlURL);
		if (definition == null) {
		    System.err.println("definition element is null");
		    System.exit(1);
		}

// find service
		Map servicesMap = definition.getServices();
		Iterator servicesIter = servicesMap.values().iterator();
		Service service;
		QName qName;
		while (servicesIter.hasNext()) {
		    service = (Service) servicesIter.next();
		    qName = service.getQName();
		    System.out.println("Service qName: " + qName + "\nLocal Part: " + qName.getLocalPart() + "\nNamespace URI: " + qName.getNamespaceURI());
		    
		}

		//*************************************************************
		// *************************************************************
		// *************************************************************//*
		Map portTypesMap = definition.getAllPortTypes();
		Iterator portTypesIter = portTypesMap.values().iterator();
		PortType portType;
		while (portTypesIter.hasNext()) {
		    portType = (PortType) portTypesIter.next();
		    Iterator operationsIter = portType.getOperations().iterator();
		    Operation operation;
		    while (operationsIter.hasNext()) {
		        operation = (Operation) operationsIter.next();

		        System.out.println("Input Message: " + operation.getInput().getName());

		        System.out.println("******************* " + operation.getName() + " *******************");
		        // display request parameters
		        Map inputPartsMap = operation.getInput().getMessage().getParts();
		        Collection inputParts = inputPartsMap.values();
		        Iterator inputPartIter = inputParts.iterator();
		        System.out.print("\tRequest: ");
		        String inPartName;
		        QName inPartTypeName;
		        while (inputPartIter.hasNext()) {
		            Part part = (Part) inputPartIter.next();
		            inPartName = part.getName();
		            inPartTypeName = part.getTypeName();
//                        System.out.println(inPartName + ":" + inPartTypeName.getLocalPart() + " , " + inPartTypeName.getNamespaceURI());
		            System.out.println(inPartName + ":" + inPartTypeName);
		        }

// display response parameters
		        Map outputPartsMap = operation.getOutput().getMessage().getParts();
		        Collection outputParts = outputPartsMap.values();
		        Iterator outputPartIter = outputParts.iterator();
		        System.out.print("\tResponse: ");
		        String outPartName;
		        QName outPartTypeName;
		        while (outputPartIter.hasNext()) {
		            Part part = (Part) outputPartIter.next();
		            outPartName = part.getName();
		            outPartTypeName = part.getTypeName();
//                        System.out.println(outPartName + ":" + outPartTypeName.getLocalPart() + " , " + outPartTypeName.getNamespaceURI());
		            System.out.println(outPartName + ":" + outPartTypeName);
		        }
		    }
		}
    }

*/
	 /*DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    File file = new File("C:/Users/mar/Desktop/JAVAxml/libro.xml");
    org.jdom.Document doc = (org.jdom.Document) docBuilder.parse(file);

    doc.getDocumentElement().normalize();
    
    SAXBuilder builder = new SAXBuilder();
	  	File xmlFile = new File("c:\\file.xml");

	  try {

		Document document = (Document) builder.build(xmlFile);*/

   /* 
    String xml = new XMLOutputter().outputString(doc);
       InputStream inputStream = new StringBufferInputStream(xml);
     SOAPMessage message = messageFactory.createMessage(null, inputStream);
   
    */
	
}