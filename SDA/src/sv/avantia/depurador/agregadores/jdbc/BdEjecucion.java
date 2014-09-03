package sv.avantia.depurador.agregadores.jdbc;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * Clase encargada de poder realizar las operaciones en la base de datos dentro
 * de la ejecución del flujo
 * 
 * @author Edwin Mejia - Avantia Consultores
 * @version 1.0
 * */
public class BdEjecucion {

	/**
	 * Obtener el appender para la impresión en un archivo de LOG
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * */
	public static Logger logger = Logger.getLogger("avantiaLogger");
	

	/**Metodo que nos servira para realizar cualquier consulta dentro de la
	 * base de datos
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param query
	 *            {String} dato de insumo para obtener un listado desde la BD
	 * @return {java.util.List}
	 * */
	public List<?> listData(String query) {
		Session session = SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		List<?> objs = null;
		try 
		{
			
			session.beginTransaction();
			objs = session.createQuery(query).list();
			session.getTransaction().commit();
		
		} catch (RuntimeException e) {
			logger.error("Error al querer realizar una consulta en la base de datos", e);
			
			if (session.getTransaction() != null && session.getTransaction().isActive()) {
				try 
				{
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.error("Error al querer realizar rolback a la base de datos", e1);
				}
				// throw again the first exception
				throw e;
			}
		}
		return objs;

	}

	/**
	 * Metodo que nos servira para realizar cualquier eliminación dentro de la
	 * base de datos
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param obj
	 *            {java.lang.Object} return void
	 * */
	public void deleteData(Object obj) {
		Session session = SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		try 
		{
			
			session.beginTransaction();
			session.delete(obj);
			session.getTransaction().commit();
		
		} catch (RuntimeException e) {
			logger.error("Error al querer realizar una eliminación en la base de datos", e);
			
			if (session.getTransaction() != null && session.getTransaction().isActive()) {
				try 
				{
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.error("Error al querer realizar rolback a la base de datos", e1);
				}
				// throw again the first exception
				throw e;
			}
		}
	}

	/**
	 * Metodo que nos servira para realizar cualquier inserción dentro de la
	 * base de datos
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param obj
	 *            {java.lang.Object} return void
	 * */
	public void createData(Object obj) {
		Session session = SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		try 
		{
			
			session.beginTransaction();
			session.save(obj);
			session.getTransaction().commit();
		
		} catch (RuntimeException e) {
			logger.error("Error al querer realizar una insercion a la base de datos", e);

			if (session.getTransaction() != null && session.getTransaction().isActive()) {
				try 
				{
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.error("Error al querer realizar rolback a la base de datos", e1);
				}
				// throw again the first exception
				throw e;
			}
		}
	}

	/**
	 * Metodo que nos servira para realizar cualquier actualización dentro de la
	 * base de datos
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param obj
	 *            {java.lang.Object} return void
	 * */
	public void updateData(Object obj) {
		Session session = SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		try 
		{
			
			session.beginTransaction();
			session.update(obj);
			session.getTransaction().commit();
		
		} catch (RuntimeException e) {
			logger.error("Error al querer realizar una actualización a la base de datos", e);
			
			if (session.getTransaction() != null && session.getTransaction().isActive()) {
				try 
				{
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.error("Error al querer realizar rolback a la base de datos", e1);
				}
				// throw again the first exception
				throw e;
			}
		}
	}

}