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
import java.util.Date;
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

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.LogDepuracion;
import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.entidades.Parametros;
import sv.avantia.depurador.agregadores.jdbc.SessionFactoryUtil;
import sv.avantia.depurador.agregadores.ws.cliente.WebServicesClient;

import com.cladonia.xml.webservice.soap.SOAPClient;
import com.cladonia.xml.webservice.wsdl.WSDLException;
import com.cladonia.xml.webservice.wsdl.XMLSupport;

public class ConsultaAgregadorPorHilo extends Thread {

	private List<String> moviles = new ArrayList<String>();
	private Agregadores agregador;
	private HashMap<String, String> parametrosData = new HashMap<String, String>();

	/**
	 * Obtener el appender para la impresión en un archivo de LOG
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	public static Logger logger = Logger.getLogger("avantiaLogger");
	
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
		System.out.println("Inicio Hilo por agregador " + getAgregador().getNombre_agregador());
		if (getAgregador() != null) 
		{
			if (getAgregador().getMetodos() != null) 
			{
				for (String movil : moviles) 
				{
					invocacionPorNumero(movil);
				}
			}
		}
	}
	
	
	/**
	 * Ejecucines por numero de telefonia movil
	 * @author Edwin Mejia - Avantia Consultores
	 * @param movil {@link String} numero de celular procesado
	 * @return {@link String}
	 * */
	public synchronized void invocacionPorNumero(String movil) throws WSDLException{
		//obtener el mensaje de input y modificar parametros
		int ordenEjecucion = 1;
		int tamanio = getAgregador().getMetodos().size();
		while(true){
			for (Metodos metodo : getAgregador().getMetodos()) 
			{
			
				//porque voy a meter en un solo metodo el setear paraetros llenare esto aqui ahorita
				getParametrosData().put("movil", movil);
				getParametrosData().put("fecha", new Date().toString());
				getParametrosData().put("accion", metodo.getAccion());
				getParametrosData().put("operacion", metodo.getAccion());
				getParametrosData().put("pass", metodo.getPass());
				
				// este seria primero ejecutar la baja de la lista negra
				// luego consultar el historial de servicios 
				// buscar en ese historial de servicios los que esten activos 
				// y de esos activos seria ejecutar la baja para cada uno de dichos servicios
				
				//verificar esto en contra del tamaño de la lista  de metodo para hacer un bucle hasta ejecutarse en el orden deseado
				if(ordenEjecucion == metodo.getOrdenEjecucion() && ordenEjecucion<= tamanio)
				{
					if (metodo.getParametros() != null) 
					{
						for (Parametros parametro : metodo.getParametros()) 
						{
							System.out.println("Remplazando " + parametro.getNombre());
							metodo.setInputMessageText(metodo.getInputMessageText().replaceAll("_*" + parametro.getNombre() + "_*" , getParametrosData().get(parametro.getNombre())));
						}
					}
					
					//con el resultado debo llenar mas el getParametrosData().put("algo debo poner sgun paamerizacion de respuesta", respuesta.getposiciion);
					System.out.println("invocamos metodo");
					if (metodo.getSeguridad().equals(0)) {
						//System.out.println("getInputMessageText: "	+ metodo.getInputMessageText());
						//System.out.println("SOAP response: \n"+ invokeOperation(metodo, null));
						invokeOperation(metodo, null);
					}
					
					if (metodo.getSeguridad().equals(1)) {
						talk(metodo);
					}
					
					ordenEjecucion++;
				}
				else
				{
					break;
				}
			}
			break;
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
		stub.setAddress("http://localhost:8090/axis2/services/servicio_1.servicio_1HttpSoap11Endpoint/");
		stub.setNamespaceURI("http://web.servicio.avantia.sv");
		stub.setReturnType(XMLType.XSD_STRING);//XMLType.XSD_STRING or Qname
		stub.setServiceName("servicio_1");
		stub.setPortName("servicio_1HttpSoap11Endpoint");			
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
	public void invokeOperation(Metodos operation,File[] attachments)
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
		URL url = new URL(operation.getEndPoint());

		// send the soap message
		Document responseDoc = client.send(url);
		
		lecturaCompleta(responseDoc, "ns:return", operation);
       // returns just the soap envelope part of the message (i.e no returned attachements will be
	   // seen)
		//return XMLSupport.prettySerialise(responseDoc);
		
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
	public void talk(Metodos metodo) {
		try {
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage msg = messageFactory.createMessage(
					new MimeHeaders(),
					new ByteArrayInputStream(metodo.getInputMessageText().getBytes(Charset.forName("UTF-8"))));

			// View input
			System.out.println("Soap request:");
			msg.writeTo(System.out);

			// Trust to certificates
			doTrustToCertificates();

			// SOAPMessage rp = conn.call(msg, urlval);
			SOAPMessage rp = sendMessage(msg, metodo.getEndPoint());

			// View the output
			System.out.println("Soap response");
			rp.writeTo(System.out);
			
			Document doc = toDocument(rp);
			
			lecturaCompleta(doc, "ns1:resultCode", metodo);
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
	
	private void lecturaCompleta(Document doc, String nodeNameToReader, Metodos metodo) {
		doc.getDocumentElement().normalize();
		if (doc.getDocumentElement().hasChildNodes()) {
			NodeList nodeList = doc.getDocumentElement().getChildNodes();
			readerList(nodeList, nodeNameToReader, metodo);
		}
	}

	private void readerList(NodeList nodeList, String nodeNameToReader, Metodos metodo) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if(node.getNodeName().equals(nodeNameToReader))
				{
					LogDepuracion objGuardar = new LogDepuracion();
					objGuardar.setFechaProcesamiento(new Date());
					objGuardar.setIdError(node.getTextContent());
					objGuardar.setMetodo(metodo);
					objGuardar.setNumero(getParametrosData().get("movil"));
					createData(objGuardar);
				}
				
				if (node.hasChildNodes())
					readerList(node.getChildNodes(), nodeNameToReader, metodo);
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

	
	/**
	 * Metodo que nos servira para realizar cualquier inserción dentro de la
	 * base de datos
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param obj
	 *            {java.lang.Object} return void
	 * */
	public void createData(Object obj) {
		Session session = SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		try 
		{
			
			session.beginTransaction();
			session.save(obj);
			session.getTransaction().commit();
		
		} catch (RuntimeException e) {
			logger.error("Error al querer realizar una insercion a la base de datos", e);

			if (session.getTransaction() != null && session.getTransaction().isActive()) {
				try 
				{
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.error("Error al querer realizar rolback a la base de datos", e1);
				}
				// throw again the first exception
				throw e;
			}
		}
	}
	
	private HashMap<String, String> getParametrosData() {
		return parametrosData;
	}	
}