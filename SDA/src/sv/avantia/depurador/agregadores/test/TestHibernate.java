package sv.avantia.depurador.agregadores.test;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.Clientes_Tel;
import sv.avantia.depurador.agregadores.entidades.LogDepuracion;
import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.entidades.Pais;
import sv.avantia.depurador.agregadores.entidades.Parametros;
import sv.avantia.depurador.agregadores.entidades.Respuesta;
import sv.avantia.depurador.agregadores.jdbc.BdEjecucion;
import sv.avantia.depurador.agregadores.jdbc.SessionFactoryUtil;

public class TestHibernate {

	/* Get actual class name to be printed on */
	public static Logger logger = Logger.getLogger(TestHibernate.class);

	/**
	 * @param args
	 */	
	public static void main(String[] args) {
		try {
			
			llenarTablaLogs();
			
			if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
	        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
	        	
	        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
	        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	        
		} catch (Exception e) {
			if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
	        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
	        	
	        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
	        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	        
	        e.printStackTrace();
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	public static void llenarTablaLogs() {
		try 
		{
			List<LogDepuracion> depuracions = ((List<LogDepuracion>) (List<?>) listData("FROM SDA_LOG_DEPURACION"));
			System.out.println(depuracions.size());
			for (LogDepuracion logDepuracion : depuracions) {
				System.out.println(logDepuracion.getMetodo().getId());
			}
		} 
		catch (Exception e) 
		{
			System.err.println("Error:No se pudo cargar la tabla de depuracion");
		}
	}
	
	
	public static void testSMTSave() {
		
		try {
			
		

		Pais pais = new Pais();
		pais.setId(2);
		pais.setCodigo("502");
		pais.setNombre("Guatemala");
		pais.setEstado(1);
		
		createData(pais);
		
		Agregadores agregador = new Agregadores();
		agregador.setEstado(1);
		agregador.setPais(pais);
		agregador.setId(4);
		agregador.setNombre_agregador("SMT");
		
		createData(agregador);

		Metodos metodo2=new Metodos();
		metodo2.setAgregador(agregador);
		metodo2.setContrasenia("x9t/yLcnC3VYKCb6v0uezTwYJNk=");
		metodo2.setId(10);
		metodo2.setInputMessageName("AddGrayListRequest");
		metodo2.setInputMessageText("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:loc=\"http://www.csapi.org/schema/parlayx/blackgray/v1_0/local\">   <soapenv:Header>      <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">         <wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">            <wsse:Username>PA00000737</wsse:Username>            <wsse:Password Type=\"...#PasswordDigest\">x9t/yLcnC3VYKCb6v0uezTwYJNk=</wsse:Password>            <wsse:Nonce>096459e93f20a2b39ab6c5ddd493e44f58bc3a91</wsse:Nonce>            <wsse:Created>2012-10-17T11:51:50.263Z</wsse:Created>         </wsse:UsernameToken>      </wsse:Security>      <tns:RequestSOAPHeader xmlns:tns=\"http://www.huawei.com.cn/schema/common/v2_1\">         <tns:AppId>35000001000001</tns:AppId>         <tns:TransId>2014011716010012345</tns:TransId>         <tns:OA>tel:50279451598</tns:OA>         <tns:FA>tel:50279451598</tns:FA>      </tns:RequestSOAPHeader>   </soapenv:Header>   <soapenv:Body>      <loc:addGrayList>         <loc:version>1.0</loc:version>         <loc:grayList>            <grayee>               <msisdn>50279451598</msisdn>				                        </grayee>         </loc:grayList>      </loc:addGrayList>   </soapenv:Body></soapenv:Envelope>");
		metodo2.setNamespaceURI("http://www.csapi.org/wsdl/parlayx/blackgray/v1_0/interface");
		//metodo2.setNombre("addGrayList");
		metodo2.setServiceName("SMT_service");
		metodo2.setSoapActionURI("loc:addGrayList");
		metodo2.setStyle("document");
		metodo2.setTargetMethodName("addGrayList");
		metodo2.setTargetObjectURI(null);
		metodo2.setTargetURL("https://hub.americamovil.com/sag/services/blackgrayService");
		metodo2.setUsuario("PA00000737");
		metodo2.setWsdl_Agregador("https://hub.americamovil.com/sag/services/blackgrayService?wsdl");
		
		createData(metodo2);
		
		Parametros parametros1 = new Parametros();
		parametros1.setId(20);
		parametros1.setMetodo(metodo2);
		parametros1.setNombre("_*tns:FA_*");
		//parametros1.setTipo("java.lang.String");
		
		Parametros parametros2 = new Parametros();
		parametros2.setId(21);
		parametros2.setMetodo(metodo2);
		parametros2.setNombre("_*tns:OA_*");
		//parametros2.setTipo("java.lang.String");
		
		Parametros parametros3 = new Parametros();
		parametros3.setId(22);
		parametros3.setMetodo(metodo2);
		parametros3.setNombre("_*msisdn_*");
		//parametros3.setTipo("java.lang.String");
		
		createData(parametros1);
		createData(parametros2);
		createData(parametros3);
		
		Respuesta respuesta2 = new Respuesta();
		respuesta2.setId(1);
		respuesta2.setNombre("AddGrayListResponse");
		//respuesta.setPosicion(2);
		//respuesta2.setTipo("java.lang.String");
		respuesta2.setMetodo(metodo2);
		
		createData(respuesta2);
		
		Metodos metodo3=new Metodos();
		metodo3.setAgregador(agregador);
		metodo3.setContrasenia("x9t/yLcnC3VYKCb6v0uezTwYJNk=");
		metodo3.setId(11);
		metodo3.setInputMessageName("DeleteGrayListRequest");
		metodo3.setInputMessageText("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:loc=\"http://www.csapi.org/schema/parlayx/blackgray/v1_0/local\">   <soapenv:Header>      <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">         <wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">            <wsse:Username>PA00000737</wsse:Username>            <wsse:Password Type=\"...#PasswordDigest\">x9t/yLcnC3VYKCb6v0uezTwYJNk=</wsse:Password>            <wsse:Nonce>096459e93f20a2b39ab6c5ddd493e44f58bc3a91</wsse:Nonce>            <wsse:Created>2012-10-17T11:51:50.263Z</wsse:Created>         </wsse:UsernameToken>      </wsse:Security>      <tns:RequestSOAPHeader xmlns:tns=\"http://www.huawei.com.cn/schema/common/v2_1\">         <tns:AppId>35000001000001</tns:AppId>         <tns:TransId>2014011716010012345</tns:TransId>         <tns:OA>tel:50258260744</tns:OA>         <tns:FA>tel:50258260744</tns:FA>      </tns:RequestSOAPHeader>   </soapenv:Header>   <soapenv:Body>      <loc:deleteBlackList>         <loc:version>1.0</loc:version>         <loc:blackList>         <balckee>               <msisdn>50258260744</msisdn>               <serviceID></serviceID>               <createTime>20111121112200</createTime>            </balckee>         </loc:blackList>         <loc:extensionInfo>            <NamedParameters>               <key>name</key>               <value>value</value>            </NamedParameters>         </loc:extensionInfo>      </loc:deleteBlackList>   </soapenv:Body></soapenv:Envelope>");
		metodo3.setNamespaceURI("http://www.csapi.org/wsdl/parlayx/blackgray/v1_0/interface");
		//metodo3.setNombre("deleteGrayList");
		metodo3.setServiceName("SMT_service");
		metodo3.setSoapActionURI("loc:deleteGrayList");
		metodo3.setStyle("document");
		metodo3.setTargetMethodName("deleteGrayList");
		metodo3.setTargetObjectURI(null);
		metodo3.setTargetURL("https://hub.americamovil.com/sag/services/blackgrayService");
		metodo3.setUsuario("PA00000737");
		metodo3.setWsdl_Agregador("https://hub.americamovil.com/sag/services/blackgrayService?wsdl");
		
		createData(metodo3);
		
		Parametros parametros4 = new Parametros();
		parametros4.setId(23);
		parametros4.setMetodo(metodo2);
		parametros4.setNombre("_*tns:FA_*");
		//parametros4.setTipo("java.lang.String");
		
		Parametros parametros5 = new Parametros();
		parametros5.setId(24);
		parametros5.setMetodo(metodo2);
		parametros5.setNombre("_*tns:OA_*");
		//parametros5.setTipo("java.lang.String");
		
		Parametros parametros6 = new Parametros();
		parametros6.setId(25);
		parametros6.setMetodo(metodo2);
		parametros6.setNombre("_*msisdn_*");
		//parametros6.setTipo("java.lang.String");
		
		createData(parametros4);
		createData(parametros5);
		createData(parametros6);
		
		Respuesta respuesta3 = new Respuesta();
		respuesta3.setId(2);
		respuesta3.setNombre("DeleteGrayListResponse");
		//respuesta3.setTipo("java.lang.String");
		respuesta3.setMetodo(metodo3);
		
		createData(respuesta3);

        SessionFactoryUtil.closeSession();
        
        if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
        	
        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
        	SessionFactoryUtil.getSessionAnnotationFactory().close();
        
		} catch (Exception e) {
			if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
	        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
	        	
	        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
	        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	        
	        e.printStackTrace();
		}
	}
	
	
	
	
	@SuppressWarnings("unused")
	private static void load100() {
		
		
		for (int i = 0; i < 100; i++) {
			LogDepuracion depuracion = new LogDepuracion();
			
			depuracion.setNumero(String.valueOf(i));
			createData(depuracion);
			
		}
		SessionFactoryUtil.closeSession();
	}

	@SuppressWarnings("unused")
	private static void load200() {
		for (int i = 101; i < 200; i++) {
			LogDepuracion depuracion = new LogDepuracion();
			
			depuracion.setNumero(String.valueOf(i));
			createData(depuracion);
		}
		SessionFactoryUtil.closeSession();
	}

	@SuppressWarnings("unused")
	private static void load300() {
		for (int i = 201; i < 300; i++) {
			LogDepuracion depuracion = new LogDepuracion();
			
			depuracion.setNumero(String.valueOf(i));
			createData(depuracion);
		}
		SessionFactoryUtil.closeSession();
	}
	
	public static void datos(){
		BdEjecucion ejecucion = new BdEjecucion();
		try {
			@SuppressWarnings("unchecked")
			List<String> datos = (List<String>)(List<?>) ejecucion.listData("select b.numero from CLIENTE_TEL b where b.id='287040'");
			for (int i = 0; i < datos.size(); i++) {
				System.out.println(datos.get(i));
			}
		} finally{
			ejecucion = null;
		}
	}
	
	public static void obtenerParmetrizacion(){
		@SuppressWarnings({ "unchecked", "unused" })
		List<Pais> datos = listData("FROM SDA_PAISES");
		SessionFactoryUtil.closeSession();

	}
	
	public static void obtenerNumeros(){
		@SuppressWarnings("unchecked")
		List<Clientes_Tel> numeros = listData("FROM CLIENTE_TEL  WHERE ID BETWEEN 0 AND 20");
		
		for (int i = 0; i < numeros.size(); i++) {
			System.out.println(numeros.get(i).getNumero());
		}
		
		SessionFactoryUtil.closeSession();

	}
	
	public static void obtenerPaises(){
		@SuppressWarnings("unchecked")
		List<Pais> pais = listData("FROM SDA_PAISES");
		
		for (int i = 0; i < pais.size(); i++) {
			System.out.println(pais.get(i).getNombre());
		}
		
		SessionFactoryUtil.closeSession();
	}
	
	public static void testDeleteCascade(){
		Pais pais = new Pais();
		pais.setId(2);
		pais.setCodigo("502");
		pais.setNombre("Guatemala");
		pais.setEstado(1);
		deleteData(pais);


        if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
        	
        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	}
	
	public static void test0() throws Exception{
		Agregadores agregadores = new Agregadores();
		System.out.println(maxValue(agregadores));
	}
	
	@SuppressWarnings("unchecked")
	public static void test1(){
		String query = "FROM SDA_AGREGADORES";
		
		Agregadores obj = new Agregadores();
	    obj.setEstado(1);
	    //obj.setIdPais(1);
	    obj.setId(17);
	    obj.setNombre_agregador("prueba de insercion hibernate");
         
        createData(obj);
        
        List<Agregadores> objs = listData(query);
        for (Agregadores agregador : objs) {
			if(agregador.getId()==17){
				agregador.setNombre_agregador("Cambio de nombre");
				updateData(agregador);
			}
		}
        
        objs = listData(query);
        for (Agregadores agregador : objs) {
			System.out.println(agregador.getNombre_agregador());
		}
        
        deleteData(obj);
        
        objs = listData(query);
        for (Agregadores agregador : objs) {
			if(agregador.getId()==13){
				agregador.setNombre_agregador("Cambio de nombre");
				updateData(agregador);
			}
		}
        
        if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
        	
        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	}
	
	public static void test2() {

		Agregadores obj = new Agregadores();
		obj.setEstado(1);
		//obj.setIdPais(1);
		obj.setId(10);
		obj.setNombre_agregador("prueba de insercion hibernate");
		
		createData(obj);

		/*Servicios servicio = new Servicios();
		servicio.setContrasenia("uno");
		servicio.setUsuario("Edwin");
		servicio.setId(10);
		servicio.setAgregador(obj);
		servicio.setWsdl_Agregador("http://192.168.0.100:8090/axis2/services/pruebaWsCadena?wsdl");

		Servicios servicio2 = new Servicios();
		servicio2.setContrasenia("dos");
		servicio2.setId(11);
		servicio2.setUsuario("Edwin");
		servicio2.setAgregador(obj);
		servicio2.setWsdl_Agregador("http://192.168.0.100:8090/axis2/services/pruebaWsCadena?wsdl");
		
		createData(servicio);
		createData(servicio2);*/
		
		
		

        if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
        	
        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	}
	
	public static void test3() {

		Pais pais = new Pais();
		pais.setId(3);
		pais.setCodigo("504");
		pais.setNombre("Honduras");
		pais.setEstado(1);
		
		createData(pais);
		
		Agregadores obj = new Agregadores();
		obj.setEstado(1);
		obj.setPais(pais);
		obj.setId(10);
		obj.setNombre_agregador("prueba de insercion hibernate");
		
		createData(obj);

		/*Servicios servicio = new Servicios();
		servicio.setContrasenia("uno");
		servicio.setUsuario("Edwin");
		servicio.setId(10);
		servicio.setAgregador(obj);
		servicio.setWsdl_Agregador("http://192.168.0.100:8090/axis2/services/pruebaWsCadena?wsdl");

		Servicios servicio2 = new Servicios();
		servicio2.setContrasenia("dos");
		servicio2.setId(11);
		servicio2.setUsuario("Edwin");
		servicio2.setAgregador(obj);
		servicio2.setWsdl_Agregador("http://192.168.0.100:8090/axis2/services/pruebaWsCadena?wsdl");
		
		createData(servicio);
		createData(servicio2);
		*/
		
		

        if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
        	
        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	}
	
	public static void test4() {

		Pais pais = new Pais();
		pais.setId(3);
		pais.setCodigo("504");
		pais.setNombre("Honduras");
		pais.setEstado(1);
		
		createData(pais);
		
		Agregadores obj = new Agregadores();
		obj.setEstado(1);
		obj.setPais(pais);
		obj.setId(10);
		obj.setNombre_agregador("prueba de insercion hibernate");
		
		createData(obj);

		/*Servicios servicio = new Servicios();
		servicio.setContrasenia("uno");
		servicio.setUsuario("Edwin");
		servicio.setId(10);
		servicio.setAgregador(obj);
		servicio.setWsdl_Agregador("http://192.168.0.100:8090/axis2/services/pruebaWsCadena?wsdl");

		Servicios servicio2 = new Servicios();
		servicio2.setContrasenia("dos");
		servicio2.setId(11);
		servicio2.setUsuario("Edwin");
		servicio2.setAgregador(obj);
		servicio2.setWsdl_Agregador("http://192.168.0.100:8090/axis2/services/pruebaWsCadena?wsdl");
		
		createData(servicio);
		createData(servicio2);*/
		
		/*Metodos metodo=new Metodos();
		metodo.setId(7);
		metodo.setNombre("metodoPrueba");
		metodo.setServicio(servicio);
		
		createData(metodo);*/

        if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
        	
        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	}
	
	public static void test5() {

		Pais pais = new Pais();
		pais.setId(3);
		pais.setCodigo("504");
		pais.setNombre("Honduras");
		pais.setEstado(1);
		
		createData(pais);
		
		Agregadores obj = new Agregadores();
		obj.setEstado(1);
		obj.setPais(pais);
		obj.setId(10);
		obj.setNombre_agregador("prueba de insercion hibernate");
		
		createData(obj);

		/*Servicios servicio = new Servicios();
		servicio.setContrasenia("uno");
		servicio.setUsuario("Edwin");
		servicio.setId(10);
		servicio.setAgregador(obj);
		servicio.setWsdl_Agregador("http://192.168.0.100:8090/axis2/services/pruebaWsCadena?wsdl");

		Servicios servicio2 = new Servicios();
		servicio2.setContrasenia("dos");
		servicio2.setId(11);
		servicio2.setUsuario("Edwin");
		servicio2.setAgregador(obj);
		servicio2.setWsdl_Agregador("http://192.168.0.100:8090/axis2/services/pruebaWsCadena?wsdl");
		
		createData(servicio);
		createData(servicio2);
		
		Metodos metodo=new Metodos();
		metodo.setId(7);
		metodo.setNombre("metodoPrueba");
		metodo.setServicio(servicio);
		
		createData(metodo);
		
		Parametros parametros = new Parametros();
		parametros.setId(22);
		//parametros.setColumna("NUMERO");
		//parametros.setInsumo("insumo1");
		parametros.setMetodo(metodo);
		parametros.setNombre("movil");
		parametros.setTipo("java.lang.String");
		
		createData(parametros);*/

        if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
        	
        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	}

	public static void test6() {

		Pais pais = new Pais();
		pais.setId(3);
		pais.setCodigo("504");
		pais.setNombre("Honduras");
		pais.setEstado(1);
		
		createData(pais);
		
		Agregadores obj = new Agregadores();
		obj.setEstado(1);
		obj.setPais(pais);
		obj.setId(10);
		obj.setNombre_agregador("prueba de insercion hibernate");
		
		createData(obj);

		/*Servicios servicio = new Servicios();
		servicio.setContrasenia("uno");
		servicio.setUsuario("Edwin");
		servicio.setId(10);
		servicio.setAgregador(obj);
		servicio.setWsdl_Agregador("http://192.168.0.100:8090/axis2/services/pruebaWsCadena?wsdl");

		Servicios servicio2 = new Servicios();
		servicio2.setContrasenia("dos");
		servicio2.setId(11);
		servicio2.setUsuario("Edwin");
		servicio2.setAgregador(obj);
		servicio2.setWsdl_Agregador("http://192.168.0.100:8090/axis2/services/pruebaWsCadena?wsdl");
		
		createData(servicio);
		createData(servicio2);
		
		Metodos metodo=new Metodos();
		metodo.setId(7);
		metodo.setNombre("metodoPrueba");
		metodo.setServicio(servicio);
		
		createData(metodo);
		
		Parametros parametros = new Parametros();
		parametros.setId(22);
		//parametros.setColumna("NUMERO");
		//parametros.setInsumo("insumo1");
		parametros.setMetodo(metodo);
		parametros.setNombre("movil");
		parametros.setTipo("java.lang.String");
		
		createData(parametros);
		
		Respuesta respuesta = new Respuesta();
		respuesta.setId(1);
		respuesta.setNombre("ni idea");
		//respuesta.setPosicion(2);
		respuesta.setTipo("java.lang.String");
		respuesta.setMetodo(metodo);
		
		createData(respuesta);*/

        if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
        	
        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	}
	
	public static void test7() {

		Pais pais = new Pais();
		pais.setId(2);
		pais.setCodigo("502");
		pais.setNombre("Guatemala");
		pais.setEstado(1);
		
		createData(pais);
		
		Agregadores agregador = new Agregadores();
		agregador.setEstado(1);
		agregador.setPais(pais);
		agregador.setId(4);
		agregador.setNombre_agregador("SMT");
		
		createData(agregador);

		Metodos metodo=new Metodos();
		metodo.setAgregador(agregador);
		metodo.setContrasenia("x9t/yLcnC3VYKCb6v0uezTwYJNk=");
		metodo.setId(10);
		metodo.setInputMessageName("AddBlackListRequest");
		metodo.setInputMessageText("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:loc=\"http://www.csapi.org/schema/parlayx/blackgray/v1_0/local\">   <soapenv:Header>      <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">         <wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">            <wsse:Username>PA00000737</wsse:Username>            <wsse:Password Type=\"...#PasswordDigest\">x9t/yLcnC3VYKCb6v0uezTwYJNk=</wsse:Password>            <wsse:Nonce>096459e93f20a2b39ab6c5ddd493e44f58bc3a91</wsse:Nonce>            <wsse:Created>2012-10-17T11:51:50.263Z</wsse:Created>         </wsse:UsernameToken>      </wsse:Security>      <tns:RequestSOAPHeader xmlns:tns=\"http://www.huawei.com.cn/schema/common/v2_1\">         <tns:AppId>35000001000001</tns:AppId>         <tns:TransId>2014011716010012345</tns:TransId>         <tns:OA>tel:50258260744</tns:OA>         <tns:FA>tel:50258260744</tns:FA>      </tns:RequestSOAPHeader>   </soapenv:Header>   <soapenv:Body>      <loc:deleteBlackList>         <loc:version>1.0</loc:version>         <loc:blackList>         <balckee>               <msisdn>50258260744</msisdn>               <serviceID></serviceID>               <createTime>20111121112200</createTime>            </balckee>         </loc:blackList>         <loc:extensionInfo>            <NamedParameters>               <key>name</key>               <value>value</value>            </NamedParameters>         </loc:extensionInfo>      </loc:deleteBlackList>   </soapenv:Body></soapenv:Envelope>");
		metodo.setNamespaceURI("");
		//metodo.setNombre("");
		metodo.setServiceName("");
		metodo.setSoapActionURI("");
		metodo.setStyle("");
		metodo.setTargetMethodName("");
		metodo.setTargetObjectURI("");
		metodo.setTargetURL("");
		metodo.setUsuario("PA00000737");
		metodo.setWsdl_Agregador("https://hub.americamovil.com/sag/services/blackgrayService?wsdl");
		
		createData(metodo);
		
		Parametros parametros = new Parametros();
		parametros.setId(22);
		//parametros.setColumna("NUMERO");
		//parametros.setInsumo("insumo1");
		parametros.setMetodo(metodo);
		parametros.setNombre("movil");
		//parametros.setTipo("java.lang.String");
		
		createData(parametros);
		
		Respuesta respuesta = new Respuesta();
		respuesta.setId(1);
		respuesta.setNombre("ni idea");
		//respuesta.setPosicion(2);
		//respuesta.setTipo("java.lang.String");
		respuesta.setMetodo(metodo);
		
		createData(respuesta);

        if(SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().isOpen())
        	SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession().close();
        	
        if(!SessionFactoryUtil.getSessionAnnotationFactory().isClosed())
        	SessionFactoryUtil.getSessionAnnotationFactory().close();
	}
	
	/**
	 * Metodo para oobtener una lista de datos
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param query
	 *            {String} dato de insumo para obtener un listado desde la BD
	 * @return {java.util.List}
	 * */
	@SuppressWarnings("rawtypes")
	private static List listData(String query) {
		Session session = SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		List objs = null;
		try {
			session.beginTransaction();
			objs = session.createQuery(query).list();
			session.getTransaction().commit();
			//session.close();
		} catch (RuntimeException e) {
			if (session.getTransaction() != null
					&& session.getTransaction().isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.debug("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}
		}
		return objs;

	}

	private static void deleteData(Object obj) {
		Session session = SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.delete(obj);
			session.getTransaction().commit();
			//session.close();
		} catch (RuntimeException e) {
			if (session.getTransaction() != null
					&& session.getTransaction().isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.debug("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}
		}
	  }

	private static void createData(Object obj) {
		Session session = SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.save(obj);
			session.getTransaction().commit();
			//session.close();
		} catch (RuntimeException e) {
			if (session.getTransaction() != null
					&& session.getTransaction().isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.debug("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}
		}
	}

	private static void updateData(Object obj) {
		Session session =  SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		try {
			session.beginTransaction();
			session.update(obj);
			session.getTransaction().commit();
			//session.close();
		} catch (RuntimeException e) {
			if (session.getTransaction() != null
					&& session.getTransaction().isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.debug("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}
		}
	  }
	
	private static Integer maxValue(Agregadores obj){
		Session session =  SessionFactoryUtil.getSessionAnnotationFactory().getCurrentSession();
		try {
			session.beginTransaction();
			System.out.println("criteria.....");
			Criteria criteria = session.createCriteria(obj.getClass()).setProjection(Projections.max("ID"));
			System.out.println(criteria.uniqueResult());
			Integer dato = (Integer)criteria.uniqueResult();
			session.getTransaction().commit();			
			return dato;
		} catch (RuntimeException e) {
			e.printStackTrace();
			if (session.getTransaction() != null
					&& session.getTransaction().isActive()) {
				try {
					// Second try catch as the rollback could fail as well
					session.getTransaction().rollback();
				} catch (HibernateException e1) {
					logger.debug("Error rolling back transaction");
				}
				// throw again the first exception
				throw e;
			}
			return null;
		}
		
	}

}
