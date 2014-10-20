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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.LogDepuracion;
import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.entidades.Parametros;
import sv.avantia.depurador.agregadores.entidades.ParametrosSistema;
import sv.avantia.depurador.agregadores.entidades.Respuesta;
import sv.avantia.depurador.agregadores.entidades.ResultadosRespuesta;
import sv.avantia.depurador.agregadores.entidades.UsuarioSistema;
import sv.avantia.depurador.agregadores.jdbc.BdEjecucion;

import com.cladonia.xml.webservice.soap.SOAPClient;
import com.cladonia.xml.webservice.wsdl.WSDLException;
import com.cladonia.xml.webservice.wsdl.XMLSupport;

public class ConsultaAgregadorPorHilo extends Thread {
	
	/**
	 * construct default
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	public ConsultaAgregadorPorHilo(){
		
	}
	
	/**
	 * construct
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param name
	 * */
	public ConsultaAgregadorPorHilo(String name){
		super.setName(name);
	}

	/**
	 * Bandera para saber si debemos guardar el la ejecucion de la consulta o no
	 * */
	private boolean guardarConsulta=true;
	
	/**
	 * Valor con el que se efectuara el timeOut Excepcion
	 * */
	private long timeOutMillisecond=5000;
	
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
			logger.info("=======================	SE CONSULTARA EL AGREGADOR " + getAgregador().getNombre_agregador() + "	=======================" );
			procesarServiciosWeb();
		} catch (Exception e) {
			logger.error("ERROR DE EJECUCION DENTRO DE LA DEPURACION DEL AGREGADOR " + getAgregador().getNombre_agregador() + e.getMessage(), e);
			LogManager.shutdown();
			this.interrupt();
		} finally{
			logger.info("======== SE TERMINO DE EJECUTAR EL HILO PARA EL AGREGADOR " + getAgregador().getNombre_agregador() + " ========");
			//LogManager.shutdown();
			//this.interrupt();
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
		logger.debug("SE DEPURARA EL NUMERO " + movil + " Se ejecutaran " + getAgregador().getMetodos().size() + " metodos el " );
		
		if(getAgregador().getMetodos().size()>0)
		{
			//ordernar Los metodos web por su tipo de ejecucion
			for (Metodos metodoX : getAgregador().getMetodos()) {
				System.out.println(metodoX.getMetodo());
				if(metodoX.getMetodo()==1)
					setListaNegra(metodoX);
				if(metodoX.getMetodo()==2)
					setConsulta(metodoX);
				if(metodoX.getMetodo()==3)
					setBaja(metodoX);
			}

			//llenar los parametros para los metodos web.
			llenarParametros(movil);
			
			//************* LISTA NEGRA ***************//
			if(getListaNegra()!=null)
			{
				logger.debug("SE EJECUTARA LA BAJA EN LISTA NEGRA PARA " + getAgregador().getNombre_agregador());
				try {
					lecturaCompleta(ejecucionMetodo(getListaNegra()), getListaNegra(), 1);
				} catch (Exception e) {
					logger.error("Se ejecuto un error al consultar la lista negra");
				}
				
			}
			
			//********* CONSULTA DE SERVICIOS **********//
			if(getConsulta()!=null)
			{
				logger.debug("SE EJECUTARA LA CONSULTA DE SERVICIOS PARA " + getAgregador().getNombre_agregador());
				try {
					lecturaCompleta(ejecucionMetodo(getConsulta()), getConsulta(), 2);
				} catch (Exception e) {
					logger.error("Se ejecuto un error al realizar la consulta de servicios");
				}
				
			}else{
				if(getBaja()!=null)
				{
					//esta opcion es solo para las bajas que no necesitan pasar por una consulta como por ejemplo el SMT
					logger.debug("SE EJECUTARA LA BAJA DE SERVICIOS PARA " + getAgregador().getNombre_agregador());
					try {
						lecturaCompleta(ejecucionMetodo(getBaja()), getBaja(), 1);
					} catch (Exception e) {
						logger.error("Se ejecuto un error al realizar la baja de servicios sin pasar por la consulta previamente");
					}
				}
				
			}
			
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
		List<ParametrosSistema> parametrosSistemas = (List<ParametrosSistema>) (List<?>)getEjecucion().listData("FROM SDA_PARAMETROS_SISTEMA");
		for (ParametrosSistema parametrosSistema : parametrosSistemas) {
			if(parametrosSistema.getDato().equals("timeOutWebServices"))
			{
				timeOutMillisecond = new Long(parametrosSistema.getValor()).longValue();
			}
			else
			{
				getParametrosData().put(parametrosSistema.getDato(), parametrosSistema.getValor());
			}
			
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
		if(nonce!=null && timestamp !=null && pass != null){
			String concatenacion = nonce.concat(timestamp).concat(pass);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(concatenacion.getBytes());
			return Base64.encode(md.digest());
		}else{
			//logger.debug(getAgregador().getNombre_agregador());
			//logger.debug("No se generara contraraseña porque Se obtubo nonce " + nonce + " time " +  timestamp  + " pass "+  pass );
			return "";
		}
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
	private synchronized Document ejecucionMetodo(Metodos metodo) {
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
		
		try {
			// primero verificamos SI es de tipo asmx
			if(metodo.getEndPoint().endsWith("asmx")){
				return asmx(metodo);
			}
			
			// si NO es asmx y no tiene NO es por https
			if (metodo.getSeguridad() == 0) 
			{
				return invokeOperation(metodo, null);
			}
			
			// si NO es asmx y no tiene SI es por https
			if (metodo.getSeguridad() == 1) 
			{
				return talk(metodo);
			}
		} catch (Exception e) {
			//logger.error(getAgregador().getNombre_agregador() + " ERROR AL INVOCAR AL INVOCAR EL METODO " + metodo.getMetodo());
        	//guardarRespuesta(metodo, "", "Error " , "ERROR AL INVOCAR EL METODO " + metodo.getMetodo());
        	return xmlError(getAgregador().getNombre_agregador() + " ERROR AL INVOCAR AL INVOCAR EL METODO " + metodo.getMetodo());
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
	private void lecturaCompleta(Document doc, Metodos metodo, int lectura) 
	{
		logger.debug("Mensaje Enviado:");
		logger.debug(metodo.getInputMessageText());
		
		if(doc==null){
			logger.error(getAgregador().getNombre_agregador() + " No se obtuvo data en la respuesta recibida...");
			guardarRespuesta(metodo, getStringFromDocument(doc), "Error", "Al recibir la respuesta no traia datos para procesar, Favor revisar el log de respuesta para verificar lo recibido");
			return;
		}
		
		if(doc.getDocumentElement()==null){
			logger.error(getAgregador().getNombre_agregador() + "No se obtuvo data en la respuesta recibida...");
			guardarRespuesta(metodo, getStringFromDocument(doc), "Error", "Al recibir la respuesta no traia datos para procesar, Favor revisar el log de respuesta para verificar lo recibido");
			return;
		}

		doc.getDocumentElement().normalize();
		
		if(doc.getDocumentElement().getNodeName().equals("errorSDA")){
			logger.error(getStringFromDocument(doc));
			guardarRespuesta(metodo, getStringFromDocument(doc), "Error " , "GENERO ERROR EL CONSULTAR EL TIEMPO AL INVOCAR EL METODO SIN SEGURIDAD");
			return;
		}else{
			logger.debug("Mensaje Recibido:");
			logger.debug(getStringFromDocument(doc));
			
			for (Respuesta respuesta : metodo.getRespuestas()) 
			{
				if (doc.getDocumentElement().hasChildNodes()) 
				{
					NodeList nodeList = doc.getDocumentElement().getChildNodes();
					if(lectura==1)
					{
						lecturaListadoNodos1(nodeList, respuesta, metodo, getStringFromDocument(doc));
						if(!validateBrowserResponse)
						{
							lecturaListadoNodos3(nodeList, metodo, getStringFromDocument(doc));
							if(!validateBrowserResponse)
							{
								logger.warn(getAgregador().getNombre_agregador() + "No se encontro el tag " + respuesta.getNombre() );
								guardarRespuesta(metodo, getStringFromDocument(doc), "Error", "No se encontro el valor parametrizado dentro de la respuesta recibida");
							}
							else
							{
								validateBrowserResponse = false;
							}						
						}
						else
						{
							validateBrowserResponse = false;
						}
					}
					if(lectura==2){
						lecturaListadoNodos2(nodeList, respuesta.getNombre());
						if(guardarConsulta){
							guardarRespuesta(getConsulta(), getStringFromDocument(doc), "Sin Servicios", "Se proceso la consulta pero no existian servicios activos");
						}
						guardarConsulta=true;//es por si en la ejecucion de la baja de servicios me lo habian convertido a false
					}
				}
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
	private void lecturaListadoNodos1(NodeList nodeList, Respuesta respuesta, Metodos metodo, String response) 
	{
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				if(node.getNodeName()!=null){
					if(node.getNodeName().equalsIgnoreCase(respuesta.getNombre()))
					{
						for (ResultadosRespuesta resultados : respuesta.getResultadosRespuestas()) 
						{
							if(node.getTextContent()!=null)
							{
								if(node.getTextContent().equals(resultados.getDato()))
								{
									guardarRespuesta(metodo, response, resultados.getValor(), "Se proceso de forma satisfactoria la consulta requerida");
									validateBrowserResponse = true;
								}
							}	
						}						
					}
				}
				
				if (node.hasChildNodes())
					lecturaListadoNodos1(node.getChildNodes(), respuesta, metodo, response);
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
	private void lecturaListadoNodos2(NodeList nodeList, String nodeNameToReader) {
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				if(node.getNodeName().equalsIgnoreCase(nodeNameToReader))
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
									//Guardamos la respuesta de la consulta siempre y cuando haya un servicio que bajar si no no guardara nada en la base de datos
									//guardarRespuesta(getConsulta(), respuesta, "Exito", "Se proceso de forma satisfactoria la solicitud para dar de baja el servicio");
									guardarConsulta = false;
									if(getBaja()!=null){
										logger.debug("SE EJECUTARA LA BAJA DE SERVICIOS PARA " + getAgregador().getNombre_agregador());
										try {
											lecturaCompleta(ejecucionMetodo(getBaja()), getBaja(), 1);
										} catch (Exception e) {
											logger.error("Se ejecuto un error al realizar la baja de servicios se consulto previamente");
										}
										
									}
								}
							}
							
						}
						
						indiceServicios ++;
						indice = 1;
					}else{
						if(indice==1)
						{
							getParametrosData().put("servicio", (node.getFirstChild().getNodeValue()==null?"":node.getFirstChild().getNodeValue().trim()));
							logger.debug("El Servicio " + getParametrosData().get("servicio"));
						}
						if(indice==2)
						{
							getParametrosData().put("servicioActivado", (node.getFirstChild().getNodeValue()==null?"":node.getFirstChild().getNodeValue().trim()));
							logger.debug("Activado " + getParametrosData().get("servicioActivado"));
						}
						if(indice==5)
						{
							getParametrosData().put("marcacion", (node.getFirstChild().getNodeValue()==null?"":node.getFirstChild().getNodeValue().trim()));
							logger.debug("Marcacion Corta " + getParametrosData().get("marcacion"));
						}
						indice++;
						
						//Esto quiere decir que si las respuesta no vienen en nodos diferentes esto hara que se reinicie el contador 
						//como es con el agregador MOVIXLA
						if(indice==12){
							indice=1;
						}
					}
				}
				
				//recursive
				if (node.hasChildNodes())
					lecturaListadoNodos2(node.getChildNodes(), nodeNameToReader);
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
	private void lecturaListadoNodos3(NodeList nodeList, Metodos metodo, String response) 
	{
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				if(node.getNodeName()!=null){
					if(node.getNodeName().equalsIgnoreCase("faultstring"))
					{
						if(node.getTextContent()!=null)
						{
							logger.debug(getAgregador().getNombre_agregador() + " Error " + node.getTextContent());
							guardarRespuesta(metodo, response, "Error", "Se proceso recibiendo un error en el mensaje detallado de la siguiente manera " + node.getTextContent());
							validateBrowserResponse = true;
						}						
					}
				}
				
				if (node.hasChildNodes())
					lecturaListadoNodos3(node.getChildNodes(), metodo, response);
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
	private void guardarRespuesta(Metodos metodo, String respuesta, String estado, String descripcionEstado){
		LogDepuracion objGuardar = new LogDepuracion();
		objGuardar.setNumero(getParametrosData().get("movil"));
		objGuardar.setEstadoTransaccion(estado);
		objGuardar.setFechaTransaccion(new Date());
		objGuardar.setMetodo(metodo);
		objGuardar.setEnvio(metodo.getInputMessageText());
		objGuardar.setRespuesta(respuesta);
		objGuardar.setTipoTransaccion(getTipoDepuracion());
		objGuardar.setUsuarioSistema(getUsuarioSistema());
		objGuardar.setDescripcionEstado(descripcionEstado);
		
		logger.debug("SE GUARDARA RESPUESTA EN LA BASE DE DATOS...");
		getEjecucion().createData(objGuardar);
	}

	private Document xmlError(String error){
		String xml = "<errorSDA>" + (error==null?"":error) +"</errorSDA>";
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = builderFactory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8")))  );
			return doc;
		} catch (Exception e) {
			logger.error("No se puede generarel mensaje de error");
			return null;
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
	//		cliente ASMX para las implementaciones con servicios web montados en windows 
	//*********************************************************************************************************************************/

	/**
	 * Metodo para la invocacion de los servicios ASMX
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param operation un bjeto {@link Metodos}
	 * @return {@link Document}
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IllegalStateException
	 * */
	@SuppressWarnings({ "resource", "deprecation" })
	private Document asmx(Metodos operation) throws ClientProtocolException,
			IOException, IllegalStateException, SAXException,
			ParserConfigurationException {
		
		HttpClient httpClient =null;
		HttpPost postRequest =null;
		HttpResponse response = null;
		try{
			
			httpClient = new org.apache.http.impl.client.DefaultHttpClient();
			// Crear la llamada al servidor
			postRequest = new HttpPost(operation.getEndPoint());
			StringEntity input = new StringEntity(operation.getInputMessageText());
			input.setContentType(operation.getContentType());
			postRequest.setEntity(input);
			
	        try {
	        	// Tratar respuesta del servidor
				response = httpClient.execute(postRequest);
	        } catch (Exception e) {
	        	//logger.error(getAgregador().getNombre_agregador() + " ERROR AL INVOCAR EL METODO A  TRAVES DE ASMX");
	        	//guardarRespuesta(operation, "", "Error " , "SERROR AL INVOCAR EL METODO A  TRAVES DE ASMX");
	        	return xmlError(getAgregador().getNombre_agregador() + " ERROR AL INVOCAR EL METODO A  TRAVES DE ASMX");
			}
	        
	        try{
				long endTimeOut = System.currentTimeMillis() + timeOutMillisecond;
				while(true){
					if(System.currentTimeMillis() > endTimeOut){
						//logger.error(getAgregador().getNombre_agregador() + " GENERO TIMEOUT EXCEPCION INVOCAR EL METODO A TRAVES DE ASMX");
						//guardarRespuesta(operation, "", "Error " , "GENERO TIMEOUT EXCEPCION INVOCAR EL METODO A TRAVES DE ASMX");
						return xmlError(getAgregador().getNombre_agregador() + " SE GENERO TIMEOUT EXCEPCION INVOCAR EL METODO A TRAVES DE ASMX");
					}else{	
						if (response != null) {
							break;
						}
					}
				}
			} catch (Exception e) {
				//logger.error(getAgregador().getNombre_agregador() + " GENERO ERROR EL CONSULTAR EL TIEMPO AL INVOCAR EL METODO A TRAVES DE ASMX");
				//guardarRespuesta(operation, "", "Error " , "GENERO ERROR EL CONSULTAR EL TIEMPO AL INVOCAR EL METODO A TRAVES DE ASMX");
				return xmlError(getAgregador().getNombre_agregador() + " GENERO ERROR EL CONSULTAR EL TIEMPO AL INVOCAR EL METODO A TRAVES DE ASMX");
			}
			
			
			// factory read response
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			if (response.getStatusLine().getStatusCode() != 200) {
				//logger.error(getAgregador().getNombre_agregador() + " ERROR AL INVOCAR EL METODO A  TRAVES DE ASMX");
				//guardarRespuesta(operation, getStringFromDocument(factory.newDocumentBuilder().parse(response.getEntity().getContent())), "Error " + response.getStatusLine().getStatusCode(), "Se tuvo un error al recibir la respuesta del web services de asmx");
				logger.error(getStringFromDocument(factory.newDocumentBuilder().parse(response.getEntity().getContent())));
				return xmlError(getAgregador().getNombre_agregador() + " ERROR AL INVOCAR EL METODO A  TRAVES DE ASMX");
			}
			
			// Obtener información de la respuesta
			return factory.newDocumentBuilder().parse(response.getEntity().getContent());
		}
		finally
		{
			// Cierre de la conexión
			if (httpClient != null) httpClient.getConnectionManager().shutdown();
		}
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
	private Document invokeOperation(Metodos operation,File[] attachments)
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
			
			Document response = null;
			
			try 
			{
	        	// Tratar respuesta del servidor
				response = client.send(url);
		    } 
			catch (Exception e) 
			{
				//logger.error(getAgregador().getNombre_agregador() + " ERROR AL INVOCAR EL METODO SIN SEGURIDAD");
		        //guardarRespuesta(operation, "", "Error " , "SERROR AL INVOCAR EL METODO SIN SEGURIDAD");
		        return xmlError(getAgregador().getNombre_agregador() + " ERROR AL INVOCAR EL METODO SIN SEGURIDAD");
			}
		        
	        try{
				long endTimeOut = System.currentTimeMillis() + timeOutMillisecond;
				while(true){
					if(System.currentTimeMillis() > endTimeOut){
						//logger.error(getAgregador().getNombre_agregador() + " GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD");
						//guardarRespuesta(operation, "", "Error " , "GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD");
						return xmlError(getAgregador().getNombre_agregador() + " GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD");
					}else{	
						if (response != null) {
							break;
						}
					}
				}
				return response;
			} catch (Exception e) {
				//logger.error(getAgregador().getNombre_agregador() + " GENERO ERROR EL CONSULTAR EL TIEMPO AL INVOCAR EL METODO SIN SEGURIDAD");
				//guardarRespuesta(operation, "", "Error " , "GENERO ERROR EL CONSULTAR EL TIEMPO AL INVOCAR EL METODO SIN SEGURIDAD");
				return xmlError(getAgregador().getNombre_agregador() + " GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD");
			}			
		}
		catch (Exception e)
		{
			//logger.error(getAgregador().getNombre_agregador() + " ERORO AL INVOCAR EL METODO SIN SEGURIDAD", e);
			return xmlError(getAgregador().getNombre_agregador() + " ERORO AL INVOCAR EL METODO SIN SEGURIDAD");
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

		logger.debug("Generando la salida con seguridad");
		
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
	private Document talk(Metodos metodo) {
		SOAPMessage response = null;
		try {
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage msg = messageFactory.createMessage(
					new MimeHeaders(),
					new ByteArrayInputStream(metodo.getInputMessageText().getBytes(Charset.forName("UTF-8"))));

			// Trust to certificates
			doTrustToCertificates();
			
			try 
			{
				// SOAPMessage rp = conn.call(msg, urlval);
				response = sendMessage(msg, metodo.getEndPoint());
		    } 
			catch (Exception e) 
			{
				//logger.error(getAgregador().getNombre_agregador() + " ERROR AL INVOCAR EL METODO SIN SEGURIDAD");
		        //guardarRespuesta(metodo, "", "Error " , "SERROR AL INVOCAR EL METODO SIN SEGURIDAD");
				if(response != null)
					logger.debug(toDocument(response));
		        return xmlError(getAgregador().getNombre_agregador() + " ERROR AL INVOCAR EL METODO CON SEGURIDAD");
			}
		} 
		catch (Exception e) 
		{
			return xmlError(getAgregador().getNombre_agregador() + " GENERO ERROR AL INVOCAR EL METODO CON SEGURIDAD");
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
	    	logger.error( getAgregador().getNombre_agregador() + " Error al querer pasar el Document a cadena de texto");
	       return "";
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
	 * @throws SOAPException 
	 * @throws UnsupportedOperationException 
	 * @throws IOException 
	 * @throws TransformerException 
	 * @throws TransformerConfigurationException 
	 * */
	private SOAPMessage sendMessage(SOAPMessage message, String endPoint) 
			throws UnsupportedOperationException, SOAPException, TransformerConfigurationException, TransformerException, IOException
	{
		SOAPMessage response = null;
		if (endPoint != null && message != null) 
		{
			
			URL url = new URL(endPoint);
			SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
			SOAPConnection connection = null;
			
			try {
				connection = scf.createConnection(); // point-to-point connection
			} finally {
				if (connection != null) 
				{
					try 
					{
						connection.close();
					} 
					catch (SOAPException soape) 
					{
						logger.error("Can't close SOAPConnection:" + soape , soape);
						return null;
					}
				}
			}
			
			try 
			{
				response = connection.call(message, url);
		    } 
			catch (SOAPException e) 
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
			
			try{
				long endTimeOut = System.currentTimeMillis() + timeOutMillisecond;
				while(true){
					if(System.currentTimeMillis() > endTimeOut){
						logger.error(getAgregador().getNombre_agregador() + " SE GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD");
						throw new SOAPException("timeOutExcepction");
					}else{	
						if (response != null) {
							break;
						}
					}
				}
				return response;
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
		return response;
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