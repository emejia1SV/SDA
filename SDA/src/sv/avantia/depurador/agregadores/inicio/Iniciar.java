package sv.avantia.depurador.agregadores.inicio;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.Pais;
import sv.avantia.depurador.agregadores.hilo.ConsultaAgregadorPorHilo;
import sv.avantia.depurador.agregadores.jdbc.BdEjecucion;
import sv.avantia.depurador.agregadores.jdbc.SessionFactoryUtil;
import sv.avantia.depurador.agregadores.utileria.Log4jInit;

public class Iniciar {

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
	public static Logger logger = Logger.getLogger("avantiaLogger");
	
	/**
	 * Instancia de las operaciones con la base de datos.
	 * 
	 * */
	private static BdEjecucion ejecucion = null;

	/**
	 * Metodo que inicializara todo el flujo del JAR ejecutable
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param args
	 */
	public static void main(String[] args) {
		long init = System.currentTimeMillis();
		List<String> moviles = new ArrayList<String>();
		try 
		{
			logger.info("Iniciando la depuración Masiva...");
			
			//iniciar la instancia a las operaciones a la base de datos
			setEjecucion(new BdEjecucion());
			
			// consultar la parametrización
			for (Pais pais : obtenerParmetrizacion()) 
			{
				// consultar los numeros
				moviles = obtenerNumeros(pais.getCodigo());

				if (moviles.size() > 0) 
				{
					System.out.println("Procesando... " + pais.getNombre());
					for (Agregadores agregador : pais.getAgregadores()) 
					{
						// abrir un hilo pr cada agregador parametrizados
						ConsultaAgregadorPorHilo hilo = new ConsultaAgregadorPorHilo();
						hilo.setMoviles(moviles);
						hilo.setAgregador(agregador);
						hilo.setTipoDepuracion("MASIVA");
						hilo.setUsuarioSistema(getEjecucion().usuarioMaestro());
						hilo.start();
					}
				}
			}			
		} 
		catch (Exception e) 
		{
			logger.error("Error en el sistema de depuracion masiva automatico ", e);
		}
		finally
		{
			//terminar el flujo.
			SessionFactoryUtil.closeSession();
			moviles = null;
			setEjecucion(null);
			logger.info("finalizo la depuración de los numeros en " + ((System.currentTimeMillis() - init)/1000)  + "Segundos");
		}
	}
	
	/**
	 * Obtener el insumo de numeros que deberan ser procesados para su
	 * depuracion
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @return {@link List} numeros para depurar
	 * @throws Exception
	 *             podria generarse una exepcion en el momento de ejecutar la
	 *             consulta a la base de datos
	 * */
	@SuppressWarnings("unchecked")
	public static List<String> obtenerNumeros(String codigo) throws Exception 
	{
		return (List<String>)(List<?>) getEjecucion().listData("select b.numero from CLIENTE_TEL b where b.numero like '"+ codigo +"%'");
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
	public static List<Pais> obtenerParmetrizacion() throws Exception 
	{
		return (List<Pais>)(List<?>) getEjecucion().listData("FROM SDA_PAISES WHERE STATUS = 1");
	}

	/**
	 * getter
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @return the ejecucion
	 */
	private static BdEjecucion getEjecucion() {
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
	private static void setEjecucion(BdEjecucion ejecucion) {
		Iniciar.ejecucion = ejecucion;
	}
}