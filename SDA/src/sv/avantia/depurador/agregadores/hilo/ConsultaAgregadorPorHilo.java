package sv.avantia.depurador.agregadores.hilo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cladonia.xml.webservice.wsdl.OperationInfo;
import com.cladonia.xml.webservice.wsdl.ServiceInfo;
import com.cladonia.xml.webservice.wsdl.WSDLException;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.entidades.Parametros;
import sv.avantia.depurador.agregadores.ws.cliente.Cliente;

public class ConsultaAgregadorPorHilo extends Thread {

	private List<String> moviles = new ArrayList<String>();
	private Agregadores agregador;

	public void run() {
		// consultar un agregador WS
		try {
			procesarServiciosWeb();
		} catch (Exception e) {
			System.out.println("Hubo Error dentro del hilo");
			this.interrupt();
		}
	}
	
	@SuppressWarnings("static-access")
	private void procesarServiciosWeb() throws WSDLException{
		Cliente cliente = new Cliente();
		if (getAgregador() != null) {
			System.out.println("procesar agregador...");
			if (getAgregador().getMetodos() != null) {
				System.out.println("procesar metodos...");
				for (Metodos metodo : getAgregador().getMetodos()) {
					if (metodo.getParametros() != null) {
						
						System.out.println("procesando ");
						for (String movil : moviles) {	
							for (Parametros parametro : metodo.getParametros()) 
							{
								System.out.println("Remplazando " + parametro.getNombre());
								metodo.setInputMessageText(metodo.getInputMessageText().replaceAll(parametro.getNombre(), movil));									
							}
							System.out.println("getInputMessageText: "	+ metodo.getInputMessageText());
							System.out.println("SOAP response: \n"+cliente.invokeOperation(metodo));	
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @throws WSDLException 
	 * */
	@SuppressWarnings("unused")
	private void obtenerServicios(String wsdl) throws WSDLException{
		Cliente wsdlparser = new Cliente();
		// http://www.xignite.com/xquotes.asmx?WSDL
		List<?> services = null;//wsdlparser.getServicesInfo(wsdl, null);

		// process objects built from the binding information
		@SuppressWarnings("null")
		Iterator<?> servicesIter = services.iterator();
		while (servicesIter.hasNext()) {
			ServiceInfo service = (ServiceInfo) servicesIter.next();
			System.out.println("Service Name: " + service.getName());
			System.out.println();

			Iterator<?> operationsIter = service.getOperations();
			while (operationsIter.hasNext()) {
				OperationInfo operation = (OperationInfo) operationsIter.next();
				
				System.out.println("Invoking the following:");
				System.out.println("Service Name: "+service.getName());
				System.out.println("Operation Name: "+operation.toString());
				System.out.println("Target URL: "+operation.getTargetURL());
				System.out.println("SOAPAction: "+operation.getSoapActionURI());
				System.out.println("SOAP request: \n"+operation.getInputMessageText());
				System.out.println();
				//System.out.println("SOAP response: \n"+wsdlparser.invokeOperation(operation));
				
				/*System.out.println("Operation Name: " 		+ operation.toString());
				System.out.println("getInputMessageName: "	+ operation.getInputMessageName());
				System.out.println("getInputMessageText: "	+ operation.getInputMessageText());
				System.out.println("getNamespaceURI: "		+ operation.getNamespaceURI());
				System.out.println("getSoapActionURI: "		+ operation.getSoapActionURI());
				System.out.println("getStyle: " 			+ operation.getStyle());
				System.out.println("getTargetMethodName: "	+ operation.getTargetMethodName());
				System.out.println("getTargetObjectURI(): "	+ operation.getTargetObjectURI());
				System.out.println("getTargetURL(): "		+ operation.getTargetURL());*/
				System.out.println();

			}
		}
	}
	
	public List<String> getMoviles() {
		return moviles;
	}

	public void setMoviles(List<String> moviles) {
		this.moviles = moviles;
	}

	public Agregadores getAgregador() {
		return agregador;
	}

	public void setAgregador(Agregadores agregador) {
		this.agregador = agregador;
	}
	
	/*private void procesarResultados(){
		try {
			if (getInsumos() != null && 0 < getInsumos().length && getInsumos()[0] != null) {
				ResultSet resultSet0 = getInsumos()[0];
				ResultSet resultSet1 = getInsumos()[1];
				ResultSet resultSet2 = getInsumos()[2];
				resultSet0.first();
				while(resultSet0.next()){
					if (getInsumos() != null && 1 < getInsumos().length && getInsumos()[1] != null) {
						resultSet1.first();
						while(resultSet1.next()){
							if (getInsumos() != null && 2 < getInsumos().length && getInsumos()[2] != null) {
								resultSet2.first();
								while(resultSet2.next()){
									if (getInsumos() != null && 3 < getInsumos().length && getInsumos()[3] != null) {
										while(getInsumos()[3].next()){
											if (getInsumos() != null && 4 < getInsumos().length && getInsumos()[4] != null) {
												while(getInsumos()[4].next()){
													if (getInsumos() != null && 5 < getInsumos().length && getInsumos()[5] != null) {
														while(getInsumos()[5].next()){
															
														}
													}
												}
											}
										}
									}else{
										try {
											if (getParametrizacion() != null) {				
												if(getParametrizacion().getServicios()!=null){
													for (Servicios servicio : getParametrizacion().getServicios()) {
														if(servicio.getMetodos()!=null){
															for (Metodos metodo : servicio.getMetodos()) {
																if(metodo.getParametros()!=null){
																	for (Parametros parametro : metodo.getParametros()) {
																		
																		
																		if (parametro.getInsumo().equals(resultSet0.getString("INSUMO"))) {
																			metodo.getOperacionSRV().setInputMessageText(
																					metodo.getOperacionSRV().getInputMessageText().replace(("_*"+parametro.getNombre()+"_*"), "" + Class.forName(parametro.getTipo()).getConstructor(String.class).newInstance(resultSet0.getString(parametro.getColumna().toUpperCase())))
																					);
																		}else if (parametro.getInsumo().equals(resultSet1.getString("INSUMO"))) {
																			metodo.getOperacionSRV().setInputMessageText(
																					metodo.getOperacionSRV().getInputMessageText().replace(("_*"+parametro.getNombre()+"_*"), "" + Class.forName(parametro.getTipo()).getConstructor(String.class).newInstance(resultSet1.getString(parametro.getColumna().toUpperCase())))
																					);
																		}else if (parametro.getInsumo().equals(resultSet2.getString("INSUMO"))) {
																			metodo.getOperacionSRV().setInputMessageText(
																					metodo.getOperacionSRV().getInputMessageText().replace(("_*"+parametro.getNombre()+"_*"), "" + Class.forName(parametro.getTipo()).getConstructor(String.class).newInstance(resultSet2.getString(parametro.getColumna().toUpperCase())))
																					);
																		}	
																	}
																}
																obtenerRespuesta(Cliente.invokeOperation(metodo.getOperacionSRV()));
																System.out.println();
																	}
																	
																}
															}
														}
											}
										} catch (ClassNotFoundException e) {

										} catch (IllegalArgumentException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (SecurityException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (InstantiationException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (IllegalAccessException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (InvocationTargetException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (NoSuchMethodException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							}
						}
						
					}else{
						System.out.println("no hay datos que procesar");
					}
				}
				resultSet0.close();
				resultSet0 = null;
				resultSet1.close();
				resultSet1 = null;
				resultSet2.close();
				resultSet2 = null;
			} else {
				System.out.println("Hasta aqui llegue porque no hay data que procesar");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	

}