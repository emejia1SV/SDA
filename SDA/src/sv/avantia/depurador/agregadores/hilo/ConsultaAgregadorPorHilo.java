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
import java.util.Calendar;
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
import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.entidades.Parametros;
import sv.avantia.depurador.agregadores.entidades.ParametrosSistema;
import sv.avantia.depurador.agregadores.entidades.Respuesta;
import sv.avantia.depurador.agregadores.entidades.UsuarioSistema;
import sv.avantia.depurador.agregadores.jdbc.BdEjecucion;

import com.cladonia.xml.webservice.soap.SOAPClient;
import com.cladonia.xml.webservice.wsdl.WSDLException;
import com.cladonia.xml.webservice.wsdl.XMLSupport;

public class ConsultaAgregadorPorHilo extends Thread {

	/**
	 * Instancia del {@link Metodos} para la lista Negra
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private Metodos listaNegra = null;
	
	/**
	 * Instancia del {@link Metodos} para la Consulta de Servicios 
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private Metodos consulta = null;
	
	/**
	 * Instancia del {@link Metodos} para Dar de Baja Los Servicios
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private Metodos baja = null;
	
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
		try {
			logger.info("=======================	SE CONSULTARA UN AGREGADOR	==============================" );
			procesarServiciosWeb();
		} catch (Exception e) {
			logger.error("ERROR DE EJECUCION DENTRO DE LA DEPURACION DEL AGREGADOR " + getAgregador().getNombre_agregador() + e.getMessage(), e);
			this.interrupt();
		} finally{
			logger.info("======== SE TERMINO DE EJECUTAR EL HILO PARA EL AGREGADOR " + getAgregador().getNombre_agregador() + " ========");
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
		logger.info("SE DEPURARA EL NUMERO " + movil);
		
		if(getAgregador().getMetodos().size()>0)
		{
			//ordernar Los metodos web por su tipo de ejecucion
			for (Metodos metodoX : getAgregador().getMetodos()) {
				if(metodoX.getMetodo()==1)
					setListaNegra(metodoX);
				if(metodoX.getMetodo()==2)
					setConsulta(metodoX);
				if(metodoX.getMetodo()==3)
					setBaja(metodoX);
			}
			
			//************* LISTA NEGRA ***************//
			//llenar los parametros para los metodos web.
			llenarParametros(movil);
			
			logger.info("SE EJECUTARA LA BAJA EN LISTA NEGRA");
			// ejecutamos el primer metodo de lista negra
			lecturaCompleta(ejecucionMetodo(getListaNegra()), getListaNegra(), 1);

			//********* CONSULTA DE SERVICIOS **********//
			logger.info("SE EJECUTARA LA CONSULTA DE SERVICIOS");
			lecturaCompleta(ejecucionMetodo(getConsulta()), getConsulta(), 2);
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
	private void llenarParametros(String movil) throws NoSuchAlgorithmException {
		
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
		getParametrosData().put("pass", getListaNegra().getContrasenia());
		getParametrosData().put("user", getListaNegra().getUsuario());
		getParametrosData().put("passSMT", contraseniaSMT(getParametrosData().get("nonce"), getParametrosData().get("dateSMT"), getListaNegra().getContrasenia()));
		
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
	private synchronized Document ejecucionMetodo(Metodos metodo) throws Exception{
		if (metodo.getParametros() != null) 
		{
			for (Parametros parametro : metodo.getParametros()) 
			{
				metodo.setInputMessageText(metodo.getInputMessageText().replace(("_*" + parametro.getNombre() + "_*").toString() , (getParametrosData().get(parametro.getNombre())==null?"":getParametrosData().get(parametro.getNombre()))  ));
			}
		}
		
		if(metodo.getMetodo()==3)
		{
			getParametrosData().remove("servicioActivado");
			getParametrosData().remove("servicio");
			getParametrosData().remove("marcacion");
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

	private boolean validateBrowserResponse = false;
	private int indiceServicios = 0;
	private int indice = 1;
	
	/**
	 * Metodo que recibe el documento Soap Response y este hace la lectura de la
	 * lista de nodos que obtiene para procesarlos y buscar la {@link Respuesta}
	 * deseada
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param doc {@link Document}
	 * @param metodo {@link Metodos}
	 * @return {@link Void}
	 * @throws Exception 
	 * */
	private void lecturaCompleta(Document doc, Metodos metodo, int lectura) throws Exception 
	{
		logger.info("Mensaje Enviado:");
		logger.info(metodo.getInputMessageText());
		
		logger.info("Mensaje Recibido:");
		logger.info(getStringFromDocument(doc));
		
		doc.getDocumentElement().normalize();
		for (Respuesta respuesta : metodo.getRespuestas()) 
		{
			if (doc.getDocumentElement().hasChildNodes()) 
			{
				NodeList nodeList = doc.getDocumentElement().getChildNodes();
				if(lectura==1)
				{
					lecturaListadoNodos1(nodeList, respuesta.getNombre(), metodo, getStringFromDocument(doc));
					if(!validateBrowserResponse)
					{
						logger.warn("No se encontro el tag " + respuesta.getNombre() );
						guardarRespuesta(metodo, getStringFromDocument(doc), "Error");
					}
					else
					{
						validateBrowserResponse = false;
					}
				}
				if(lectura==2)
					lecturaListadoNodos2(nodeList, respuesta.getNombre(), getStringFromDocument(doc));
				
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
	private void lecturaListadoNodos1(NodeList nodeList, String nodeNameToReader, Metodos metodo, String respuesta) {
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				if(node.getNodeName().equals(nodeNameToReader))
				{
					if(node.getTextContent().equals("1"))
						guardarRespuesta(metodo, respuesta, "Exito");
					if(node.getTextContent().equals("0"))
						guardarRespuesta(metodo, respuesta, "Fallo");
					
					
					validateBrowserResponse = true;
				}
				
				if (node.hasChildNodes())
					lecturaListadoNodos1(node.getChildNodes(), nodeNameToReader, metodo, respuesta);
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
	 * @return {@link Void}
	 * @throws Exception 
	 * */
	private void lecturaListadoNodos2(NodeList nodeList, String nodeNameToReader, String respuesta) throws Exception {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if(node.getNodeName().equals(nodeNameToReader))
				{
					if(node.getFirstChild()==null || node.getFirstChild().getNodeValue()==null)
					{
						if(indiceServicios>0)
						{
							//********* BAJA DE SERVICIOS **********//
							if(getParametrosData().containsKey("servicioActivado"))
							{
								if(getParametrosData().get("servicioActivado").equals("1"))
								{
									//Guardamos la respuesta de la consulta
									guardarRespuesta(getConsulta(), respuesta, "Exito");
									
									//ejecutamos el metodo de bajar servicios activos
									lecturaCompleta(ejecucionMetodo(getBaja()), getBaja(), 1);
								}
							}
						}
						indiceServicios ++;
						indice = 1;
					}else{
						if(indice==1)
						{
							getParametrosData().put("servicio", (node.getFirstChild().getNodeValue()==null?"":node.getFirstChild().getNodeValue().trim()));
							logger.info("El Servicio " + getParametrosData().get("servicio"));
						}
						if(indice==2)
						{
							getParametrosData().put("servicioActivado", (node.getFirstChild().getNodeValue()==null?"":node.getFirstChild().getNodeValue().trim()));
							logger.info("Activado " + getParametrosData().get("servicioActivado"));
						}
						if(indice==5)
						{
							getParametrosData().put("marcacion", (node.getFirstChild().getNodeValue()==null?"":node.getFirstChild().getNodeValue().trim()));
							logger.info("Marcacion Corta " + getParametrosData().get("marcacion"));
						}
						indice++;
					}
				}
				
				//recursive
				if (node.hasChildNodes())
					lecturaListadoNodos2(node.getChildNodes(), nodeNameToReader, respuesta);
			}
		}
	}
	
	/**
	 * Metodo que servira para unificar el guardado de respuesta
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param metodo
	 * @param respuesta
	 * @param estado
	 * @return {@link Void}
	 * */
	private void guardarRespuesta(Metodos metodo, String respuesta, String estado){
		LogDepuracion objGuardar = new LogDepuracion();
		objGuardar.setNumero(getParametrosData().get("movil"));
		objGuardar.setEstadoTransaccion(estado);
		objGuardar.setFechaTransaccion(new Date());
		objGuardar.setMetodo(metodo);
		objGuardar.setEnvio(metodo.getInputMessageText());
		objGuardar.setRespuesta(respuesta);
		objGuardar.setTipoTransaccion(getTipoDepuracion());
		objGuardar.setUsuarioSistema(getUsuarioSistema());
		
		logger.info("SE GUARDARA RESPUESTA EN LA BASE DE DATOS...");
		getEjecucion().createData(objGuardar);
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

	/**
	 * @return the listaNegra
	 */
	private Metodos getListaNegra() {
		return listaNegra;
	}

	/**
	 * @param listaNegra the listaNegra to set
	 */
	private void setListaNegra(Metodos listaNegra) {
		this.listaNegra = listaNegra;
	}

	/**
	 * @return the consulta
	 */
	private Metodos getConsulta() {
		return consulta;
	}

	/**
	 * @param consulta the consulta to set
	 */
	private void setConsulta(Metodos consulta) {
		this.consulta = consulta;
	}

	/**
	 * @return the baja
	 */
	private Metodos getBaja() {
		return baja;
	}

	/**
	 * @param baja the baja to set
	 */
	private void setBaja(Metodos baja) {
		this.baja = baja;
	}
}