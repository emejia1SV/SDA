package sv.avantia.depurador.agregadores.hilo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.rpc.encoding.XMLType;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.entidades.Parametros;
import sv.avantia.depurador.agregadores.ws.cliente.Cliente;
import sv.avantia.depurador.agregadores.ws.cliente.WebServicesClient;

import com.cladonia.xml.webservice.soap.SOAPClient;
import com.cladonia.xml.webservice.wsdl.WSDLException;
import com.cladonia.xml.webservice.wsdl.XMLSupport;

public class ConsultaAgregadorPorHilo extends Thread {

	private List<String> moviles = new ArrayList<String>();
	private Agregadores agregador;
	private HashMap<String, String> parametrosData = new HashMap<String, String>();

	public void run() {
		// consultar un agregador WS
		try {
			procesarServiciosWeb();
		} catch (Exception e) {
			System.out.println("Hubo Error dentro del hilo");
			this.interrupt();
			e.printStackTrace();
		}
	}
	
	private void procesarServiciosWeb() throws WSDLException
	{		
		if (getAgregador() != null) 
		{
			if (getAgregador().getMetodos() != null) 
			{
				System.out.println("procesar metodos...");
				for (Metodos metodo : getAgregador().getMetodos()) 
				{
					if (metodo.getParametros() != null) 
					{
						
						System.out.println("procesando ");
						for (String movil : moviles) 
						{	
							for (Parametros parametro : metodo.getParametros()) 
							{
								System.out.println("Remplazando " + parametro.getNombre());
								metodo.setInputMessageText(metodo.getInputMessageText().replaceAll(parametro.getNombre(), movil));									
							}

							if (metodo.getSeguridad().equals(0)) {
								System.out.println("getInputMessageText: "	+ metodo.getInputMessageText());
								System.out.println("SOAP response: \n"+ invokeOperation(metodo, null));
							}
							
							if (metodo.getSeguridad().equals(1)) {
								talk(metodo.getEndPoint(), metodo.getInputMessageText());
							}
						}
					}
				}
			}
		}
	}

	public List<String> getMoviles() {
		return moviles;
	}

	public void setMoviles(List<String> moviles) {
		this.moviles = moviles;
	}

	public Agregadores getAgregador() {
		return agregador;
	}

	public void setAgregador(Agregadores agregador) {
		this.agregador = agregador;
	}
	

	//*********************************************************************************************************************************/
	//		cliente SIN SSL es un cliente normal sin seguridad RPC
	//*********************************************************************************************************************************/

	
	/**
	 * Llenar con los parametros requeridos para el llamdo de el web services
	 * para la comunicacion con los servicios parametrizados
	 * 
	 * @author Emejia - Avantia Consultores
	 * @param definitionArgument
	 *            - es en objeto vienen los dos marametros listos para consumir
	 *            el web services SMG3 donde viene el objeto y el dato para el
	 *            trace level del SMG3
	 * @param operacion
	 *            - este es el nombre de la operacion que se va a efectuar
	 *            dentro de servicio web ya sea para consultar una estrategia o
	 *            para actualizar dichas estrategias
	 * @throws Exception
	 * */
	public String consultarServicio(Map<String, Object> definitionArgument, String operacion) throws Exception{
		WebServicesClient stub = new WebServicesClient();
		//1st argument service URI, refer to wsdl document above
		//2nd argument is service name, refer to wsdl document above
		//QName qname = new QName("http://webservices.smg3.bbmass.com.sv/", "DACallWSService");
		//Localmente >> stub.setAddress("http://sv01d000n6116:9081/SMG3_HTTP/DACallWSService");
		//DESARROLLO CLUSTER >> stub.setAddress("http://sv4044aap.daviviendasv.com:9121/SMG3_HTTP/DACallWSService");
		//BUS >>stub.setAddress("http://sv4012lap.daviviendasv.com/SMG3_HTTP/DACallWSService");
		stub.setAddress("http://sv4012lap/SMG3_HTTP/DACallWSService");
		stub.setNamespaceURI("http://webservices.smg3.bbmass.com.sv/");
		stub.setReturnType(XMLType.XSD_STRING);//XMLType.XSD_STRING or Qname
		stub.setServiceName("DACallWSService");
		stub.setPortName("DACallWSPort");			
		stub.setDefinitionArgument(definitionArgument);
		stub.setOpertationNameInvoke(operacion); //"DACallPREBURO"
		stub.setTimeOutSeconds(6000);
		
		return (String) stub.respuestaWebService();
	}
	
	//*********************************************************************************************************************************/
	//		cliente SIN SSL es un cliente normal sin seguridad
	//*********************************************************************************************************************************/

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
	
	//*********************************************************************************************************************************/
	//		cliente SSL
	//*********************************************************************************************************************************/

	/**
	 * Metodo que nos carga un certificado digital
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @return {@link Void}
	 * */
	static public void doTrustToCertificates() throws Exception {
		// Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		TrustManager[] trustAllCerts = new TrustManager[] { 
				new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
		
					public void checkServerTrusted(X509Certificate[] certs,
							String authType) throws CertificateException {
						return;
					}
		
					public void checkClientTrusted(X509Certificate[] certs,
							String authType) throws CertificateException {
						return;
					}
				} 
		};

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
					System.out.println("Warning: URL host '" + urlHostName	+ "' is different to SSLSession host '"	+ session.getPeerHost() + "'.");
				}
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
	
	/**
	 * Metodo que realiza el envio del archivo request y espera el archivo
	 * response para el metodo web
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param urlval
	 *            - {@link String}
	 * @param inputMessage
	 *            - {@link String}
	 * @return {@link Void}
	 * */
	public static void talk(String urlEndpoint, String inputMessage) {
		try {
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage msg = messageFactory.createMessage(
					new MimeHeaders(),
					new ByteArrayInputStream(inputMessage.getBytes(Charset.forName("UTF-8"))));

			// View input
			System.out.println("Soap request:");
			msg.writeTo(System.out);

			// Trust to certificates
			doTrustToCertificates();

			// SOAPMessage rp = conn.call(msg, urlval);
			SOAPMessage rp = sendMessage(msg, urlEndpoint);

			// View the output
			System.out.println("Soap response");
			rp.writeTo(System.out);
			
			Document doc = toDocument(rp);
			
			lecturaCompleta(doc, "ns1:resultCode");
		} 
		catch (Exception e) 
		{
			System.out.println(e.getMessage());
		}
	}
	
	private static Document toDocument(SOAPMessage soapMsg)
			throws TransformerConfigurationException, TransformerException,
			SOAPException, IOException {
		Source src = soapMsg.getSOAPPart().getContent();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		DOMResult result = new DOMResult();
		transformer.transform(src, result);
		return (Document) result.getNode();
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

	/**
	 * Metodo que envia realiza la conexion al servicio web e invoca al metodo a
	 * ejecutar
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param message
	 *            - {@link SOAPMessage}
	 * @param endPoint
	 *            - {@link String}
	 * @return {@link SOAPMessage}
	 * */
	static public SOAPMessage sendMessage(SOAPMessage message, String endPoint)
			throws MalformedURLException, SOAPException {
		SOAPMessage result = null;
		if (endPoint != null && message != null) {
			URL url = new URL(endPoint);
			SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
			SOAPConnection connection = null;
			long time = System.currentTimeMillis();
			try {
				connection = scf.createConnection(); // point-to-point connection
				result = connection.call(message, url);
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (SOAPException soape) {
						System.out.print("Can't close SOAPConnection:" + soape);
					}
				}
			}
			System.out.println("Respuesta en " + (System.currentTimeMillis() - time));
		}
		return result;
	}	
}