package sv.avantia.depurador.agregadores.hilo;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.LogDepuracion;
import sv.avantia.depurador.agregadores.entidades.UsuarioSistema;

public class ConsultaAgregadorPorHilo implements Callable<HashMap<String, List<LogDepuracion>>> {
	
	/**
	 * construct default
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	public ConsultaAgregadorPorHilo(){
		
	}
	
	/**
	 * Instancia de un {@link HashMap} para mantener en memoria los parametros
	 * con los que me serviran de insumo para llenar los parametros requeridos
	 * por los agregadores
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private HashMap<String, String> parametrosData = null;
	
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
	
	@Override
	public HashMap<String, List<LogDepuracion>> call() throws Exception {
		//long initLocal = System.currentTimeMillis();
		DepuracionMasiva depuracion = new DepuracionMasiva();
		try {
			//colocamos nombre al hilo para entender en el log de que agregador estamos hablando
			if(!Thread.currentThread().getName().equals(getAgregador().getNombre_agregador()) )
				Thread.currentThread().setName(getAgregador().getNombre_agregador()); 
			
			//entregamos el insumo a la clase de la depuracion
			depuracion.setAgregador(getAgregador());
			depuracion.setMoviles(getMoviles());
			depuracion.setTipoDepuracion(getTipoDepuracion());
			depuracion.setUsuarioSistema(getUsuarioSistema());
			depuracion.setParametrosData(getParametrosData());
			
			logger.info("SE CONSULTARA EL AGREGADOR " + getAgregador().getNombre_agregador() + " CON LA CANTIDAD DE " + getMoviles().size() + " NUMEROS" );
			
			return depuracion.procesarDepuracion();
		} 
		catch (Exception e) 
		{
			logger.error("ERROR DE EJECUCION DENTRO DE LA DEPURACION DEL AGREGADOR " + getAgregador().getNombre_agregador() + e.getMessage(), e);
			depuracion = null;
			return new HashMap<String, List<LogDepuracion>>();
		} finally
		{
			//System.out.println("se tardo " + getAgregador().getNombre_agregador() + " " + (System.currentTimeMillis() - initLocal) );
			depuracion = null;
			logger.info("SE TERMINO DE EJECUTAR EL AGREGADOR " + getAgregador().getNombre_agregador());
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
}