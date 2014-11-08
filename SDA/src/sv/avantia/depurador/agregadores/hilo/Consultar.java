package sv.avantia.depurador.agregadores.hilo;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.utileria.ErroresSDA;

public class Consultar {

	/**
	 * Instancia del insumo {@link Agregadores} que se espera recibir y se espera nunca llegue a
	 * esta instancia nulo y este es el que obtendra todo el insumo de la
	 * parametrizacion para la consulta a los agregadores
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	private Agregadores agregador = null;
	
	/**
	 * Obtener el appender para la impresión en un archivo de LOG
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	protected static Logger logger = Logger.getLogger("avantiaLogger");
	
	/**
	 * Metodo que me generara el String en formato XML como respuesta para poder
	 * devolver cualquier excepcion que sea expuesta a esta altura de clases de
	 * invoacion a los agregadores
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param error
	 *            {@link ErroresSDA} return String con formato XML
	 * @return {@link Document}
	 * */
	protected Document xmlErrorSDA(ErroresSDA error)
	{
		return getdocumentFromString(xmlErrorSDAString(error));
	}
	
	/**
	 * Metodo que me generara el String en formato XML como respuesta para poder
	 * devolver cualquier excepcion que sea expuesta a esta altura de clases de
	 * invoacion a los agregadores
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param error
	 *            {@link ErroresSDA} return String con formato XML
	 * @return {@link String}
	 * */
	private String xmlErrorSDAString(ErroresSDA error)
	{
		return "<respuesta><errorSDA><codigoError>"+error.getCodigo()+"</codigoError><descripcionEstado>"+error.getDescripcion()+"</descripcionEstado></errorSDA></respuesta>";
	}
	
	/**
	 * Metodo para la transformación de un {@link Document} a un {@link String}
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param soapMsg
	 *            {@link SOAPMessage}
	 * @return {@link Document}
	 * */
	protected String getStringFromDocument(Document doc)
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
	       return xmlErrorSDAString(ErroresSDA.ERROR_PASANDO_DOCUMENT_A_CADENA_TEXTO);
	    }
	}
	
	/**
	 * Metodo para la transformacion de un {@link String} a un {@link Document}
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param doc
	 *            {@link String} return {@link Document}
	 * */
	protected Document getdocumentFromString(String doc)
	{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new ByteArrayInputStream(doc.getBytes("utf-8")))  );
			return document;
		} catch (Exception e) {
			logger.error("NO SE PUEDE GENERAR EL PARSEO DE STRING A DOCUMENT");
			return null;
		}
	}
	
	/**
	 * Metodo para la transformación de un {@link SOAPMessage} Response a un
	 * {@link Document} y hacer mas facil la busqueda del dato
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param soapMsg
	 *            {@link SOAPMessage}
	 * @return {@link Document}
	 * */
	protected Document toDocument(SOAPMessage soapMsg) 
	{
		try {
			Source src = soapMsg.getSOAPPart().getContent();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			DOMResult result = new DOMResult();
			transformer.transform(src, result);
			return (Document) result.getNode();
		} catch (Exception e) {
			return xmlErrorSDA(ErroresSDA.ERROR_PASANDO_SOAPMESSAGE_RESPONSE_A_DOCUMENT);
		}
	}

	/**
	 * @return the agregador
	 */
	protected Agregadores getAgregador() {
		return agregador;
	}

	/**
	 * @param agregador the agregador to set
	 */
	public void setAgregador(Agregadores agregador) {
		this.agregador = agregador;
	}

}
