package sv.avantia.depurador.agregadores.hilo;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;

import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.utileria.ErroresSDA;

import com.cladonia.xml.webservice.soap.SOAPClient;
import com.cladonia.xml.webservice.soap.SOAPException;

public class ConsultarHTTP extends Consultar {

	//*********************************************************************************************************************************/
	//		cliente SIN SSL es un cliente normal sin seguridad
	//*********************************************************************************************************************************/

	/**
	 * Invoke a SOAP call passing in an operation instance and attachments
	 * 
	 * @param metodo
	 *            {@link Metodos} The selected operation
	 * @param timeOutMillisecond
	 *            tiempo estimado para timeout excepcion
	 * 
	 * @return The response SOAP Envelope as a String
	 */
	public Document invoke(Metodos metodo, long timeOutMillisecond)
	{
		Document docRequest = getdocumentFromString(metodo.getInputMessageText());;

		if(docRequest==null)
			return xmlErrorSDA(ErroresSDA.ERROR_PASANDO_DE_CADENA_TEXTO_A_DOCUMENT);
		
		// create the saaj based soap client
		SOAPClient client;
		try {
			client = new SOAPClient(docRequest);
		} catch (SOAPException e) {
			logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_CREAR_SOAP_CLIENT.getDescripcion() + " " + e.getMessage(), e);
			return xmlErrorSDA(ErroresSDA.ERROR_AL_CREAR_SOAP_CLIENT);
		}

		// set the SOAPAction
		client.setSOAPAction(metodo.getSoapActionURI());

		// get the url
		URL url;
		try {
			url = new URL(metodo.getEndPoint());
		} catch (MalformedURLException e) {
			logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_CREAR_ENDPOINT_CON_EL_INSUMO_OBTENIDO.getDescripcion() + " " + metodo.getEndPoint() + " - " + e.getMessage(), e);
			return xmlErrorSDA(ErroresSDA.ERROR_AL_CREAR_ENDPOINT_CON_EL_INSUMO_OBTENIDO);
		}
		Document response = null;
		
		try 
		{
        	// Tratar respuesta del servidor
			response = client.send(url);
	    } 
		catch (Exception e) 
		{
			logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_INVOCAR_EL_METODO_SIN_SEGURIDAD.getDescripcion() + " " + e.getMessage(),e);
	        return xmlErrorSDA(ErroresSDA.ERROR_AL_INVOCAR_EL_METODO_SIN_SEGURIDAD);
		}
	        
        try{
			long endTimeOut = System.currentTimeMillis() + timeOutMillisecond;
			while(true){
				if(System.currentTimeMillis() > endTimeOut){
					logger.error(getAgregador().getNombre_agregador() + " SE GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD");
					return xmlErrorSDA(ErroresSDA.ERROR_TIMEUP_EXCEPTION);
				}else{	
					if (response != null) {
						break;
					}
				}
			}
			return response;
		} catch (Exception e) {
			logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_CONSULTAR_TIMEUP_EN_EL_METODO_SIN_SEGURIDAD.getDescripcion() + " " + e.getMessage(), e);
			return xmlErrorSDA(ErroresSDA.ERROR_AL_CONSULTAR_TIMEUP_EN_EL_METODO_SIN_SEGURIDAD);
		}			
		
	}
}
