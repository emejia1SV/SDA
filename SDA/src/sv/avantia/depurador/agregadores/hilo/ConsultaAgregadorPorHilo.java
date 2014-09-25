package sv.avantia.depurador.agregadores.hilo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.LogDepuracion;
//import sv.avantia.depurador.agregadores.entidades.LogDepuracion;
import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.entidades.Parametros;
import sv.avantia.depurador.agregadores.entidades.ParametrosSistema;
import sv.avantia.depurador.agregadores.entidades.Respuesta;
import sv.avantia.depurador.agregadores.entidades.UsuarioSistema;
import sv.avantia.depurador.agregadores.jdbc.BdEjecucion;
import sv.avantia.depurador.agregadores.utileria.MetodosComparator;

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
	 * Instancia del usuario del sistema que esta ejecutando el proceso en este momento.
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private UsuarioSistema usuarioSistema = null;
	
	/**
	 * Instancia de un {@link HashMap} para mantener en memoria los parametros
	 * con los que me serviran de insumo para llenar los parametros requeridos
	 * por los agregadores
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private HashMap<String, String> parametrosData = null;
	
	/**
	 * Instancia de la Clase {@link BdEjecucion} que maneja los tipos de
	 * transacciones, que podemos realizar contra la base de datos.
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private BdEjecucion ejecucion = new BdEjecucion();

	/**
	 * Instancia que mantiene en memoria el tipo de depuracion que se esta
	 * realizando Tipo de depuracion ARCHIVO MASIVA UNITARIA
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private String tipoDepuracion;

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
		logger.info("Se iniciaran las invocaciones por el número " + movil);
		//obtener el mensaje de input y modificar parametros
		
		if(getAgregador().getMetodos().size()>0)
		{
			//ordernar Los metodos web por su tipo de ejecucion
			List<Metodos> metodos = new ArrayList<Metodos>();
			metodos.addAll(getAgregador().getMetodos());
			Collections.sort(metodos, new MetodosComparator());
			
			//************* LISTA NEGRA ***************//
			//llenar los parametros para los metodos web.
			llenarParametros(movil, metodos.get(0));
			
			// ejecutamos el primer metodo de lista negra
			lecturaCompleta(ejecucionMetodo(metodos.get(0)), metodos.get(0));
			
			//********* CONSULTA DE SERVICIOS **********//
			lecturaCompleta(ejecucionMetodo(metodos.get(1)), "item", metodos.get(2));
		}
	}
	
	/**
	 * Llena los parametros que serviran de insumo para la invocacion de los
	 * metodos web y estos se mantendran en memoria para agregar mas parametros
	 * mas adelante, en este caso se llenan primeramente con los de la tabla de
	 * la base de datos SDA_PARAMETROS_SISTEMA, luego se ejan unos explicitos
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param movil {@link String}
	 * @param metodo {@link Metodos}
	 * @return {@link Void}
	 * */
	@SuppressWarnings("unchecked")
	private void llenarParametros(String movil, Metodos metodo) throws NoSuchAlgorithmException {
		
		setParametrosData(new HashMap<String, String>());
		List<ParametrosSistema> parametrosSistemas = (List<ParametrosSistema>) (List<?>)getEjecucion().listData("from SDA_PARAMETROS_SISTEMA");
		for (ParametrosSistema parametrosSistema : parametrosSistemas) {
			getParametrosData().put(parametrosSistema.getDato(), parametrosSistema.getValor());
		}
		getParametrosData().put("movil", movil);
		getParametrosData().put("date", new Date().toString());
		getParametrosData().put("fecha", new Date().toString());
		getParametrosData().put("dateSMT", fechaFormated());
		getParametrosData().put("nonce", java.util.UUID.randomUUID().toString());
		getParametrosData().put("pass", metodo.getContrasenia());
		getParametrosData().put("user", metodo.getUsuario());
		getParametrosData().put("passSMT", contraseniaSMT(getParametrosData().get("nonce"), getParametrosData().get("dateSMT"), metodo.getContrasenia()));
		
	}
	
	/**
	 * Metodo que genera la contraseña de seguridad para el agregador del SMT
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param nonce
	 *            numero aleatorio
	 * @param timestamp
	 *            {@link String} fecha ya formateada
	 * @param pass
	 *            {@link String} password sin encriptar
	 * @return {@link String}
	 * */
	private String contraseniaSMT(String nonce, String timestamp, String pass) throws NoSuchAlgorithmException{
		String concatenacion = nonce.concat(timestamp).concat(pass);
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(concatenacion.getBytes());
		return Base64.encode(md.digest());
	}
	
	/**
	 * Metodo que se encarga de formatear la fecha asi como fue solictado por el SMT
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @return {@link String}
	 * */
	private String fechaFormated(){
    	SimpleDateFormat dateT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	return dateT.format(Calendar.getInstance().getTime());
    }
	
	/**
	 * Metodo que se encarga de cambiar los parametros por data real se remplaza
	 * el comodin _*, Se verifica si el metodo tiene seguridad o no osea si
	 * tiene un http o un https y de esta forma sabe por donde sera ejecutado
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param metodo
	 *            {@link Metodos}
	 * @return {@link Document}
	 * @throws Exception
	 * */
	private Document ejecucionMetodo(Metodos metodo) throws Exception{
		if (metodo.getParametros() != null) 
		{
			for (Parametros parametro : metodo.getParametros()) 
			{
				metodo.setInputMessageText(metodo.getInputMessageText().replace(("_*" + parametro.getNombre() + "_*").toString() , (getParametrosData().get(parametro.getNombre())==null?"":getParametrosData().get(parametro.getNombre()))  ));
			}
		}
		
		if (metodo.getSeguridad() == 0) 
		{
			return invokeOperation(metodo, null);
		}
		
		if (metodo.getSeguridad() == 1) 
		{
			return talk(metodo);
		}
		return null;
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
	 * @throws Exception 
	 * */
	private void lecturaCompleta(Document doc, String dato, Metodos metodo) throws Exception {
		doc.getDocumentElement().normalize();
		if (doc.getDocumentElement().hasChildNodes()) {
			NodeList nodeList = doc.getDocumentElement().getChildNodes();
			lecturaListadoNodos2(nodeList, dato, metodo);
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
	 * @return {@link Void}
	 * @throws Exception 
	 * */
	private void lecturaListadoNodos2(NodeList nodeList, String nodeNameToReader, Metodos metodoBaja) throws Exception {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if(node.getNodeName().equals(nodeNameToReader))
				{
					if(node.getFirstChild().getNodeValue().trim().isEmpty()){
						if(indiceServicios>0){
							//********* BAJA DE SERVICIOS **********//
							if(getParametrosData().get("servicioActivado").equals("1"))//el servicio esta activado y hay que darle de baja
								ejecucionMetodo(metodoBaja);
						}
						indiceServicios ++;
						indice = 1;
					}else{
						if(indice==1)
							getParametrosData().put("servicio", node.getFirstChild().getNodeValue().trim());
						if(indice==1)
							getParametrosData().put("servicioActivado", node.getFirstChild().getNodeValue().trim());
						if(indice==5)
							getParametrosData().put("marcacion", node.getFirstChild().getNodeValue().trim());
						
						indice++;
					}
				}
				
				if (node.hasChildNodes())
					lecturaListadoNodos2(node.getChildNodes(), nodeNameToReader, metodoBaja);
			}
		}
	}
	
	private int indiceServicios = 0;
	private int indice = 1;
	
	/**
	 * Metodo que recibe el documento Soap Response y este hace la lectura de la
	 * lista de nodos que obtiene para procesarlos y buscar la {@link Respuesta}
	 * deseada
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param doc
	 *            {@link Document}
	 * @param metodo
	 *            {@link Metodos}
	 * @return {@link Void}
	 * */
	private void lecturaCompleta(Document doc, Metodos metodo) {
		doc.getDocumentElement().normalize();
		logger.info("A buscar cada respuesta");
		for (Respuesta respuesta : metodo.getRespuestas()) {
			if (doc.getDocumentElement().hasChildNodes()) {
				NodeList nodeList = doc.getDocumentElement().getChildNodes();
				lecturaListadoNodos(nodeList, respuesta.getNombre(), metodo);
				
				if(!validateBrowserResponse){
					LogDepuracion objGuardar = new LogDepuracion();
					objGuardar.setNumero(getParametrosData().get("movil"));
					objGuardar.setRespuesta("No se encontro el nodo " + respuesta.getNombre() +" dentro del documento " + getStringFromDocument(doc));
					objGuardar.setEstadoTransaccion("fallida");
					objGuardar.setFechaTransaccion(new Date());
					objGuardar.setMetodo(metodo);
					objGuardar.setEnvio(metodo.getInputMessageText());
					objGuardar.setTipoTransaccion(getTipoDepuracion());
					if(getUsuarioSistema().getId()==null){
						logger.warn("El usuario no pudo ser obtenido; para no detener el flujo, se guardara con el usuario maestro");
						objGuardar.setUsuarioSistema(getEjecucion().usuarioMaestro());
					}else{
						objGuardar.setUsuarioSistema(getUsuarioSistema());
					}
					
					
					logger.info("A guardar a la bd la respuesta");
					getEjecucion().createData(objGuardar);
				}else{
					validateBrowserResponse = false;
				}
			}
		}
	}
	
	boolean validateBrowserResponse = false;

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
		logger.info("buscando el nodo " + nodeNameToReader);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if(node.getNodeName().equals(nodeNameToReader))
				{
					LogDepuracion objGuardar = new LogDepuracion();
					objGuardar.setNumero(getParametrosData().get("movil"));
					objGuardar.setRespuesta(node.getTextContent());
					objGuardar.setEstadoTransaccion("Exitosa");
					objGuardar.setFechaTransaccion(new Date());
					objGuardar.setMetodo(metodo);
					objGuardar.setEnvio(metodo.getInputMessageText());
					objGuardar.setTipoTransaccion(getTipoDepuracion());
					objGuardar.setUsuarioSistema(getUsuarioSistema());
					
					logger.info("A guardar a la bd la respuesta");
					getEjecucion().createData(objGuardar);
					
					validateBrowserResponse = true;
				}
				
				if (node.hasChildNodes())
					lecturaListadoNodos(node.getChildNodes(), nodeNameToReader, metodo);
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
	//		cliente SIN SSL es un cliente normal sin seguridad
	//*********************************************************************************************************************************/

	/**
	 * Invoke a SOAP call passing in an operation instance and attachments
	 * 
	 * @param operation
	 *            {@link Metodos} The selected operation
	 * @param attachements
	 *            The required attachments
	 * 
	 * @return The response SOAP Envelope as a String
	 * @throws WSDLException
	 *             algun problema en la interpretacion e invocacion de los
	 *             servicios
	 */
	public Document invokeOperation(Metodos operation,File[] attachments)
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
			return client.send(url);
			
			
		}
		catch (Exception e)
		{
			logger.error("Problemas al invocar el metodo Sin seguridad", e);
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

		logger.info("Generando la salida con seguridad");
		
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
	private Document talk(Metodos metodo) throws Exception {
		Document doc = null;
		try {
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage msg = messageFactory.createMessage(
					new MimeHeaders(),
					new ByteArrayInputStream(metodo.getInputMessageText().getBytes(Charset.forName("UTF-8"))));

			// View input
			//msg.writeTo(System.out);

			// Trust to certificates
			doTrustToCertificates();

			logger.info("Enviando el mensaje");
			// SOAPMessage rp = conn.call(msg, urlval);
			SOAPMessage rp = sendMessage(msg, metodo.getEndPoint());

			// View the output
			//rp.writeTo(System.out);
			logger.info("Respuesta recibida");
			logger.info(rp.toString());
			return toDocument(rp);
			
			
			
			//lecturaCompleta(doc, metodo);
		} 
		catch (Exception e) 
		{
			logger.error("Error al nvocar con seguridad " + e.getMessage());
			LogDepuracion objGuardar = new LogDepuracion();
			objGuardar.setNumero(getParametrosData().get("movil"));
			objGuardar.setRespuesta( doc==null ? e.getMessage() : doc.getTextContent() );
			objGuardar.setEstadoTransaccion("Error al procesar");
			objGuardar.setFechaTransaccion(new Date());
			objGuardar.setMetodo(metodo);
			objGuardar.setEnvio(metodo.getInputMessageText());
			objGuardar.setTipoTransaccion(getTipoDepuracion());
			objGuardar.setUsuarioSistema(getUsuarioSistema());
			
			getEjecucion().createData(objGuardar);
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
	 * Metodo para la transformación de un {@link Document} Response a un
	 * {@link String} y hacer mas facil la busqueda del dato
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param soapMsg
	 *            {@link SOAPMessage}
	 * @return {@link Document}
	 * */
	public String getStringFromDocument(Document doc)
	{
	    try
	    {
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       return writer.toString();
	    }
	    catch(TransformerException ex)
	    {
	       ex.printStackTrace();
	       return null;
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

	/**
	 * @return the usuarioSistema
	 */
	public UsuarioSistema getUsuarioSistema() {
		return usuarioSistema;
	}

	/**
	 * @param usuarioSistema the usuarioSistema to set
	 */
	public void setUsuarioSistema(UsuarioSistema usuarioSistema) {
		this.usuarioSistema = usuarioSistema;
	}

	/**
	 * @return the ejecucion
	 */
	private BdEjecucion getEjecucion() {
		return ejecucion;
	}

	/**
	 * @return the tipoDepuracion
	 */
	public String getTipoDepuracion() {
		return tipoDepuracion;
	}

	/**
	 * @param tipoDepuracion the tipoDepuracion to set
	 */
	public void setTipoDepuracion(String tipoDepuracion) {
		this.tipoDepuracion = tipoDepuracion;
	}
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
	 * *//*
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
	}*/

/*
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
		System.out.println("Procesando... " + metodo.getMetodo());
		//llenar los parametros para los metodos web.
		llenarParametros(movil, metodo);
		
		// este seria primero ejecutar la baja de la lista negra
		// luego consultar el historial de servicios 
		// buscar en ese historial de servicios los que esten activos 
		// y de esos activos seria ejecutar la baja para cada uno de dichos servicios
		
		//verificar el debug de los logger
		logger.info(">>>> " + tamanio);
		logger.info(">>>> " + ordenEjecucion);
		
		//verificar esto en contra del tamaño de la lista  de metodo para hacer un bucle hasta ejecutarse en el orden deseado
		if(ordenEjecucion == metodo.getMetodo() && ordenEjecucion <= tamanio)
		{
			if (metodo.getParametros() != null) 
			{
				for (Parametros parametro : metodo.getParametros()) 
				{
					metodo.setInputMessageText(metodo.getInputMessageText().replace(("_*" + parametro.getNombre() + "_*").toString() , (getParametrosData().get(parametro.getNombre())==null?"":getParametrosData().get(parametro.getNombre()))  ));
				}
			}
			
			//con el resultado debo llenar mas el getParametrosData().put("algo debo poner sgun paamerizacion de respuesta", respuesta.getposiciion);
			logger.info("Seguridad " + metodo.getSeguridad());
			
			logger.info(metodo.getInputMessageText());
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
			if(ordenEjecucion>tamanio)
				seguir = false;
		}
	}
}*/