package sv.avantia.depurador.agregadores.servicio.web;

/**
 * Servicio Web SDA
 * 
 * @author Edwin Mejia - Avantia Consultores
 * @version1.0
 * */
public class ServicioWebSDA {

	/**
	 * Metodo web que sirve de conector para ejecutar todo el codigo ya
	 * realizado del sistema SDA, vease documentacion para ver la estructura
	 * tanto del insumo de numeros como de la respuesta desplegada
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param numerosEnXML
	 *            {@link String} en formato XML
	 * @return {@link String} en formato XML
	 * 
	 * */
	public String depuracionMasiva(String numerosEnXML) {
		EjecucionServicioWeb accion = new EjecucionServicioWeb();
		return accion.respuestaBajaMasivas(numerosEnXML);
	}

	/**
	 * Metodo web que sirve para dar de alta en la lista negra a traves del SDA,
	 * vease documentacion para ver la estructura tanto del insumo de numeros
	 * como de la respuesta desplegada
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param numerosEnXML
	 *            {@link String} en formato XML
	 * @return {@link String} en formato XML
	 * */
	public String ingresoListaNegra(String numerosEnXML) {
		EjecucionServicioWeb accion = new EjecucionServicioWeb();
		return accion.respuestaIngresoListaNegra(numerosEnXML);
	}
}
