package sv.avantia.depurador.agregadores.hilo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import sv.avantia.depurador.agregadores.entidades.LogDepuracion;
import sv.avantia.depurador.agregadores.jdbc.BdEjecucion;

public class DepuracionMasiva {

	/**
	 * La constante que se enviara como estado de la tansaccion fallida
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private static String ESTADO_ERROR = "ERROR";
	
	/**
	 * Objeto respuesta
	 * */
	private List<LogDepuracion> respuestas;
	
	/**
	 * Obtener el appender para la impresión en un archivo de LOG
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private static Logger logger = Logger.getLogger("avantiaLogger");
	
	/**
	 * Instancia de la Clase {@link BdEjecucion} que maneja los tipos de
	 * transacciones, que podemos realizar contra la base de datos.
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private BdEjecucion ejecucion = new BdEjecucion();
	
	/**
	 * Bandera que nos servira para conocer las veces en que se ha recibido
	 * respuestas desde la depuracion por numero a traves de hilo de ejecucion
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private int contadorRespuestasObtendas = 0;
	
	private List<DepuracionPorNumero> paraProcesarData;
	
	private DepuracionPorNumero depuracionPorNumero;
	
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
		
		if(getParaProcesarData().size()<1)
			return new HashMap<String, List<LogDepuracion>>();
		
		try {			
			for (DepuracionPorNumero porNumero : getParaProcesarData()) 
			{	
	        	//future = executor.submit(porNumero);
	        	Thread taskInvoke;
				Runnable run = new Runnable() 
				{
					public void run() 
					{
						try 
						{
							ExecutorService executor = Executors.newSingleThreadExecutor();
							Future<List<LogDepuracion>> future = executor.submit(depuracionPorNumero);
							getRespuestas().addAll(future.get());
							contadorRespuestasObtendas++;
					        executor.shutdown();
						} 
						catch (Exception ex) 
						{
							logger.error("Error al obtener el listado de respuestas", ex);
						}
					}
				};
				
				depuracionPorNumero = porNumero;
				taskInvoke = new Thread(run);
				taskInvoke.start();
				Thread.sleep(100);
			}
	        					
			//nos quedamos esperando todas las respuestas
			while(true)
			{
				Thread.sleep(500);//para no ejecutar tantas veces la misma preguntadera
				//hasta que las tengamos todas las respuestas dejamos de esperar
				if(contadorRespuestasObtendas >= getParaProcesarData().size())
					break;
			}
		} 
		catch (Exception e) 
		{
			// Tuvimos un error al estar esperando las respuestas obtenidas desde el hilo por numero
			guardarRespuesta(ESTADO_ERROR, "Se tuvo una excepcion en el momento de estar esperando las respuestas por numero");
		}
		
		try {
			//retornamos las respuestas
			HashMap<String, List<LogDepuracion>> respuestaOut = new HashMap<String, List<LogDepuracion>>();
			respuestaOut.put(getParaProcesarData().get(0).getAgregador().getNombre_agregador(), getRespuestas());
			return respuestaOut;
		} catch (Exception e) {
			logger.error("No e pudo poner el resultado en el hashmap", e);
			return new HashMap<String, List<LogDepuracion>>();
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
	private void guardarRespuesta(String estado, String descripcion){
		LogDepuracion objGuardar = new LogDepuracion();
		objGuardar.setNumero("00000");
		objGuardar.setEstadoTransaccion(estado);
		objGuardar.setFechaTransaccion(new Date());
		objGuardar.setIdMetodo(4);
		objGuardar.setRespuestaFK(null);
		objGuardar.setEnvio("");
		objGuardar.setRespuesta("");
		objGuardar.setTipoTransaccion((getParaProcesarData().size()>0?getParaProcesarData().get(0).getTipoDepuracion():"generic error"));
		objGuardar.setUsuarioSistema((getParaProcesarData().size()>0?getParaProcesarData().get(0).getUsuarioSistema():getEjecucion().usuarioMaestro()));
		objGuardar.setDescripcionEstado(descripcion);
		
		getEjecucion().createData(objGuardar);
		
		getRespuestas().add(objGuardar);
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
	
	/**
	 * @return the ejecucion
	 */
	private BdEjecucion getEjecucion() {
		return ejecucion;
	}

	/**
	 * @return the paraProcesarData
	 */
	public List<DepuracionPorNumero> getParaProcesarData() {
		return paraProcesarData;
	}

	/**
	 * @param paraProcesarData the paraProcesarData to set
	 */
	public void setParaProcesarData(List<DepuracionPorNumero> paraProcesarData) {
		this.paraProcesarData = paraProcesarData;
	}
}
