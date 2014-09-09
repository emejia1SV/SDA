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
	 * 
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
	
	private static List<String> moviles = new ArrayList<String>();

	/**
	 * Metodo que inicializara todo el flujo del JAR ejecutable
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param args
	 */
	public static void main(String[] args) {
	
		long init = System.currentTimeMillis();
		//consultar los numeros
		obtenerNumeros();
		
		//consultar la parametrización
		for (Pais pais : obtenerParmetrizacion()) {
			System.out.println("Procesando... " + pais.getNombre());
			for (Agregadores agregador : pais.getAgregadores()) {
				//abrir un hilo pr cada agregador parametrizados
				ConsultaAgregadorPorHilo hilo = new ConsultaAgregadorPorHilo();
				hilo.setMoviles(moviles);
				hilo.setAgregador(agregador);
				hilo.start();
			}
		}
		
		//terminar el flujo.
		SessionFactoryUtil.closeSession();
		
		System.out.println("finish " + ((System.currentTimeMillis() - init)/1000)  + "Segundos");
	}
	
	@SuppressWarnings("unchecked")
	public static void obtenerNumeros(){
		BdEjecucion ejecucion = new BdEjecucion();
		try {
			moviles = (List<String>) ejecucion.listData("select b.numero from CLIENTE_TEL b where b.id='287040'");
		} finally{
			ejecucion = null;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static List<Pais> obtenerParmetrizacion(){
		BdEjecucion ejecucion = new BdEjecucion();
		try {
			return (List<Pais>) ejecucion.listData("FROM AGR_PAISES WHERE ID = 2");
		} finally{
			ejecucion = null;
		}
	}

}
