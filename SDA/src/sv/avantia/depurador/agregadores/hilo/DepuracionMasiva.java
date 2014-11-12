package sv.avantia.depurador.agregadores.hilo;

import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cladonia.xml.webservice.wsdl.WSDLException;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.LogDepuracion;
import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.entidades.Parametros;
import sv.avantia.depurador.agregadores.entidades.ParametrosSistema;
import sv.avantia.depurador.agregadores.entidades.Respuesta;
import sv.avantia.depurador.agregadores.entidades.ResultadosRespuesta;
import sv.avantia.depurador.agregadores.entidades.UsuarioSistema;
import sv.avantia.depurador.agregadores.jdbc.BdEjecucion;
import sv.avantia.depurador.agregadores.utileria.ErroresSDA;

public class DepuracionMasiva {

	/**
	 * La constante que se enviara como estado de la tansaccion fallida
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private static String ESTADO_ERROR = "ERROR";
	
	/**
	 * La constante que se enviara como estado de la tansaccion cuando no ha
	 * surgido error pero tampoco ejecuto el flujo normal esperado
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private static String ESTADO_WARN = "VERIFICAR";
	
	/**
	 * La constante que se enviara como estado de la tansaccion cuando no se
	 * hayan encontrado servicios para dar de baja dentro de la consulta de
	 * servicios activos
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private static String SIN_SERVICIOS = "SIN SERVICIOS";
	
	/**
	 * Instancia del insumo {@link Agregadores} que se espera recibir y se espera nunca llegue a
	 * esta instancia nulo y este es el que obtendra todo el insumo de la
	 * parametrizacion para la consulta a los agregadores
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private Agregadores agregador = null;
	
	/**
	 * Instancia del insumo {@link List} de {@link String} donde se espera
	 * recibir un listado de numeros para depurar
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private List<String> moviles = null;
	
	/**
	 * Obtener el appender para la impresión en un archivo de LOG
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private static Logger logger = Logger.getLogger("avantiaLogger");
	
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
	 * Instancia de un {@link HashMap} para mantener en memoria los parametros
	 * con los que me serviran de insumo para llenar los parametros requeridos
	 * por los agregadores
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private HashMap<String, String> parametrosData = null;
	
	/**
	 * Valor con el que se efectuara el timeOut Excepcion solo que este dato es
	 * solo para inicializacion y evitando se tenga problemas a obtenerlo desde
	 * la base de datos porque este dato es sobre escrito desde el valor que
	 * esta parametrizado en {@link ParametrosSistema}
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private long timeOutMillisecond=5000;
	
	/**
	 * Instancia de la Clase {@link BdEjecucion} que maneja los tipos de
	 * transacciones, que podemos realizar contra la base de datos.
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private BdEjecucion ejecucion = new BdEjecucion();
	
	/**
	 * Bandera para saber si debemos guardar el la ejecucion de la consulta o no
	 * */
	private boolean guardarConsulta=true;
	
	/***/
	private boolean validateBrowserResponse = false;
	
	/**
	 * Este parametro mantiene en memoria el mesaje de envio para despues de una primer baja
	 * */
	private String mensajeEnvioBajaOriginal;
	
	/**
	 * Este dato indica el indice del recorrido de parametros que se estan leyendo en una consulta sabiendo de antemano que nos daran 11 valores por servicio en la consulta
	 * */
	private int indice = 1;
	
	/**
	 * Instancia que mantiene en memoria el tipo de depuracion que se esta
	 * realizando Tipo de depuracion ARCHIVO MASIVA UNITARIA
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private String tipoDepuracion;
	
	/**
	 * Instancia del usuario del sistema que esta ejecutando el proceso en este momento.
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private UsuarioSistema usuarioSistema = null;
	
	/**
	 * Objeto respuesta
	 * */
	private List<LogDepuracion> respuestas;
	
	/**
	 * Seccion de lectura de los numeros de telefono
	 * @author Edwin Mejia - Avantia Consultores
	 * @throws Exception 
	 * 
	 * */
	
	public HashMap<String, List<LogDepuracion>> procesarDepuracion()
	{				
		// iniciamos el listado de respuestas 
		setRespuestas(new ArrayList<LogDepuracion>());
		
		//validamos que el agregador obtenido en el hilo no sea nulo
		if (getAgregador() != null) 
		{
			//validamos que tenga metodos parametrizados dicho agregador
			if (getAgregador().getMetodos() != null) 
			{
				//validamos que tengamos por lo menos un numero para procesar
				if(getMoviles()==null || getMoviles().size()<1)
				{
					guardarRespuesta(null, null, ESTADO_ERROR, "Dentro de la depuración masiva no se han recibido números para depurar", null);
				}
				else
				{
					for (String movil : getMoviles()) 
					{
						//iniciamos el listado de parametros con los que se trataran los mensajes de envio para los servicios web
						//setParametrosData(new HashMap<String, String>());
						
						//colocamos el numero en una lista en memoria para que este listo por cualquier exepciones
						getParametrosData().put("movil", movil);
						
						//hacemos las depuraciones por cada número obtenido
						invocacionPorNumero(movil);
					}
				}
			}
			else
			{
				//No hay metodos que procesar- problema en parametrizacion o en la obtencion de parametrizacion nivel anterior 
				guardarRespuesta(null, null, ESTADO_ERROR, "Dentro de la depuración masiva No hay metodos que procesar- problema en parametrizacion o en la obtencion de parametrizacion nivel anterior", null);
			}
		}
		else
		{
			//no se ha recibido agregador para depurar por lo que no hay nada que hacer - el problema estuvo en el envio del agregador a depurar en el nivel anterior
			guardarRespuesta(null, null, ESTADO_ERROR, "Dentro de la depuración masiva no se ha recibido agregador para depurar por lo que no hay nada que hacer - el problema estuvo en el envio del agregador a depurar en el nivel anterior", null);
		}
		
		//retornamos las respuestas
		HashMap<String, List<LogDepuracion>> respuestaOut = new HashMap<String, List<LogDepuracion>>();
		respuestaOut.put(getAgregador().getNombre_agregador(), getRespuestas());
		return respuestaOut;
	}
	
	/**
	 * Ejecucines de depuraciones por numero de telefonia movil
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param movil {@link String} numero de celular procesado
	 * @return {@link String}
	 * */
	private void invocacionPorNumero(String movil)
	{
		//se extraen los metodos esperados 
		//ya que llevan un orden de ejecucion y solo se esperan tres tipos de metodos
		for (Metodos metodoX : getAgregador().getMetodos()) 
		{
			if(metodoX.getMetodo()==1)
				setListaNegra(metodoX);
			if(metodoX.getMetodo()==2)
				setConsulta(metodoX);
			if(metodoX.getMetodo()==3){
				setBaja(metodoX);
				
				//mantiene en memoria para el mensaje original
				mensajeEnvioBajaOriginal = new String( getBaja().getInputMessageText() );
			}
		}

		//llenar los parametros para los metodos web.
		try {
			llenarParametros(movil);
		} catch (Exception e) {
			guardarRespuesta(null, null, ESTADO_ERROR, ErroresSDA.ERROR_AL_LLENAR_LOS_PARAMETROS_DE_COMUNICACION_CON_LOS_AGREGADORES.getDescripcion(), null);
		}
		
		logger.debug("SE DEPURARA EL NUMERO " + movil + 
				" EN LOS " + getAgregador().getMetodos().size() + " METODOS PARAMETRIZADOS " +
				(getListaNegra()!=null && getTipoDepuracion().equals("Alta Servicio Web")?" ALTA EN LISTA NEGRA":"")+
				(getListaNegra()!=null?" LISTA NEGRA":"")+
				(getConsulta()!= null?" CONSULTA DE SERVICIOS":"")+
				(getBaja()!=null?" BAJAR SERVICIOS ACTIVOS":""));
		
		// ha esta altura no debe existir una propagacion de error porque aun no nos hemos comunicado con ningun servicio 
		// y cualquier excepcion de comunicacion debe ser tratada en el lugar donde se obtiene la excepcion
		// ejecutamos los metodos de acuerdo al orden establecido
		
		//************* LISTA NEGRA ***************//
		if(getListaNegra()!=null)
		{
			logger.debug("SE EJECUTARA LA BAJA EN LISTA NEGRA PARA EL AGREGADOR " + getAgregador().getNombre_agregador());
			lecturaMetodoWeb(getListaNegra(), 1);
		}
		// Si es la peticion de alta por servicio web solo debe ejecutar la accion anterior y terminar - por reutilizacion de codigo esta asi
		if(getTipoDepuracion().equals("Alta Servicio Web"))
			return;
		
		//********* CONSULTA DE SERVICIOS **********//
		if(getConsulta()!=null)
		{
			logger.debug("SE EJECUTARA LA CONSULTA DE SERVICIOS PARA " + getAgregador().getNombre_agregador());
			lecturaMetodoWeb(getConsulta(), 2);
		}else{
			if(getBaja()!=null)
			{
				//esta opcion es solo para las bajas que no necesitan pasar por una consulta como por ejemplo el CDC
				logger.debug("SE EJECUTARA LA BAJA DE SERVICIOS PARA " + getAgregador().getNombre_agregador());
				lecturaMetodoWeb(getBaja(), 1);
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
	private void llenarParametros(String movil) throws NoSuchAlgorithmException 
	{	
		getParametrosData().put("movil", movil);
		
		//trato especial porque debemos convertirlo a long desde el string obtenido
		setTimeOutMillisecond(new Long(getParametrosData().get("timeOutWebServices")));
		
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
	private String contraseniaSMT(String nonce, String timestamp, String pass) 
			throws NoSuchAlgorithmException
	{
		if(nonce!=null && timestamp !=null && pass != null)
		{
			String concatenacion = nonce.concat(timestamp).concat(pass);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(concatenacion.getBytes());
			return Base64.encode(md.digest());
		}
		return "";
	}
	
	/**
	 * Metodo que recibe el documento Soap Response y este hace la lectura de la
	 * lista de nodos que obtiene para procesarlos y buscar la {@link Respuesta}
	 * deseada
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param metodo {@link Metodos}
	 * @param lectura - este parametro espera saber la forma en como leera la respuesta del metodo web 
	 * @return {@link Void}
	 * */
	private void lecturaMetodoWeb(Metodos metodo, int lectura) 
	{
		//en este momento se envia a realizar la invocacion del metodo web.
		Document doc = invocarMetodoWeb(metodo);
		
		// dificilmente se pudiera dar pero por si se escapo algo se dejo esta validacion
		if(doc==null){
			logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_NULLPOINTEREXCEPTION.getDescripcion() +  " No se obtuvo data en la respuesta recibida...", new Exception("verificar porque no se recibio nada de data despues de haber invocado al metodo web"));
			guardarRespuesta(metodo, doc, ESTADO_ERROR, ErroresSDA.ERROR_NULLPOINTEREXCEPTION.getDescripcion(), metodo.getRespuestas().iterator().next());
			return;
		}
		
		try 
		{
			logger.debug("Mensaje Recibido:");
			logger.debug(com.cladonia.xml.webservice.wsdl.XMLSupport.prettySerialise(doc));
		} 
		catch (WSDLException e) 
		{
			logger.error("imprimiendo formateado el XML RECIBIDO");
		}
		
		// dificilmente se pudiera dar pero por si se escapo algo se dejo esta validacion
		if(doc.getDocumentElement()==null){
			logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_NULLPOINTEREXCEPTION +  " No se obtuvo data en la respuesta recibida...", new Exception("verificar porque no se recibio nada de data despues de haber invocado al metodo web"));
			guardarRespuesta(metodo, doc, ESTADO_ERROR, ErroresSDA.ERROR_NULLPOINTEREXCEPTION.getDescripcion(), metodo.getRespuestas().iterator().next());
			return;
		}

		// normalizamos la respuesta recibida
		doc.getDocumentElement().normalize();

		//verificamos si obtuvimos algún error al invocar el metodo, conociendo ya la estructura en la que devolvera un error
		if(doc.getDocumentElement().getFirstChild().getNodeName().equals("errorSDA")){
			logger.error(getStringFromDocument(doc));
			guardarRespuesta(metodo, doc, ESTADO_ERROR, doc.getDocumentElement().getFirstChild().getLastChild().getTextContent(), metodo.getRespuestas().iterator().next());
			return;
		}
		else
		{
			// Se ha recibido una respuesta sin errores despues de la invocacion
			// al metodo del agregador por lo que solo queda procesar la
			// respuesta recibida.
			
			
			//las respuestas parametrizadas
			for (Respuesta respuesta : metodo.getRespuestas()) 
			{
				//tiene nodos hijos
				if (doc.getDocumentElement().hasChildNodes()) 
				{
					//listado de nodos hijos
					NodeList nodeList = doc.getDocumentElement().getChildNodes();
					
					// Se hara una lectura normal donde se lea la respuesta
					// parametrizada y el resultado parametrizado
					if(lectura==1)
					{
						// lectura de toda la estructura XML recibida con el fin
						// de buscara las respuestas parametrizadas
						lecturaListadoNodos1(nodeList, respuesta, metodo, doc);
						
						// si nuestra bandera no fue cambiada en nuestra
						// ejecucion anterior
						if(!validateBrowserResponse)
						{
							// buscaremos algun error que haya sido enviado a
							// traves del mismo mensaje por lo que lo
							// escanearemos con el fin de encontrar algun error
							lecturaListadoNodos3(nodeList, metodo, doc, respuesta);
							
							// si nuestra bandera no fue cambiada en nuestra
							// ejecucion anterior
							if(!validateBrowserResponse)
							{
								// si no se encontro el valor de la respuesta
								// parametrizado ni tampoco se encontro error
								// por comunicacion SOAP guardaremos un warning
								// para que sea revisada la parametrizacion de
								// respuestas para este metodo
								logger.warn(getAgregador().getNombre_agregador() + " " + ErroresSDA.NO_SE_ENCONTRO_EL_VALOR_PARAMETRIZADO_DENTRO_DE_LA_RESPUESTA_RECIBIDA.getDescripcion() + " No se encontro el tag " + respuesta.getNombre() );
								guardarRespuesta(metodo, doc, ESTADO_WARN, ErroresSDA.NO_SE_ENCONTRO_EL_VALOR_PARAMETRIZADO_DENTRO_DE_LA_RESPUESTA_RECIBIDA.getDescripcion(), respuesta);
							}
							else
							{
								// ya se guardo la respuesta por lo que
								// regresamos a su valor original a esta bandera
								validateBrowserResponse = false;
							}						
						}
						else
						{
							// ya se guardo la respuesta por lo que
							// regresamos a su valor original a esta bandera
							validateBrowserResponse = false;
						}
					}
					
					// se leera la respuesta del metodo de CONSULTA DE SERVICIOS ACTIVOS donde se
					// leera el arreglo con la respuesta parametrizada
					if(lectura==2)
					{						
						// lectura de toda la estructura XML recibida con el fin
						// de buscara los 11 parametros de una consulta de
						// servicios activos solo necesitamos el nombre del tag
						// de esos 11 valores que buscamos
						lecturaListadoNodos2(nodeList, respuesta.getNombre());
						
						// validamos a través de esta bandera si NO habian
						// servicios activos
						if(guardarConsulta)
						{
							guardarRespuesta(getConsulta(), doc, SIN_SERVICIOS, ErroresSDA.SIN_SERVICIOS_ACTIVOS.getDescripcion(), respuesta);
						}
						guardarConsulta=true;//es por si en la ejecucion de la baja de servicios me lo habian convertido a false
					}
				}
			}
		}	
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
	private Document invocarMetodoWeb(Metodos metodo) {
		//restauramos ese mensaje al original para poder hacer las sustituciones necesarias
		if(metodo.getMetodo()==3)
			metodo.setInputMessageText(mensajeEnvioBajaOriginal);
		
		// Cambiamos la accion a alta en lista negra exclusivo del Alta Servicio Web
		if(getTipoDepuracion().equals("Alta Servicio Web"))
			getParametrosData().put("accion", "1");
		
		//verificamos si tiene parametros parametrizados el metodo para hacer la susticion de valores pertinentes
		if (metodo.getParametros() != null) 
		{
			for (Parametros parametro : metodo.getParametros()) 
			{
				metodo.setInputMessageText(metodo.getInputMessageText().replace(("_*" + parametro.getNombre() + "_*").toString() , (getParametrosData().get(parametro.getNombre())==null?"":getParametrosData().get(parametro.getNombre()))  ));
			}
		}
		
		// Cambiamos la accion a baja en lista negra como normalmente se encuentra
		if(getTipoDepuracion().equals("Alta Servicio Web"))
			getParametrosData().put("accion", "2");
		
		//si el metodo es el de baja limpiamos de los parametros del sistema con los valores propios de la consulta 
		//para que estos datos sean cargados por cada servicio que se dara de baja
		if(metodo.getMetodo()==3)
		{
			getParametrosData().remove("servicioActivado");
			getParametrosData().remove("servicio");
			getParametrosData().remove("mcorta");
			getParametrosData().remove("macortaBK");
		}
		
		try 
		{
			logger.debug("Mensaje enviado:");
			logger.debug(com.cladonia.xml.webservice.wsdl.XMLSupport.prettySerialise(com.cladonia.xml.webservice.wsdl.XMLSupport.parse(metodo.getInputMessageText())));
		} 
		catch (WSDLException e) 
		{
			logger.error("imprimiendo formateado el XML RECIBIDO");
		}
		
		// primero verificamos SI es de tipo asmx
		if(metodo.getEndPoint().endsWith("asmx"))
		{
			ConsultarASMX stub = new ConsultarASMX();
			stub.setAgregador(getAgregador());
			return stub.invoke(metodo, getTimeOutMillisecond());
		}
		
		// si NO es asmx y si tiene seguridad SI es por https
		if (metodo.getSeguridad() == 1) 
		{
			ConsultarHTTPS stub = new ConsultarHTTPS();
			stub.setAgregador(getAgregador());
			return stub.invoke(metodo, getTimeOutMillisecond());
		}
		else 
		{
			// si NO es asmx y no tiene NO es por https
			ConsultarHTTP stub = new ConsultarHTTP();
			stub.setAgregador(getAgregador());
			return stub.invoke(metodo, getTimeOutMillisecond());
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
	private void guardarRespuesta(Metodos metodo, Document respuesta, String estado, String descripcion, Respuesta respuestaObj){
		LogDepuracion objGuardar = new LogDepuracion();
		objGuardar.setNumero((getParametrosData()==null?"No se pudo obtener":getParametrosData().get("movil")));
		objGuardar.setEstadoTransaccion(estado);
		objGuardar.setFechaTransaccion(new Date());
		objGuardar.setIdMetodo(metodo.getMetodo());
		objGuardar.setRespuestaFK(respuestaObj);
		objGuardar.setEnvio((metodo==null?"":metodo.getInputMessageText()));
		objGuardar.setRespuesta((respuesta==null?"":getStringFromDocument(respuesta)));
		objGuardar.setTipoTransaccion(getTipoDepuracion());
		objGuardar.setUsuarioSistema(getUsuarioSistema());
		objGuardar.setDescripcionEstado(descripcion);
		
		getEjecucion().createData(objGuardar);
		
		getRespuestas().add(objGuardar);
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
	private String getStringFromDocument(Document doc)
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
	    	//TODO:verificar si podemos sacar este error al exterior
	    	logger.error( getAgregador().getNombre_agregador() + " Error al querer pasar el Document a cadena de texto");
	       return "";
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
	private void lecturaListadoNodos1(NodeList nodeList, Respuesta respuesta, Metodos metodo, Document response) 
	{
		//recorremos todo el listado de nodos
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
			// nodo por nodo recibido en la respuesta
			Node node = nodeList.item(i);
			
			//verificamos el tipo de nodo sea un elemento
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				// verificamos el nombre con el fin de evitar un
				// nullpointerexception en la siguiente consulta
				if(node.getNodeName()!=null)
				{
					if(node.getNodeName().equalsIgnoreCase(respuesta.getNombre()))
					{
						// estamos en un nodo parametrizado por lo que deseamos
						// buscar las posibles respuestas que vienen en el
						for (ResultadosRespuesta resultados : respuesta.getResultadosRespuestas()) 
						{
							if(node.getTextContent()!=null)
							{
								//si encontramos un resultado parametrizado
								if(node.getTextContent().equals(resultados.getDato()))
								{
									//guardaremos la respuesta con el dato encontrado
									guardarRespuesta(metodo, response, resultados.getValor(), ErroresSDA.EJECUCION_EXITOSA.getDescripcion(), respuesta);
									
									// nuestra bandera la volvemos a true
									// indicando que si encontramos respuesta
									validateBrowserResponse = true;
								}
							}	
						}						
					}
				}
				
				// si el nodo tiene mas nodos hijos entonces entraremos de
				// manera recursiva a leer los nodos hijos
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
	private void lecturaListadoNodos2(NodeList nodeList, String nodeNameToReader) 
	{
		//recorremos todo el listado de nodos
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
			// nodo por nodo recibido en la respuesta
			Node node = nodeList.item(i);
			node.normalize();
			
			//verificamos el tipo de nodo sea un elemento
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				// verificamos el nombre con el fin de evitar un
				// nullpointerexception en la siguiente consulta
				if(node.getNodeName()!=null)
				{
					// si el nombre del nodo es igual al parametrizado
					if(node.getNodeName().equalsIgnoreCase(nodeNameToReader))
					{
						//si el nodo encontrado no tiene mas nodos hijos
						if(node.getFirstChild()==null || node.getFirstChild().getNodeValue()==null)
						{
							if(indice > 1 && indice < 12)
								indice++;
						}else
						{
							// obtenemos los datos en la posicion 1 2 y 5 en donde obtengo el servicio, si es activo y la marcacion de baja
							if(indice==1)
							{
								getParametrosData().put("servicio", (node.getFirstChild().getNodeValue()==null?"":node.getFirstChild().getNodeValue().trim()));
								//logger.debug("El Servicio " + getParametrosData().get("servicio"));
							}
							if(indice==2)
							{
								getParametrosData().put("servicioActivado", (node.getFirstChild().getNodeValue()==null?"":node.getFirstChild().getNodeValue().trim()));
								//logger.debug("Activado " + getParametrosData().get("servicioActivado"));
							}
							if(indice==4)
							{
								getParametrosData().put("mcortaBK", (node.getFirstChild().getNodeValue()==null?"":node.getFirstChild().getNodeValue().trim()));
								//logger.debug("Marcacion Corta backup" + getParametrosData().get("mcortaBK"));
							}
							if(indice==5)
							{
								getParametrosData().put("mcorta", (node.getFirstChild().getNodeValue()==null?"":node.getFirstChild().getNodeValue().trim()));
								//logger.debug("Marcacion Corta " + getParametrosData().get("mcorta"));
							}
							indice++;
						}
							
						//en este momento debe hacer lectura del ultimo valor
						if(indice == 12){
							
							// reiniciamos el conteo porque hemos terminado de leer los 11 valores esperado por respuesta
							indice = 1;
							
							//********* BAJA DE SERVICIOS **********//
							if(getParametrosData().containsKey("servicioActivado"))
							{
								//verificamos que el servicio este activo
								if(getParametrosData().get("servicioActivado").equals("1"))
								{
									//si el valor maracacion corta viene vacio ocupamos el anterior ( caso CRONOS )
									if(getParametrosData().get("mcorta") == null || getParametrosData().get("mcorta").equals("") )
									{
										getParametrosData().put("mcorta", getParametrosData().get("mcortaBK"));
									}
									
									//Guardamos la respuesta de la consulta siempre y cuando haya un servicio que bajar si no no guardara nada en la base de datos
									//guardarRespuesta(getConsulta(), respuesta, "Exito", "Se proceso de forma satisfactoria la solicitud para dar de baja el servicio");
									guardarConsulta = false;
									
									// validamos que si tengamos la configuracion para el metodo de baja
									if(getBaja()!=null){
										
										logger.debug("SE EJECUTARA LA BAJA DE SERVICIOS PARA " + getAgregador().getNombre_agregador());
										lecturaMetodoWeb(getBaja(), 1);
									}
								}
								else
								{
									getParametrosData().remove("servicioActivado");
									getParametrosData().remove("servicio");
									getParametrosData().remove("mcorta");
									getParametrosData().remove("macortaBK");
								}
							}
						}
					}
				}
				
				// si el nodo tiene mas nodos hijos entonces entraremos de
				// manera recursiva a leer los nodos hijos
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
	 * @param metodo
	 *            {@link Metodos} insumo para poder guardar la {@link Respuesta}
	 *            en la base de datos
	 * @param response {@link Document} recibido
	 * @return {@link Void}
	 * */
	private void lecturaListadoNodos3(NodeList nodeList, Metodos metodo, Document response, Respuesta respuesta) 
	{
		//recorremos todo el listado de nodos
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
			// nodo por nodo recibido en la respuesta
			Node node = nodeList.item(i);
			
			//verificamos el tipo de nodo sea un elemento
			if (node.getNodeType() == Node.ELEMENT_NODE) 
			{
				//verificamos el nombre con el fin de evitar un nullpointerexception en la siguiente consulta
				if(node.getNodeName()!=null){
					if(node.getNodeName().equalsIgnoreCase("faultstring"))
					{
						//si encontramos un error generado a través de la mensajeria SOAP aqui se verá reflejado
						if(node.getTextContent()!=null)
						{
							//guardaremos la respuesta con el error soap encontrado
							logger.debug(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_DESCRITO_DENTRO_DEL_MENSAJE_RECIBIDO.getDescripcion() + " " + node.getTextContent());
							guardarRespuesta(metodo, response, ESTADO_ERROR, ErroresSDA.ERROR_DESCRITO_DENTRO_DEL_MENSAJE_RECIBIDO.getDescripcion() + " " + node.getTextContent(), respuesta);
							
							// nuestra bandera la volvemos a true
							// indicando que si encontramos respuesta
							validateBrowserResponse = true;
						}						
					}
				}

				// si el nodo tiene mas nodos hijos entonces entraremos de
				// manera recursiva a leer los nodos hijos
				if (node.hasChildNodes())
					lecturaListadoNodos3(node.getChildNodes(), metodo, response, respuesta);
			}
		}
	}
	
	/**
	 * @return the agregador
	 */
	private Agregadores getAgregador() {
		return agregador;
	}

	/**
	 * @param agregador the agregador to set
	 */
	public void setAgregador(Agregadores agregador) {
		this.agregador = agregador;
	}

	/**
	 * @return the moviles
	 */
	private List<String> getMoviles() {
		return moviles;
	}

	/**
	 * @param moviles the moviles to set
	 */
	public void setMoviles(List<String> moviles) {
		this.moviles = moviles;
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

	/**
	 * @return the parametrosData
	 */
	private HashMap<String, String> getParametrosData() {
		return parametrosData;
	}

	/**
	 * @param parametrosData the parametrosData to set
	 */
	public void setParametrosData(HashMap<String, String> parametrosData) {
		this.parametrosData = parametrosData;
	}

	/**
	 * @return the timeOutMillisecond
	 */
	private long getTimeOutMillisecond() {
		return timeOutMillisecond;
	}

	/**
	 * @param timeOutMillisecond the timeOutMillisecond to set
	 */
	private void setTimeOutMillisecond(long timeOutMillisecond) {
		this.timeOutMillisecond = timeOutMillisecond;
	}

	/**
	 * @return the ejecucion
	 */
	private BdEjecucion getEjecucion() {
		return ejecucion;
	}

	/**
	 * @return the usuarioSistema
	 */
	private UsuarioSistema getUsuarioSistema() {
		return usuarioSistema;
	}

	/**
	 * @param usuarioSistema the usuarioSistema to set
	 */
	public void setUsuarioSistema(UsuarioSistema usuarioSistema) {
		this.usuarioSistema = usuarioSistema;
	}

	/**
	 * @return the tipoDepuracion
	 */
	private String getTipoDepuracion() {
		return tipoDepuracion;
	}

	/**
	 * @param tipoDepuracion the tipoDepuracion to set
	 */
	public void setTipoDepuracion(String tipoDepuracion) {
		this.tipoDepuracion = tipoDepuracion;
	}

	/**
	 * @return the respuestas
	 */
	private List<LogDepuracion> getRespuestas() {
		return respuestas;
	}

	/**
	 * @param respuestas the respuestas to set
	 */
	private void setRespuestas(List<LogDepuracion> respuestas) {
		this.respuestas = respuestas;
	}
}
