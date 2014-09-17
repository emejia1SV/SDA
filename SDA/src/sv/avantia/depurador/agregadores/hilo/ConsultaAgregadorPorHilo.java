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
import sv.avantia.depurador.agregadores.entidades.Respuesta;
import sv.avantia.depurador.agregadores.jdbc.SessionFactoryUtil;
import sv.avantia.depurador.agregadores.ws.cliente.WebServicesClient;

import com.cladonia.xml.webservice.soap.SOAPClient;
import com.cladonia.xml.webservice.wsdl.WSDLException;
import com.cladonia.xml.webservice.wsdl.XMLSupport;

public class ConsultaAgregadorPorHilo extends Thread {

	/**
	 * Instancia del insumo {@link List} de {@link String} donde se espera
	 * recibir un listado de numeros para depurar
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private List<String> moviles = null;
	
	/**
	 * Instancia del insumo {@link Agregadores} que se espera recibir y se espera nunca llegue a
	 * esta instancia nulo y este es el que obtendra todo el insumo de la
	 * parametrizacion para la consulta a los agregadores
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private Agregadores agregador = null;
	
	/**
	 * Instancia de un {@link HashMap} para mantener en memoria los parametros
	 * con los que me serviran de insumo para llenar los parametros requeridos
	 * por los agregadores
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private HashMap<String, String> parametrosData = null;

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
			logger.info("Se ha terminado la ejecución del Thread por el agregador " + getAgregador().getNombre_agregador());
		} catch (Exception e) {
			logger.error("Error dentro del Hilo de ejecucion para la depuracion " + e.getMessage(), e);
			this.interrupt();
		} finally{
			this.interrupt();
		}
	}
	
	/**
	 * Seccion de lectura de los numeros de telefono
	 * @author Edwin Mejia - Avantia Consultores
	 * @throws Exception 
	 * 
	 * */
	private void procesarServiciosWeb() throws Exception
	{		
		if (getAgregador() != null) 
		{
			if (getAgregador().getMetodos() != null) 
			{
				for (String movil : getMoviles()) 
				{
					invocacionPorNumero(movil);
				}
			}
			else
			{
				this.interrupt();
			}
		}
		else
		{
			this.interrupt();
		}
	}
	
	
	/**
	 * Ejecucines por numero de telefonia movil
	 * @author Edwin Mejia - Avantia Consultores
	 * @param movil {@link String} numero de celular procesado
	 * @return {@link String}
	 * @throws Exception 
	 * */
	public synchronized void invocacionPorNumero(String movil) throws Exception
	{
		logger.info("Se iniciaran las invocaciones por el número "+movil);
		//obtener el mensaje de input y modificar parametros
		int ordenEjecucion = 1;
		int tamanio = getAgregador().getMetodos().size();
		boolean seguir=true;
		while(seguir)
		{
			
			//nos evitamos un bucle infinito
			if( getAgregador().getMetodos().size()<1)
			{
				seguir = false;
				break;
			}
			
			for (Metodos metodo : getAgregador().getMetodos()) 
			{
				//porque voy a meter en un solo metodo el setear paraetros llenare esto aqui ahorita
				setParametrosData(new HashMap<String, String>());
				getParametrosData().put("movil", movil);
				getParametrosData().put("fecha", new Date().toString());
				getParametrosData().put("accion", "2");//2 es el que ejecuta la liminacion de la lista negra
				getParametrosData().put("operacion", "2");//2 es el que ejecuta la liminacion de la lista negra
				getParametrosData().put("pass", metodo.getPass());
				getParametrosData().put("servicio", "");
				getParametrosData().put("mcorta", "");
				
				// este seria primero ejecutar la baja de la lista negra
				// luego consultar el historial de servicios 
				// buscar en ese historial de servicios los que esten activos 
				// y de esos activos seria ejecutar la baja para cada uno de dichos servicios
				
				//logger.info(">>>> " + tamanio);
				//logger.info(">>>> " + ordenEjecucion);
				//logger.info(">>>> " + metodo.getOrdenEjecucion());
				
				//verificar esto en contra del tamaño de la lista  de metodo para hacer un bucle hasta ejecutarse en el orden deseado
				if(ordenEjecucion == metodo.getOrdenEjecucion() && ordenEjecucion <= tamanio)
				{
					if (metodo.getParametros() != null) 
					{
						for (Parametros parametro : metodo.getParametros()) 
						{
							metodo.setInputMessageText(metodo.getInputMessageText().replace(("_*" + parametro.getNombre() + "_*").toString() , getParametrosData().get(parametro.getNombre())));
						}
					}
					
					//con el resultado debo llenar mas el getParametrosData().put("algo debo poner sgun paamerizacion de respuesta", respuesta.getposiciion);
					if (metodo.getSeguridad() == 0) 
					{
						invokeOperation(metodo, null);
					}
					
					if (metodo.getSeguridad() == 1) 
					{
						talk(metodo);
					}
					ordenEjecucion++;
				}
				else
				{
					//logger.info(">> " + tamanio);
					//logger.info(">> " + ordenEjecucion);
					if(ordenEjecucion>tamanio)
						seguir = false;
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
	@Deprecated
	public String consultarServicio(Map<String, Object> definitionArgument, String operacion) throws Exception{
		WebServicesClient stub = new WebServicesClient();
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
	 * @param operation {@link Metodos} The selected operation
	 * @param attachements The required attachments
	 *
	 * @return The response SOAP Envelope as a String
	 * @throws WSDLException algun problema en la interpretacion e invocacion de los servicios
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
			client.setSOAPAction(operation.getSoapActionURI());
	
			// get the url
			URL url = new URL(operation.getEndPoint());
	
			// send the soap message
			Document responseDoc = client.send(url);
			
			lecturaCompleta(responseDoc, operation);
		}
		catch (Exception e)
		{
			logger.error("Problemas al invocar el metodo in seguridad", e);
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
					logger.warn("Warning: URL host '" + urlHostName	+ "' is different to SSLSession host '"	+ session.getPeerHost() + "'.");
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
	 * @param  metodo {@link Metodos}
	 * @return {@link Void}
	 * @throws Exception 
	 * */
	private void talk(Metodos metodo) throws Exception {
		try {
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage msg = messageFactory.createMessage(
					new MimeHeaders(),
					new ByteArrayInputStream(metodo.getInputMessageText().getBytes(Charset.forName("UTF-8"))));

			// View input
			//msg.writeTo(System.out);

			// Trust to certificates
			doTrustToCertificates();

			// SOAPMessage rp = conn.call(msg, urlval);
			SOAPMessage rp = sendMessage(msg, metodo.getEndPoint());

			// View the output
			//rp.writeTo(System.out);
			
			Document doc = toDocument(rp);
			
			lecturaCompleta(doc, metodo);
		} 
		catch (Exception e) 
		{
			throw e;
		}
	}
	
	/**
	 * Metodo para la transformación de un {@link SOAPMessage} Response a un
	 * {@link Document} y hacer mas facil la busqueda del dato
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param soapMsg
	 *            {@link SOAPMessage}
	 * @return {@link Document}
	 * */
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
	
	/**
	 * Metodo que recibe el documento Soap Response y este hace la lectura de la
	 * lista de nodos que obtiene para procesarlos y buscar la {@link Respuesta}
	 * deseada
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param doc
	 *            {@link Document}
	 * @param nodeNameToReader
	 *            {@link String}
	 * @param metodo
	 *            {@link Metodos}
	 * @return {@link Void}
	 * */
	private void lecturaCompleta(Document doc, Metodos metodo) {
		doc.getDocumentElement().normalize();
		for (Respuesta respuesta : metodo.getRespuestas()) {
			if (doc.getDocumentElement().hasChildNodes()) {
				NodeList nodeList = doc.getDocumentElement().getChildNodes();
				lecturaListadoNodos(nodeList, respuesta.getNombre(), metodo);
			}
		}
	}

	/**
	 * Metodo recursivo, para la lectura de nodos del Soap Response que se ha
	 * recibido de la consulta de los {@link Agregadores} a su vez este metodo
	 * se encarga de guardar en la base de datos as {@link Respuesta} obtenidas
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param nodeList
	 *            {@link NodeList}
	 * @param nodeNameToReader
	 *            {@link String} nombre del nodo que andamos buscando dentro del listado
	 * @param metodo
	 *            {@link Metodos} insumo para poder guardar la {@link Respuesta}
	 *            en la base de datos
	 * @return {@link Void}
	 * */
	private void lecturaListadoNodos(NodeList nodeList, String nodeNameToReader, Metodos metodo) {
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
					lecturaListadoNodos(node.getChildNodes(), nodeNameToReader, metodo);
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
	 * @throws MalformedURLException
	 * @throws SOAPException 
	 * @throws UnsupportedOperationException 
	 * */
	static private SOAPMessage sendMessage(SOAPMessage message, String endPoint) 
			throws MalformedURLException, UnsupportedOperationException, SOAPException
	{
		SOAPMessage result = null;
		if (endPoint != null && message != null) 
		{
			URL url = new URL(endPoint);
			SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
			SOAPConnection connection = null;
			try 
			{
				connection = scf.createConnection(); // point-to-point connection
				result = connection.call(message, url);
			} 
			finally 
			{
				if (connection != null) 
				{
					try 
					{
						connection.close();
					} 
					catch (SOAPException soape) 
					{
						logger.error("Can't close SOAPConnection:" + soape , soape);
					}
				}
			}
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
	
	/**
	 * getter
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @return {@link HashMap} listado de datos para llenar parametros de los
	 *         servicios web de los  {@link Agregadores}
	 */
	private HashMap<String, String> getParametrosData() {
		return parametrosData;
	}

	/**
	 * setter
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param parametrosData the parametrosData to set {@link HashMap} 
	 * @return {@link Void}
	 */
	private void setParametrosData(HashMap<String, String> parametrosData) {
		this.parametrosData = parametrosData;
	}	
}