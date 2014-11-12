package sv.avantia.depurador.agregadores.hilo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.utileria.ErroresSDA;

public class ConsultarHTTPS extends Consultar {

	//*********************************************************************************************************************************/
	//		cliente SSL
	//*********************************************************************************************************************************/

	private SOAPMessage response = null;
	private URL url;
	private SOAPConnection connection = null;
	private SOAPConnectionFactory scf = null;
	private Document salidaError = null;
	SOAPMessage message = null;
	
	/**
	 * Metodo que nos carga un certificado digital
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @return {@link Void}
	 * */
	static private void doTrustToCertificates() throws Exception {
		// Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		TrustManager[] trustAllCerts = new TrustManager[] { 
				new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
		
					public void checkServerTrusted(X509Certificate[] certs,
							String authType) throws CertificateException {
						return;
					}
		
					public void checkClientTrusted(X509Certificate[] certs,
							String authType) throws CertificateException {
						return;
					}
				} 
		};

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
					logger.warn("Warning: URL host '" + urlHostName	+ "' is different to SSLSession host '"	+ session.getPeerHost() + "'.");
				}
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
		
	/**
	 * Metodo que realiza el envio del archivo request y espera el archivo
	 * response para el metodo web
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param  metodo {@link Metodos}
	 * @return {@link Void}
	 * */
	public Document invoke(Metodos metodo, long timeOutMillisecond) 
	{
		MessageFactory messageFactory = null;
		SOAPMessage msg = null;
		try 
		{
			messageFactory = MessageFactory.newInstance();
			msg = messageFactory.createMessage(
					new MimeHeaders(),
					new ByteArrayInputStream(metodo.getInputMessageText().getBytes(Charset.forName("UTF-8")))
															);
		} 
		catch (Exception e) 
		{
			logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_INYECTAR_MENSAJE_ENVIO_EN_EL_METODO_CON_SEGURIDAD.getDescripcion() + " " + e.getMessage(),e);
			return xmlErrorSDA(ErroresSDA.ERROR_AL_INYECTAR_MENSAJE_ENVIO_EN_EL_METODO_CON_SEGURIDAD);
		}
			
		try 
		{
			// Trust to certificates
			doTrustToCertificates();
		} 
		catch (Exception e) 
		{
			logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_VERIFICAR_LOS_CERTIFICADOS_DE_SEGURIDAD.getDescripcion() + " " + e.getMessage(), e);
			return xmlErrorSDA(ErroresSDA.ERROR_AL_VERIFICAR_LOS_CERTIFICADOS_DE_SEGURIDAD);
		}
		message = msg;
		return sendMessage(metodo.getEndPoint(), timeOutMillisecond);		
	}
	
	/**
	 * Metodo que envia realiza la conexion al servicio web e invoca al metodo a
	 * ejecutar
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param message
	 *            - {@link SOAPMessage}
	 * @param endPoint
	 *            - {@link String}
	 * @return {@link SOAPMessage}
	 * @throws SOAPException 
	 * @throws UnsupportedOperationException 
	 * @throws IOException 
	 * @throws TransformerException 
	 * @throws TransformerConfigurationException 
	 * */
	private Document sendMessage(String endPoint, long timeOutMillisecond) 
	{
		if (endPoint != null && message != null) 
		{
			try {
				url = new URL(endPoint);
			} catch (MalformedURLException e) {
				logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_CREAR_ENDPOINT_CON_EL_INSUMO_OBTENIDO.getDescripcion() + " " + endPoint + " - " + e.getMessage(), e);
				return xmlErrorSDA(ErroresSDA.ERROR_AL_CREAR_ENDPOINT_CON_EL_INSUMO_OBTENIDO);
			}
			
			try {
				scf = SOAPConnectionFactory.newInstance();
			} catch (UnsupportedOperationException e) {
				logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_CREAR_CONEXION_FACTORY_HTTPS.getDescripcion() + " " + e.getMessage(), e);
				return xmlErrorSDA(ErroresSDA.ERROR_AL_CREAR_CONEXION_FACTORY_HTTPS);
			} catch (SOAPException e) {
				logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_CREAR_CONEXION_FACTORY_HTTPS.getDescripcion() + " " + e.getMessage(), e);
				return xmlErrorSDA(ErroresSDA.ERROR_AL_CREAR_CONEXION_FACTORY_HTTPS);
			}
			
			try {
				connection = scf.createConnection(); // point-to-point connection
			} catch (Exception e) {
				if (connection != null) 
				{
					try 
					{
						connection.close();
					} 
					catch (SOAPException soape) 
					{
						logger.warn(getAgregador().getNombre_agregador() + " Verificar porque no se pudo dar CLOSE a la conexion httpClient " + soape.getMessage());
					}
				}
				
				logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_CREANDO_CONEXION_HTTPS.getDescripcion() + " " + e.getMessage(), e);
				return xmlErrorSDA(ErroresSDA.ERROR_CREANDO_CONEXION_HTTPS);
			}
			
			Thread taskInvoke;
			try {
				Runnable run = new Runnable() {

					public void run() {
						try {
							//invocamos el metodo web del Servicio
							response = connection.call(message, url);
						} catch (Exception e) {
							logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_INVOCAR_EL_METODO_CON_SEGURIDAD.getDescripcion() + " " + e.getMessage() ,e);
							salidaError = xmlErrorSDA(ErroresSDA.ERROR_AL_INVOCAR_EL_METODO_CON_SEGURIDAD);
						}
					}

				};

				taskInvoke = new Thread(run, "AInvocacionWebService");
				taskInvoke.start();

				int m_seconds = 1;
				int contSeconds = 0;
				while (true) {
					Thread.sleep(m_seconds * 1000);
					contSeconds += m_seconds;
					if (response != null) {
						break;
					}
					if (contSeconds >= timeOutMillisecond) {
						taskInvoke.interrupt();
						logger.error(getAgregador().getNombre_agregador() + " SE GENERO TIMEOUT EXCEPCION INVOCAR EL METODO CON SEGURIDAD");
						salidaError = xmlErrorSDA(ErroresSDA.ERROR_TIMEUP_EXCEPTION);
						break;
					}
				}
				if (taskInvoke.isAlive()) {
					taskInvoke.interrupt();
				}

				if(salidaError!=null)
					return salidaError;
				else
					return toDocument(response);
				
			} catch (Exception e) {
				//call = null;
				return xmlErrorSDA(ErroresSDA.ERROR_AL_CONSULTAR_TIMEUP_EN_EL_METODO_SIN_SEGURIDAD);
			}
			finally 
			{
				if (connection != null) 
				{
					try 
					{
						connection.close();
					} 
					catch (SOAPException e) 
					{
						logger.warn(getAgregador().getNombre_agregador() + " Verificar porque no se pudo dar CLOSE a la conexion httpClient " + e.getMessage());
					}
				}
			}
			
	       /* Thread taskInvoke;
			
			Runnable run = new Runnable() {
				public void run() {
					try 
					{
						response = connection.call(message, url);
				    } 
					catch (SOAPException e) 
					{
						if (connection != null) 
						{
							try 
							{
								connection.close();
							} 
							catch (SOAPException soape) 
							{
								logger.warn(getAgregador().getNombre_agregador() + " Verificar porque no se pudo dar CLOSE a la conexion httpClient " + soape.getMessage());
							}
						}
						logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_INVOCAR_EL_METODO_CON_SEGURIDAD.getDescripcion() + " " + e.getMessage() ,e);
						salidaError = xmlErrorSDA(ErroresSDA.ERROR_AL_INVOCAR_EL_METODO_CON_SEGURIDAD);
						
					}
				}

			};

			taskInvoke = new Thread(run, "ServicioWebHttps");
			taskInvoke.start();

	        try
	        {
	        	long endTimeOut = System.currentTimeMillis() + timeOutMillisecond;
	        	
	        	while (true) 
				{
					if (response != null) 
					{
						break;
					}
					if (System.currentTimeMillis() > endTimeOut) 
					{
						logger.error(getAgregador().getNombre_agregador() + " SE GENERO TIMEOUT EXCEPCION INVOCAR EL METODO SIN SEGURIDAD");
						salidaError = xmlErrorSDA(ErroresSDA.ERROR_TIMEUP_EXCEPTION);
						taskInvoke.stop();
						break;
					}
				}
				if (taskInvoke.isAlive()) 
				{
					taskInvoke.stop();
				}
				
				if(salidaError!=null)
					return salidaError;
				else
					return toDocument(response);
				
			} 
	        catch (Exception e) 
	        {
				logger.error(getAgregador().getNombre_agregador() + " " + ErroresSDA.ERROR_AL_CONSULTAR_TIMEUP_EN_EL_METODO_SIN_SEGURIDAD.getDescripcion() + " " + e.getMessage(), e);
				return xmlErrorSDA(ErroresSDA.ERROR_AL_CONSULTAR_TIMEUP_EN_EL_METODO_SIN_SEGURIDAD);
			}
			finally 
			{
				if (connection != null) 
				{
					try 
					{
						connection.close();
					} 
					catch (SOAPException e) 
					{
						logger.warn(getAgregador().getNombre_agregador() + " Verificar porque no se pudo dar CLOSE a la conexion httpClient " + e.getMessage());
					}
				}
			}*/
		}
		return xmlErrorSDA(ErroresSDA.ERROR_NULLPOINTEREXCEPTION);
	}
}
