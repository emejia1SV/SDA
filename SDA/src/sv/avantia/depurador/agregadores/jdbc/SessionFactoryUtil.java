package sv.avantia.depurador.agregadores.jdbc;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class SessionFactoryUtil {

	/**
	 * Obtener el appender para la impresión en un archivo de LOG
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	static Logger logger = Logger.getLogger("avantiaLogger");

	/**
	 * Creación del SessionFactory
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	static SessionFactory sessionAnnotationFactory;
	
	
	/**
	 * Metodo que nos generara la SessionFactory desde la configuracion de
	 * anotaciones en el archivo hibernate-annotation.cfg.xml
	 * 
	 * @author Edwin Mejia - Avantia Consultores 
	 * @return SessionFactory
	 * */
	static SessionFactory buildSessionAnnotationFactory() 
	{
		try 
		{
			Configuration configuration = new Configuration();
			configuration.configure("hibernate-annotation.cfg.xml");
			ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
			SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
			return sessionFactory;		
		} 
		catch (Throwable ex) 
		{
			logger.error("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	/**
	 * Metodo estatico que nos servira para obtener la SessionFactory desde
	 * cualquier lado en la aplicacion para poder realizar operaciones en la
	 * base de datos
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @return SessionFactory
	 * */
	public static SessionFactory getSessionAnnotationFactory() 
	{
		if (sessionAnnotationFactory == null)
			sessionAnnotationFactory = buildSessionAnnotationFactory();
		
		return sessionAnnotationFactory;
	}
}
