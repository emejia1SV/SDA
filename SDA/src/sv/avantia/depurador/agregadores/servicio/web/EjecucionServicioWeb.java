package sv.avantia.depurador.agregadores.servicio.web;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sv.avantia.depurador.agregadores.hilo.GestionarParametrizacion;
import sv.avantia.depurador.agregadores.utileria.ErroresSDA;

/**
 * Clase que realizara el procesar el XML de insumo con numeros moviles y
 * gestionar la depuracion masiva
 * 
 * @author Edwin Mejia - Avantia Consultores
 * @version 1.0
 * */
public class EjecucionServicioWeb {

	private List<String> listaMoviles = new ArrayList<String>();
	private GestionarParametrizacion gestion = new GestionarParametrizacion();
	private String respuesta;

	/**
	 * Metodo que realizara la gestion de la depuracion apuntando al codigo que
	 * ya se tiene dentro del SDA
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param moviles
	 *            {@link String} en formato XML
	 * @return {@link String} en formato XML
	 * */
	public String respuestaBajaMasivas(String moviles) {
		try 
		{
			leerInsumos(moviles);
		} catch (Exception e) {
			if (getRespuesta() == null)
				setRespuesta(getGestion().xmlError(ErroresSDA.ERROR_GENERICO));
			return getRespuesta();
		}
		
		try {
			return getGestion().depuracionBajaMasiva(getListaMoviles(),	"Servicio Web", true);
		} finally {
			setGestion(null);
		}
	}

	/**
	 * Metodo que realizara el ingreso de numeros a la lista negra con codigo
	 * que ya se tiene dentro del SDA
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param moviles
	 *            {@link String} en formato XML
	 * @return {@link String} en formato XML
	 * */
	public String respuestaIngresoListaNegra(String moviles) {
		try 
		{
			leerInsumos(moviles);
		} catch (Exception e) {
			if (getRespuesta() == null)
				setRespuesta(getGestion().xmlError(ErroresSDA.ERROR_GENERICO));
			return getRespuesta();
		}
		
		try {
			// el nombre 'Alta Servicio Web' de sirve de bandera para realizar el alta
			return getGestion().depuracionBajaMasiva(getListaMoviles(),	"Alta Servicio Web", true);
		} finally {
			setGestion(null);
		}
	}

	/**
	 * Metodo que leera el archivo XML donde vienen los numeros moviles a
	 * depurar
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param insumoXML
	 *            {@link String} en formato XML
	 * @return {@link List} de numeros mviles
	 * */
	private void leerInsumos(String insumoXML) {

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = builderFactory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(
					new ByteArrayInputStream(insumoXML.getBytes("utf-8"))));
			doc.getDocumentElement().normalize();
			if (doc.getDocumentElement().hasChildNodes()) {
				NodeList nodeList = doc.getDocumentElement().getChildNodes();
				setListaMoviles(new ArrayList<String>());
				lecturaListaTelefonos(nodeList, "movil");
			}
		} catch (Exception e) {
			// si no esta nula es porque si se obtuvo antes
			if (getRespuesta() == null)
				setRespuesta(getGestion().xmlError(ErroresSDA.ERROR_GENERICO));
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
	 *            {@link String} nombre del nodo que andamos buscando dentro del
	 *            listado
	 * @return {@link Void}
	 * @throws Exception
	 * */
	private void lecturaListaTelefonos(NodeList nodeList,
			String nodeNameToReader) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getNodeName().equalsIgnoreCase(nodeNameToReader)) {
					if (node.getFirstChild() != null
							|| node.getFirstChild().getNodeValue() != null) {
						listaMoviles.add(node.getTextContent());
					}
				}
				// recursive
				if (node.hasChildNodes())
					lecturaListaTelefonos(node.getChildNodes(),
							nodeNameToReader);
			}
		}
	}

	/**
	 * @return the listaMoviles
	 */
	private List<String> getListaMoviles() {
		return listaMoviles;
	}

	/**
	 * @param listaMoviles
	 *            the listaMoviles to set
	 */
	private void setListaMoviles(List<String> listaMoviles) {
		this.listaMoviles = listaMoviles;
	}

	/**
	 * @return the gestion
	 */
	private GestionarParametrizacion getGestion() {
		return gestion;
	}

	/**
	 * @param gestion
	 *            the gestion to set
	 */
	private void setGestion(GestionarParametrizacion gestion) {
		this.gestion = gestion;
	}

	/**
	 * @return the respuesta
	 */
	private String getRespuesta() {
		return respuesta;
	}

	/**
	 * @param respuesta
	 *            the respuesta to set
	 */
	private void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

}
