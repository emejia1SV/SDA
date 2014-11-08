package sv.avantia.depurador.agregadores.inicio;

import java.util.List;

import org.apache.log4j.Logger;

import sv.avantia.depurador.agregadores.hilo.GestionarParametrizacion;
import sv.avantia.depurador.agregadores.jdbc.BdEjecucion;
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
	private static Logger logger = Logger.getLogger("avantiaLogger");
	
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
		try 
		{
			logger.info("Iniciando la depuración Masiva...");
			
			//iniciar la instancia a las operaciones a la base de datos
			setEjecucion(new BdEjecucion());
			
			//gestionar la logica de la depuracion
			GestionarParametrizacion gestion = new GestionarParametrizacion();
			gestion.depuracionBajaMasiva(obtenerNumeros(), "MASIVA", false);
		} 
		catch (Exception e) 
		{
			logger.error("Error en el sistema de depuracion masiva automatico ", e);
		}
		finally
		{
			setEjecucion(null);
			logger.info("finalizo la depuración Masiva de los numeros en " + ((System.currentTimeMillis() - init)/1000)  + "Segundos");
		}
	}
	
	/**
	 * Obtener el insumo de numeros que deberan ser procesados para su
	 * depuracion todos los que se encuentren en esa tabla
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @return {@link List} numeros para depurar
	 * @throws Exception
	 *             podria generarse una exepcion en el momento de ejecutar la
	 *             consulta a la base de datos
	 * */
	@SuppressWarnings("unchecked")
	private static List<String> obtenerNumeros() throws Exception 
	{
		return (List<String>)(List<?>) getEjecucion().listData("select b.numero from CLIENTE_TEL b");
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