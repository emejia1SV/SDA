package sv.avantia.depurador.agregadores.utileria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.LogDepuracion;
import sv.avantia.depurador.agregadores.entidades.Pais;
import sv.avantia.depurador.agregadores.hilo.ConsultaAgregadorPorHilo;
import sv.avantia.depurador.agregadores.hilo.DepuracionMasiva;
import sv.avantia.depurador.agregadores.hilo.GestionarParametrizacion;
import sv.avantia.depurador.agregadores.jdbc.BdEjecucion;

public class TestSDA {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> moviles = new ArrayList<String>();
		moviles.add("50257128949");
		TestSDA sda = new TestSDA();
		sda.depuracionBajaMasiva(moviles, "prueba", true);
	}
	
	/**
	 * Iniciar la configuracion para los apender del LOG4J
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	static {
		Log4jInit.init();
	}

	/**
	 * Obtener el appender para la impresión en un archivo de LOG
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private static Logger logger = Logger.getLogger("avantiaLogger");
	
	/**
	 * Instancia de las operaciones con la base de datos.
	 * 
	 * */
	private BdEjecucion ejecucion = null;

	/**
	 * Metodo que inicializara todo el flujo del JAR ejecutable
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public String depuracionBajaMasiva(List<String> moviles, String tipoDepuracion, boolean obtenerRespuesta) {
		long init = System.currentTimeMillis();
		List<String> numerosPorPais = new ArrayList<String>();
		String out = "";
		try 
		{			
			//iniciar la instancia a las operaciones a la base de datos
			setEjecucion(new BdEjecucion());
			
			logger.info("Obtener Parametrización");
			// consultar la parametrización
			for (Pais pais : obtenerParmetrizacion()) 
			{
				//validamos que el estado de pais a verificar este activo
				if(pais.getEstado().intValue()==1)
				{
					//verificamos la cantidad de moviles recibidos para la depuración
					if (moviles.size() > 0) 
					{
						// iniciamos la nueva lista de numero por pais
						numerosPorPais = new ArrayList<String>();
						
						//recorremos los numeros obtenidos para su clasificacion por pais
						for (String string : moviles) 
						{
							// para reconocer el pais lo hacemos a través de su codigo de pais 
							if(string.startsWith(pais.getCodigo()))
								numerosPorPais.add(string);
						}
						
						//si no hay numeros en el pais recorrido no se debe enviar ningun hilo
						if(numerosPorPais.size()>0)
						{
							//recorremos cada aregador para levantar un hilo por agregador por pais
							for (Agregadores agregador : pais.getAgregadores()) 
							{
								//verificamos el estado del agregador que este activo para ser tomado en cuenta en la depuración
								if(agregador.getEstado().intValue()==1)
								{
									//verificammos que por lo menos un agregador este parametrizado con metodos
									if(!agregador.getMetodos().isEmpty())
									{			
										
										if(agregador.getId().intValue() == 24)
										{
											// abrir un hilo pr cada agregador parametrizados
											DepuracionMasiva hilo = new DepuracionMasiva();
											hilo.setMoviles(numerosPorPais);
											hilo.setAgregador(agregador);
											hilo.setTipoDepuracion(tipoDepuracion);
											hilo.setUsuarioSistema(getEjecucion().usuarioMaestro());
											
											guardarRespuestaEnContenedor(hilo.procesarDepuracion(), agregador.getNombre_agregador());
										}
									}
								}
							}
						}
					}
				}
			}	
			
			//consultar las respuestas
			for (Pais pais : obtenerParmetrizacion()) 
			{
				//validamos que el estado de pais a verificar este activo
				if(pais.getEstado()==1)
				{
					//recorremos cada aregador para conocer la respuesta de cada hilo iniciado
					for (Agregadores agregador : pais.getAgregadores()) 
					{
						//verificamos el estado del agregador que este activo para ser tomado en cuenta en la depuración
						if(agregador.getEstado()==1)
						{
							//verificammos que por lo menos un agregador este parametrizado con metodos
							if(!agregador.getMetodos().isEmpty())
							{
								//conocer respuesta de cada hilo por cada agregador iniciado
								ThreadGroup currentGroup = ConsultaAgregadorPorHilo.currentThread().getThreadGroup();
								
								//verificamos la cantidad de hilos activos
								int nHilos = currentGroup.activeCount();
								
								//iniciamos un arreglo para los hilos activos
								Thread[] hilosActivos = new Thread[nHilos];
								currentGroup.enumerate(hilosActivos);
								
								//recorremos los hilos activos para obtener la respuesta
								for (int i = 0; i < nHilos; i++)
								{
									//verificamos el nombre del hilo para saber si es un hilo que deseamos conocer la respuesta
									if(hilosActivos[i].getName().equals(agregador.getId().toString()))
									{
										//aparte del nombre verificamos el tipo del hilo
										if (hilosActivos[i] instanceof ConsultaAgregadorPorHilo) 
										{
											ConsultaAgregadorPorHilo propio = (ConsultaAgregadorPorHilo) hilosActivos[i];
											
											// consultamos si sigue vivo y
											// por esta simple razon siempre
											// debe leerse la respuesta para
											// que por lo menos podamos
											// detener el hilo
											if (propio.isAlive()) 
											{
												// verificamos si externamente quieren conocer las respuestas sino
												// terminamos el flujo lo antes posible y todo que solo quede
												// constancia en la base de datos
												if(obtenerRespuesta)
												{
													//verificamos con bandera si ya termino de ejecutarse normalmente el hilo que estamos consultando
													if(propio.isSePuedeObtenerRespuesta())
													{
														guardarRespuestaEnContenedor(propio.getRespuestas(), propio.getName());
														propio.stop();
														logger.debug("======== SE MANDO A DETENER EL HILO PARA EL AGREGADOR " + propio.getName() + " ========");
													}
													else
													{
														//como no esta lista aun la respuesta lo esperaremos 3 segundos mas
														Thread.sleep(3000);
														//verificamos con bandera si ya termino de ejecutarse normalmente el hilo que estamos consultando
														if(propio.isSePuedeObtenerRespuesta())
														{
															guardarRespuestaEnContenedor(propio.getRespuestas(), propio.getName());
															propio.stop();
															logger.debug("======== SE MANDO A DETENER EL HILO PARA EL AGREGADOR " + propio.getName() + " ========");
														}
													}
												}
												else
												{
													propio.stop();
													logger.debug("======== SE MANDO A DETENER EL HILO PARA EL AGREGADOR " + propio.getName() + " ========");
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			generar();
		} 
		catch (Exception e) 
		{
			logger.error("Error en el sistema de depuracion masiva automatico ", e);
		}
		finally
		{
			moviles = null;
			setEjecucion(null);
			logger.info("finalizo la depuración de los numeros en " + ((System.currentTimeMillis() - init)/1000)  + "Segundos");
		}
		return out;
	}
	
	public String altaListaNegra(List<String> moviles, String tipoDepuracion){
		return "<respuesta>																												"
				+"	<SMT>																													"
				+"		<depuracion>                                                                                                        "
				+"			<listaNegra>                                                                                                    "
				+"				<numero>50377494676</numero>                                                                                "
				+"				<codigoError>2</codigoError>                                                                                "
				+"				<descripcionEstado>SMT GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD</descripcionEstado>         "
				+"			</listaNegra>                                                                                                   "
				+"		</depuracion>                                                                                                       "
				+"		<depuracion>                                                                                                        "
				+"			<listaNegra>                                                                                                    "
				+"				<numero>50377504963</numero>                                                                                "
				+"				<codigoError>0</codigoError>                                                                                "
				+"				<descripcionEstado>EXITO</descripcionEstado>                                                                "
				+"			</listaNegra>                                                                                                   "
				+"		</depuracion>                                                                                                       "
				+"		<depuracion>                                                                                                        "
				+"			<listaNegra>                                                                                                    "
				+"				<numero>50379748568</numero>                                                                                "
				+"				<codigoError>1</codigoError>                                                                                "
				+"				<descripcionEstado>FALLO</descripcionEstado>                                                                "
				+"			</listaNegra>                                                                                                   "
				+"		</depuracion>                                                                                                       "
				+"	</SMT>                                                                                                                  "
				+"	<GRUPO_M>                                                                                                               "
				+"		<depuracion>                                                                                                        "
				+"			<listaNegra>                                                                                                    "
				+"				<numero>50377494676</numero>                                                                                "
				+"				<codigoError>2</codigoError>                                                                                "
				+"				<descripcionEstado>GRUPO_M GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD</descripcionEstado>     "
				+"			</listaNegra>                                                                                                   "
				+"			<consulta>                                                                                                      "
				+"				<numero>50377494676</numero>                                                                                "
				+"				<codigoError>0</codigoError>                                                                                "
				+"				<descripcionEstado>SIN SERVICIOS</descripcionEstado>                                                        "
				+"			</consulta>                                                                                                     "
				+"			<baja>                                                                                                          "
				+"				<numero>50377494676</numero>                                                                                "
				+"				<servicio/>                                                                                                 "
				+"				<codigoError>0</codigoError>                                                                                "
				+"				<descripcionEstado/>                                                                                        "
				+"			</baja>                                                                                                         "
				+"		</depuracion>                                                                                                       "
				+"		<depuracion>                                                                                                        "
				+"			<listaNegra>                                                                                                    "
				+"				<numero>50377504963</numero>                                                                                "
				+"				<codigoError>3</codigoError>                                                                                "
				+"				<descripcionEstado>GRUPO_M GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD</descripcionEstado>     "
				+"			</listaNegra>                                                                                                   "
				+"			<consulta>                                                                                                      "
				+"				<numero>50377504963</numero>                                                                                "
				+"				<codigoError>0</codigoError>                                                                                "
				+"				<descripcionEstado>SIN SERVICIOS</descripcionEstado>                                                        "
				+"			</consulta>                                                                                                     "
				+"			<baja>                                                                                                          "
				+"				<numero>50377504963</numero>                                                                                "
				+"				<servicio/>                                                                                                 "
				+"				<codigoError>0</codigoError>                                                                                "
				+"				<descripcionEstado/>                                                                                        "
				+"			</baja>                                                                                                         "
				+"		</depuracion>                                                                                                       "
				+"		<depuracion>                                                                                                        "
				+"			<listaNegra>                                                                                                    "
				+"				<numero>50379748568</numero>                                                                                "
				+"				<codigoError>0</codigoError>                                                                                "
				+"				<descripcionEstado>EXITO</descripcionEstado>                                                                "
				+"			</listaNegra>                                                                                                   "
				+"			<consulta>                                                                                                      "
				+"				<numero>50379748568</numero>                                                                                "
				+"				<codigoError>0</codigoError>                                                                                "
				+"				<descripcionEstado>3 SERVICIOS</descripcionEstado>                                                          "
				+"			</consulta>                                                                                                     "
				+"			<baja>                                                                                                          "
				+"				<numero>50379748568</numero>                                                                                "
				+"				<servicio>Servicio1</servicio>                                                                              "
				+"				<codigoError>0</codigoError>                                                                                "
				+"				<descripcionEstado>EXITO</descripcionEstado>                                                                "
				+"			</baja>                                                                                                         "
				+"			<baja>                                                                                                          "
				+"				<numero>50379748568</numero>                                                                                "
				+"				<servicio>Servicio2</servicio>                                                                              "
				+"				<codigoError>1</codigoError>                                                                                "
				+"				<descripcionEstado>FALLO</descripcionEstado>                                                                "
				+"			</baja>                                                                                                         "
				+"			<baja>                                                                                                          "
				+"				<numero>50379748568</numero>                                                                                "
				+"				<servicio>Servicio3</servicio>                                                                              "
				+"				<codigoError>2</codigoError>                                                                                "
				+"				<descripcionEstado>GRUPO_M GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD</descripcionEstado>     "
				+"			</baja>                                                                                                         "
				+"		</depuracion>                                                                                                       "
				+"	</GRUPO_M>	                                                                                                            "
				+"</respuesta>                                                                                                              ";
	}
	
	private contenedorRespuestas todasLasRespuestas = new contenedorRespuestas();
	private void guardarRespuestaEnContenedor(List<LogDepuracion> respuestasObtenidas, String agregador)
	{
		todasLasRespuestas.getData().put(agregador, respuestasObtenidas);
		System.out.println(todasLasRespuestas.getData().size());
	}
	
	
	private void generar() throws ParserConfigurationException, TransformerException 
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		
		// Create from whole cloth
		Element root = (Element) document.createElement("respuesta");
		document.appendChild(root);
		
		Iterator<Entry<String, List<LogDepuracion>>> it = todasLasRespuestas.getData().entrySet().iterator();
		while(it.hasNext())
		{
			Entry<String, List<LogDepuracion>> entry = it.next();
			Element agregador = (Element) document.createElement(entry.getKey());
			Element depuracion = (Element) document.createElement("Depuracion");
			
			root.appendChild(agregador);
			agregador.appendChild(depuracion);
			
			for (LogDepuracion depuracionX : entry.getValue()) 
			{
				Element metodo = (Element) document.createElement(depuracionX.getRespuestaFK().getMetodo().getMetodo().intValue()==1?"listaNegra":depuracionX.getRespuestaFK().getMetodo().getMetodo().intValue()==2?"consulta":depuracionX.getRespuestaFK().getMetodo().getMetodo().intValue()==3?"baja":"Deafult");
				Element numero = (Element) document.createElement("numero");
				Element codigoError = (Element) document.createElement("codigoError");
				Element descripcionEstado = (Element) document.createElement("descripcionEstado");
				
				depuracion.appendChild(metodo);
				
				metodo.appendChild(numero);
				metodo.appendChild(codigoError);
				metodo.appendChild(descripcionEstado);
				
				numero.appendChild(document.createTextNode(depuracionX.getNumero()));
				codigoError.appendChild(document.createTextNode(depuracionX.getEstadoTransaccion()));
				descripcionEstado.appendChild(document.createTextNode(depuracionX.getDescripcionEstado()));
				
			}
		}
				
		document.getDocumentElement().normalize();

		xmlOut(document, new StreamResult(System.out));

	}
	
	/**
	 * Metodo Para darle Salida al archivo document recibido como parametro
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param document
	 * @return void
	 * @throws javax.xml.transform.TransformerException
	 * */
	private void xmlOut( org.w3c.dom.Node document, javax.xml.transform.stream.StreamResult result)  
			throws javax.xml.transform.TransformerException 
	{
		// usamos una fabrica de transformacion para la salida del document
		javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
		javax.xml.transform.Transformer transformer;
		try 
		{
			transformer = tFactory.newTransformer();
		} 
		catch (javax.xml.transform.TransformerConfigurationException e1) 
		{
			throw new javax.xml.transform.TransformerConfigurationException("error en la fabrica de transformación");
		}

		
		try 
		{
			// cargamos nuestro insumo para la transformacion
			javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(document);
			//transformacion
			transformer.transform(source, result);
		} 
		catch (javax.xml.transform.TransformerException e) 
		{
			throw new javax.xml.transform.TransformerException("error en la transformación");
		}
	}
	
	public String xmlError(ErroresSDA error){
		return "<respuesta><errorSDA><codigoError>"+error.getCodigo()+"</codigoError><descripcionEstado>"+error.getDescripcion()+"</descripcionEstado></errorSDA></respuesta>";
	}
	
	/**
	 * Obtener insumo de parametrización para consultar a los agregadores
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @return {@link List} paises con sus dependencias en la base de datos
	 * @throws Exception
	 *             podria generarse una exepcion en el momento de ejecutar la
	 *             consulta a la base de datos
	 * */
	@SuppressWarnings("unchecked")
	private List<Pais> obtenerParmetrizacion() throws Exception 
	{
		return (List<Pais>)(List<?>) getEjecucion().listData("FROM SDA_PAISES WHERE STATUS = 1");
	}

	/**
	 * getter
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @return the ejecucion
	 */
	private BdEjecucion getEjecucion() {
		return ejecucion;
	}

	/**
	 * setter
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param ejecucion
	 *            the ejecucion to set
	 * @return {@link Void}
	 */
	private void setEjecucion(BdEjecucion ejecucion) {
		this.ejecucion = ejecucion;
	}

	/**
	 * Clase interna de {@link GestionarParametrizacion} para contener las respuestas de cada agregador
	 * @author Edwin Mejia - Avantia Consultores
	 * @version 1.0
	 * */
	class contenedorRespuestas implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private HashMap<String, List<LogDepuracion>> data = new HashMap<String, List<LogDepuracion>>();
		
		private HashMap<String, List<LogDepuracion>> getData() 
		{
			return data;
		}
	}
}
