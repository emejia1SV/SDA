package sv.avantia.depurador.agregadores.hilo;

import java.util.List;

import org.apache.log4j.Logger;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.LogDepuracion;
import sv.avantia.depurador.agregadores.entidades.UsuarioSistema;

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
	
	/**
	 * Objeto respuesta que se retornara
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	List<LogDepuracion> respuestas;
	
	/**
	 * Bandera si se puede obtener datos 
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	boolean sePuedeObtenerRespuesta=false;
	
	
	@SuppressWarnings({ "static-access", "deprecation" })
	public void run() {
		DepuracionMasiva depuracion = new DepuracionMasiva();
		try {
			
			depuracion.setAgregador(getAgregador());
			depuracion.setMoviles(getMoviles());
			depuracion.setTipoDepuracion(getTipoDepuracion());
			depuracion.setUsuarioSistema(getUsuarioSistema());
			
			logger.info("=======================	SE CONSULTARA EL AGREGADOR " + getAgregador().getNombre_agregador() + "	=======================" + getMoviles().size() );
			
			setRespuestas(depuracion.procesarDepuracion());
			setSePuedeObtenerRespuesta(true);
			this.sleep(120000); //esperamos 2 minutos para que sea recojida la respuesta de este hilo.
		
		} 
		catch (Exception e) 
		{
			logger.error("ERROR DE EJECUCION DENTRO DE LA DEPURACION DEL AGREGADOR " + getAgregador().getNombre_agregador() + e.getMessage(), e);
			depuracion = null;
			this.stop();
		} finally
		{
			depuracion = null;
			logger.info("======== SE TERMINO DE EJECUTAR EL HILO PARA EL AGREGADOR " + getAgregador().getNombre_agregador() + " ========");
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
	 * @return the respuestas
	 */
	public List<LogDepuracion> getRespuestas() {
		return respuestas;
	}

	/**
	 * @param respuestas the respuestas to set
	 */
	private void setRespuestas(List<LogDepuracion> respuestas) {
		this.respuestas = respuestas;
	}

	/**
	 * @return the sePuedeObtenerRespuesta
	 */
	public boolean isSePuedeObtenerRespuesta() {
		return sePuedeObtenerRespuesta;
	}

	/**
	 * @param sePuedeObtenerRespuesta the sePuedeObtenerRespuesta to set
	 */
	private void setSePuedeObtenerRespuesta(boolean sePuedeObtenerRespuesta) {
		this.sePuedeObtenerRespuesta = sePuedeObtenerRespuesta;
	}
}