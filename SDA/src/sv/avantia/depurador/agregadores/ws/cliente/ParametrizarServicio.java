package sv.avantia.depurador.agregadores.ws.cliente;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Particle;
import org.exolab.castor.xml.schema.Schema;

import org.exolab.castor.xml.schema.Structure;
import org.exolab.castor.xml.schema.XMLType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sv.avantia.depurador.agregadores.entidades.Agregadores;
import sv.avantia.depurador.agregadores.entidades.Metodos;
import sv.avantia.depurador.agregadores.entidades.Parametros;

import com.cladonia.xml.webservice.wsdl.WSDLException;
import com.cladonia.xml.webservice.wsdl.XMLSupport;
import com.ibm.wsdl.extensions.schema.SchemaImpl;

public class ParametrizarServicio {
	
	// holds the SOAP Body element for each message
	private Element header = null;

	// holds the SOAP Body element for each message
	private Element body = null;

	// WSDL4J Factory instance
	private WSDLFactory wsdlFactory = null;

	// castor schema types
	private Schema wsdlTypes = null;

	// dom to hold each soap message as it gets built
	private Document document = null;

	// schema target namespace
	private String schemaTargetNamespace = null;
	
	public ParametrizarServicio() throws WSDLException
	{
		try{
			wsdlFactory = WSDLFactory.newInstance();
		}
		catch (javax.wsdl.WSDLException e)
		{
			throw new WSDLException(e.toString());
		}
	}
	
	/**
	 * Builds a List of ServiceInfo components for each Service defined in a
	 * WSDL Document
	 * 
	 * @param wsdlBaseURI
	 *            A base URI that points to a WSDL file
	 * @param dcoument
	 *            the w3c dom document.
	 * 
	 * @return A List of ServiceInfo objects populated for each service defined
	 *         in the WSDL file.
	 */
	public Agregadores getServicesInfo(String wsdlBaseURI, String document, Agregadores agregador)
			throws WSDLException 
	{
		try 
		{
			// create the WSDL Reader object
			WSDLReader reader = wsdlFactory.newWSDLReader();

			// read the WSDL and get the top-level Definition object
			Definition def = reader.readWSDL(wsdlBaseURI, document);

			// create a castor schema from the types element defined in WSDL this method will return null if there are types defined in the WSDL
			wsdlTypes = createSchemaFromTypes(def);

			// get the services defined in the document
			Map<?, ?> services = def.getServices();

			if (services != null) 
			{
				@SuppressWarnings("rawtypes")
				Iterator svcIter = services.values().iterator();
				while (svcIter.hasNext()) 
				{
					// add the new component to the List to be returned populate the new component from the WSDL Definition read
					agregador = populateInfo((Service) svcIter.next(), agregador);					
				}
			}

			// return the List of services we created
			return agregador;

		} 
		catch (WSDLException e) 
		{
			final String errMsg = "The following error occurred obtaining the service "	+ "information from the WSDL: " + e.getMessage();
			throw new WSDLException(errMsg, e);
		} 
		catch (Exception e) 
		{
			final String errMsg = "The following error occurred obtaining the service "	+ "information from the WSDL: " + e.getMessage();
			throw new WSDLException(errMsg, e);
		}
	}

	public Agregadores getServicesInfoFromFile(String urlWSDLFile, Agregadores agregador){
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            File file = new File(urlWSDLFile);
            Document doc = docBuilder.parse(file);
            
            return getServicesInfo(doc, agregador);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return agregador;
	}
		
	/**
	 * Builds a List of ServiceInfo components for each Service defined in a
	 * WSDL Document
	 * 
	 * @param wsdlURI
	 *            A URI that points to a WSDL file
	 * 
	 * @return A List of ServiceInfo objects populated for each service defined
	 *         in the WSDL file.
	 */
	public Agregadores getServicesInfo(Document wsdl, Agregadores agregador) throws WSDLException 
	{
		try 
		{
			// create the WSDL Reader object
			WSDLReader reader = wsdlFactory.newWSDLReader();

			//reader.setFeature("javax.wsdl.verbose", false);
			//reader.setFeature("javax.wsdl.importDocuments", true);
            
			// read the WSDL and get the top-level Definition object
			Definition def = reader.readWSDL(null, wsdl);

			// create a castor schema from the types element defined in WSDL this method will return null if there are types defined in the WSDL
			wsdlTypes = createSchemaFromTypes(def);

			// get the services defined in the document
			@SuppressWarnings("rawtypes")
			Map services = def.getServices();

			if (services != null) 
			{
				// create a ServiceInfo for each service defined
				@SuppressWarnings("rawtypes")
				Iterator svcIter = services.values().iterator();
				while (svcIter.hasNext()) 
				{
					// add the new component to the List to be returned populate the new component from the WSDL Definition read
					agregador = populateInfo((Service) svcIter.next(), agregador);					
				}
				
			}

			//clean of memory JVM object instanced
			reader=null;
			def = null;
			services = null;
			
			// return the List of services we created
			return agregador;

		} 
		catch (WSDLException e) 
		{
			// should really log this here
			final String errMsg = "The following error occurred obtaining the service information from the WSDL: " + e.getMessage();
			System.out.println(errMsg);
			throw e;
		} 
		catch (Exception e) 
		{
			final String errMsg = "The following error occurred obtaining the service information from the WSDL: " + e.getMessage();
			System.out.println(errMsg);
			throw new WSDLException(errMsg, e);
		}
	}
	
	/**
	 * Builds a List of ServiceInfo components for each Service defined in a
	 * WSDL Document
	 * 
	 * @param wsdlURI
	 *            A URI that points to a WSDL file
	 * 
	 * @return A List of ServiceInfo objects populated for each service defined
	 *         in the WSDL file.
	 */
	public Agregadores getServicesInfo(String wsdlURI, Agregadores agregador) throws WSDLException 
	{
		try 
		{
			// create the WSDL Reader object
			WSDLReader reader = wsdlFactory.newWSDLReader();

			reader.setFeature("javax.wsdl.verbose", false);
			reader.setFeature("javax.wsdl.importDocuments", true);
            
			// read the WSDL and get the top-level Definition object
			Definition def = reader.readWSDL(wsdlURI);

			// create a castor schema from the types element defined in WSDL
			// this method will return null if there are types defined in the WSDL
			wsdlTypes = createSchemaFromTypes(def);

			// get the services defined in the document
			@SuppressWarnings("rawtypes")
			Map services = def.getServices();

			if (services != null) 
			{
				// create a ServiceInfo for each service defined
				@SuppressWarnings("rawtypes")
				Iterator svcIter = services.values().iterator();
				while (svcIter.hasNext()) 
				{
					// add the new component to the List to be returned
					// populate the new component from the WSDL Definition read
					agregador = populateInfo((Service) svcIter.next(), agregador);					
				}
				
			}

			//clean of memory JVM object instanced
			reader=null;
			def = null;
			services = null;
			
			// return the List of services we created
			return agregador;

		} 
		catch (WSDLException e) 
		{
			// should really log this here
			final String errMsg = "The following error occurred obtaining the service information from the WSDL: " + e.getMessage();
			System.out.println(errMsg);
			throw e;
		} 
		catch (Exception e) 
		{
			final String errMsg = "The following error occurred obtaining the service information from the WSDL: " + e.getMessage();
			System.out.println(errMsg);
			throw new WSDLException(errMsg, e);
		}
	}
	
	/**
	 * Creates a castor schema based on the types defined by a WSDL document
	 * 
	 * @param wsdlDefinition
	 *            The WSDL4J instance of a WSDL definition.
	 * 
	 * @return A castor schema is returned if the WSDL definition contains a
	 *         types element.
	 */
	private Schema createSchemaFromTypes(Definition wsdlDefinition) 
	{
		// get the schema element from the WSDL definition
		Element schemaElement = null;

		if (wsdlDefinition.getTypes() != null) 
		{
			ExtensibilityElement schemaExtElem = findExtensibilityElement(wsdlDefinition.getTypes().getExtensibilityElements(),"schema");

			if (schemaExtElem != null && schemaExtElem instanceof UnknownExtensibilityElement)
				schemaElement = ((UnknownExtensibilityElement) schemaExtElem).getElement();
			
			if (schemaExtElem != null && schemaExtElem instanceof SchemaImpl)
				schemaElement = ((SchemaImpl) schemaExtElem).getElement();
		}
		
		// no schema to read
		if (schemaElement == null){
			return null;
		}
		
		Map<?, ?> namespaces = wsdlDefinition.getNamespaces();
		if (namespaces != null && !namespaces.isEmpty()) 
		{
			Iterator<?> nsIter = namespaces.keySet().iterator();
			while (nsIter.hasNext()) 
			{
				String nsPrefix = (String) nsIter.next();
				String nsURI = (String) namespaces.get(nsPrefix);

				if (nsPrefix != null && nsPrefix.length() > 0) 
				{
					// add the namespaces from the definition element to teh schema element
					schemaElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + nsPrefix, nsURI);
				}
			}
		}

		// convert it into a Castor schema instance
		Schema schema = null;

		try 
		{
			System.out.println(schemaElement.toString());
			schema = XMLSupport.convertElementToSchema((Element)schemaElement);
			if(schema!=null){
				schemaTargetNamespace = schema.getTargetNamespace();
			}else{
				System.out.println("El Schema nos regreso nulo");
			}
			
		}
		catch (Exception e) 
		{
			System.out.println("The following error occurred obtaining the schema from WSDL: " + e.getMessage());
			e.printStackTrace();
		}
		
		return schema;
	}

	/**
	 * Populates a ServiceInfo instance from the specified Service definiition
	 * 
	 * @param component
	 *            The ServiceInfo component to populate
	 * @param service
	 *            The Service to populate from
	 * 
	 * @return The populated ServiceInfo is returned representing the Service
	 *         parameter
	 * @throws com.cladonia.xml.webservice.wsdl.WSDLException
	 */
	private Agregadores populateInfo(Service service, Agregadores agregador) throws WSDLException 
	{
		try 
		{
			// get the qualified service name information
			QName qName = service.getQName();

			// get the service's namespace URI
			String namespace = qName.getNamespaceURI();

			// get the defined ports for this service
			Map<?, ?> ports = service.getPorts();

			// use the Ports to create OperationInfos for all request/response
			// messages defined
			Iterator<?> portIter = ports.values().iterator();

			
			while (portIter.hasNext()) 
			{
				// get the next defined port
				Port port = (Port) portIter.next();

				// now we will create operations from the Binding information
				agregador = buildOperations(port, namespace, agregador);
			}

			
			
			return agregador;
		} 
		catch (WSDLException e) 
		{
			throw e;
		} 
		catch (Exception e) 
		{
			// should log this here
			throw new com.cladonia.xml.webservice.wsdl.WSDLException(e);
		}
	}

	/**
	 * Creates Info objects for each Binding Operation defined in a Port Binding
	 * 
	 * @param binding
	 *            The Binding that defines Binding Operations used to build info
	 *            objects from
	 * @param namespace
	 *            The namespace obtained from the service part
	 * 
	 * @return A List of built and populated OperationInfos is returned for each
	 *         Binding Operation
	 */
	private Agregadores buildOperations(Port port, String namespace, Agregadores agregador) throws WSDLException 
	{
		try 
		{
			// get the Port's Binding
			Binding binding = port.getBinding();
			
			// create the array of info objects to be add to Agregadores
			List<Metodos> metodos = new ArrayList<Metodos>();
			//agregador.setMetodos(new HashSet<Metodos>(metodos));
			
			// get the list of Binding Operations from the passed binding
			List<?> operations = binding.getBindingOperations();

			if (operations != null && !operations.isEmpty()) 
			{
				// determine encoding (rpc or document)
				ExtensibilityElement soapBindingElem = findExtensibilityElement(binding.getExtensibilityElements(), "binding");

				// set "document" as the default
				String style = "document";

				if (soapBindingElem != null	&& soapBindingElem instanceof SOAPBinding) 
				{
					SOAPBinding soapBinding = (SOAPBinding) soapBindingElem;
					style = soapBinding.getStyle();
				}

				// for each binding operation, create a new OperationInfo
				Iterator<?> opIter = operations.iterator();
				int id = 8;
				while (opIter.hasNext()) 
				{
					// for each operation we need a new clean dom
					createEmptySoapMessage();

					BindingOperation oper = (BindingOperation) opIter.next();

					// only required to support soap:operation bindings
					ExtensibilityElement operElem = findExtensibilityElement(oper.getExtensibilityElements(), "operation");

					if (operElem != null && operElem instanceof SOAPOperation) 
					{
						// create a new operation info
						//OperationInfo operationInfo = new OperationInfo(style);
						Metodos operationInfo = new Metodos();
						operationInfo.setStyle(style);
						operationInfo.setId(id);

						// style maybe overridden in operation
						String operStyle = ((SOAPOperation) operElem).getStyle();
						if ((operStyle != null) && (!operStyle.equals(""))) 
						{
							operationInfo.setStyle(operStyle);
						}

						// set the namespace URI for the operation.
						operationInfo.setNamespaceURI(namespace);

						// populate it from the Binding Operation
						buildOperation(operationInfo, oper);

						// find the SOAP target URL
						ExtensibilityElement addrElem = findExtensibilityElement(port.getExtensibilityElements(), "address");

						if (addrElem != null && addrElem instanceof SOAPAddress) 
						{
							// set the SOAP target URL
							SOAPAddress soapAddr = (SOAPAddress) addrElem;
							operationInfo.setTargetURL(soapAddr.getLocationURI());
						}
						
						// add to the return list
						//component.addOperation(operationInfo);
						metodos.add(operationInfo);
					}
				}
			}

			// llenamos la lista de metodos que tienen este agregador
			agregador.getMetodos().addAll(metodos);
			
			return agregador;

		} 
		catch (WSDLException e) 
		{
			throw e;
		} 
		catch (Exception e) 
		{
			// should log this here
			throw new WSDLException(e);
		}
	}
		
	/**
	 * Crear un mensaje SOAP vacio.
	 * 
	 * @author Edwin Mejia - Avantia Consultores return void
	 */
	private void createEmptySoapMessage() throws Exception 
	{
		// create the dom
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		document = db.newDocument();

		// create the SOAP Envelope element
		Element envelope = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/",
				"SOAP-ENV:Envelope");

		// add the SOAP Namespace (1.1 version)
		envelope.setAttributeNS("http://www.w3.org/2000/xmlns/",
				"xmlns:SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");

		// add the soap encoding namespace
		envelope.setAttributeNS("http://www.w3.org/2000/xmlns/",
				"xmlns:SOAP-ENC", "http://schemas.xmlsoap.org/soap/encoding/");

		// add the schema instance namespace
		envelope.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");

		// add the schema namespace
		envelope.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsd",
				"http://www.w3.org/2001/XMLSchema");

		// create the SOAP header (security headers etc)
		header = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV:Header");
		
		/* No eliminar sera implementado a  futuro actualmente no manejara encabezado dinamico
		 * Element node =  DocumentBuilderFactory
			    .newInstance()
			    .newDocumentBuilder()
			    .parse(new ByteArrayInputStream("<node>value</node>".getBytes()))
			    .getDocumentElement();
		
		node = (Element) document.importNode(node, true);
		header.appendChild(node);*/
		
		Element security = document.createElement( "wsse:Security" );
		security.setAttribute("xmlns:wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		header.appendChild(security);
		
		Element usernameToken = document.createElement("wsse:UsernameToken");
        usernameToken.setAttribute("xmlns:wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        security.appendChild(usernameToken);
        
        Element userName = document.createElement("wsse:Username");
        userName.setTextContent("NKK");
        usernameToken.appendChild(userName);
        
        Element userPass = document.createElement("wsse:Password");
        userPass.setAttribute("Type", "...#PasswordDigest");
        userPass.setTextContent("jdnsdkjfh78sdfys87d");
        usernameToken.appendChild(userPass);
		
        Element nonce = document.createElement("wsse:Nonce");
        nonce.setTextContent("WScqanjCEAC4mQoBE07sAQ====");
        usernameToken.appendChild(nonce);
        
        Element created = document.createElement("wsu:Created");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss'Z'");
        created.setTextContent(df.format(Calendar.getInstance().getTime()));
        usernameToken.appendChild(created);
        
		// create the SOAP Body (store in a memeber variale so we can easly access later)
		body = document.createElementNS(
				"http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV:Body");
		
		// add the body to the envelope
		envelope.appendChild(header);
		
		// add the body to the envelope
		envelope.appendChild(body);

		// add the envelope to the document
		document.appendChild(envelope);
	}
	
	/**
	 * Populates an OperationInfo from the specified Binding Operation
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * @param operationInfo
	 *            The component Metodos
	 * @param bindingOper
	 *            A Binding Operation to define the OperationInfo from
	 * 
	 * @return The populated OperationInfo object is returned.
	 */
	private Metodos buildOperation(Metodos operationInfo, BindingOperation bindingOper) throws WSDLException 
	{
		try 
		{
			// get the operation
			Operation oper = bindingOper.getOperation();

			// set the name using the operation name
			operationInfo.setTargetMethodName(oper.getName());

			// set the action URI
			ExtensibilityElement operElem = findExtensibilityElement(
					bindingOper.getExtensibilityElements(), "operation");

			if (operElem != null && operElem instanceof SOAPOperation) 
			{
				SOAPOperation soapOperation = (SOAPOperation) operElem;
				operationInfo.setSoapActionURI(soapOperation.getSoapActionURI());
			}

			// get the Binding Input
			BindingInput bindingInput = bindingOper.getBindingInput();

			// get the Binding Output
			@SuppressWarnings("unused")
			BindingOutput bindingOutput = bindingOper.getBindingOutput();
			
			// get the SOAP Body part (of the operation inside the binding)
			ExtensibilityElement bodyElem = findExtensibilityElement(bindingInput.getExtensibilityElements(), "body");

			if (bodyElem != null && bodyElem instanceof SOAPBody) 
			{
				SOAPBody soapBody = (SOAPBody) bodyElem;

				// the SOAP Body contains the target object's namespace URI (may or may not be present)
				operationInfo.setTargetObjectURI(soapBody.getNamespaceURI());
			}

			// get the Operation's Input definition
			Input inDef = oper.getInput();

			if (inDef != null) 
			{
				// build input parameters
				Message inMsg = inDef.getMessage();

				if (inMsg != null) 
				{
					// set the name of the operation's input message (good to know for debugging)
					operationInfo.setInputMessageName(inMsg.getQName().getLocalPart());

					// set the body of the operation's input message
					operationInfo = buildMessageText(operationInfo, inMsg);
				}
			}

			// finished, return the populated object
			return operationInfo;

		} 
		catch (WSDLException e) 
		{
			// should log\trace this here
			throw e;
		} 
		catch (Exception e) 
		{
			// should log\trace this here
			throw new WSDLException(e);
		}
	}
		
	/**
	 * Returns the desired ExtensibilityElement if found in the List
	 * 
	 * @param extensibilityElements
	 *            The list of extensibility elements to search
	 * @param elementType
	 *            The element type to find
	 * 
	 * @return Returns the first matching element of type found in the list
	 */
	private static ExtensibilityElement findExtensibilityElement(List<?> extensibilityElements, String elementType) 
	{
		if (extensibilityElements != null) 
		{
			Iterator<?> iter = extensibilityElements.iterator();
			while (iter.hasNext()) 
			{
				ExtensibilityElement element = (ExtensibilityElement) iter.next();
				if (element.getElementType().getLocalPart().equalsIgnoreCase(elementType)) 
				{
					return element;
				}
			}
		}
		return null;
	}

	/**
	 * Builds the SOAP Body content given a SOAP Message definition (from WSDL)
	 * 
	 * @param operationInfo
	 *            The component to build message text for
	 * @param msg
	 *            The SOAP Message definition that has parts to defined
	 *            parameters for
	 * 
	 * @return The SOAP Envelope as a String
	 */
	private Metodos buildMessageText(Metodos operationInfo, Message msg) throws WSDLException 
	{
		try 
		{
			//List to insert data in operationInfo
			List<Parametros> parametros = new ArrayList<Parametros>();
			
			// the root element to add all the message content
			Element rootElem = null;
			String operationStyle = operationInfo.getStyle();

			if (operationStyle.equalsIgnoreCase("rpc")) 
			{
				// if "rpc" style then add wrapper element with the name of the operation
				if ((operationInfo.getTargetObjectURI() != null) && (!operationInfo.getTargetObjectURI().equals(""))) 
				{
					// create the element with the object namespace
					rootElem = document.createElementNS(operationInfo.getTargetObjectURI(), "xngr:"	+ operationInfo.getTargetMethodName());
					rootElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xngr", operationInfo.getTargetObjectURI());				
				} 
				else 
				{
					// create the element with no namespace
					rootElem = document.createElementNS(null, operationInfo.getTargetMethodName());
				}
			} else {
				// else for "document" style set the root to be the SOAP Body
				rootElem = body;
			}

			// get the message parts
			List<?> msgParts = msg.getOrderedParts(null);

			// process each part
			Iterator<?> iter = msgParts.iterator();

			while (iter.hasNext()) 
			{
				// get each part
				Part part = (Part) iter.next();

				// add content for each message part
				String partName = part.getName();

				if (partName != null) 
				{
					// is it an element or a type ?
					if (part.getElementName() != null) 
					{
						// determine if the element is complex or simple
						XMLType xmlType = getXMLType(part);
						
						if (xmlType != null && xmlType.isComplexType()) 
						{
							// build the element that will be added to the message
							Element partElem = document.createElementNS(null, part.getElementName().getLocalPart());

							// build the complex message structure
							operationInfo = buildComplexPart((ComplexType) xmlType, partElem, operationInfo);

							// add this message part
							rootElem.appendChild(partElem);
						} 
						else if (xmlType != null && xmlType.isSimpleType()) 
						{
							Parametros parametro = new Parametros();
							parametro.setNombre(partName);
							parametro.setTipo(xmlType.getName());
							parametros.add(parametro);
							
							// build the simple element that will be added to the message
							Element partElem = document.createElementNS(null, partName);
							partElem.appendChild(document.createTextNode("_*".concat(partName).concat("_*")));

							// add this message part
							rootElem.appendChild(partElem);

						}
					} 
					else 
					{
						// of type "type"
						XMLType xmlType = getXMLType(part);
						
						// is it comlex or simple type
						if (xmlType != null && xmlType.isComplexType()) 
						{
							if (operationStyle.equalsIgnoreCase("rpc")) 
							{
								// create an element with the part name (only required for RPC)
								Element partElem = document.createElementNS(null, partName);

								// build the complex message structure
								operationInfo = buildComplexPart((ComplexType) xmlType, partElem, operationInfo);

								// add this message part
								rootElem.appendChild(partElem);
							} 
							else 
							{
								// build the complex message structure
								operationInfo = buildComplexPart((ComplexType) xmlType, rootElem, operationInfo);
							}
						} 
						else if (xmlType != null && xmlType.isSimpleType()) 
						{
							Parametros parametro = new Parametros();
							parametro.setNombre(partName);
							parametro.setTipo(xmlType.getName());
							parametros.add(parametro);
							// build the simple element that will be added to the message
							Element partElem = document.createElementNS(null,partName);
							partElem.appendChild(document.createTextNode("_*".concat(partName).concat("_*")));

							// add this message part
							rootElem.appendChild(partElem);
						}
					}
				}
			}

			// append the content to the SOAP Body element
			if (operationStyle.equalsIgnoreCase("rpc"))	
			{
				body.appendChild(rootElem);
			}

			// add the schema targetnamespace if "document" style to the SOAP body
			if (operationStyle.equalsIgnoreCase("document")) 
			{
				if (schemaTargetNamespace != null) 
				{
					// add the schema targetnameapace to the soap body
					body.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", schemaTargetNamespace);
				} 
				else if ((operationInfo.getNamespaceURI() != null) && (!operationInfo.getNamespaceURI().equals(""))) 
				{
					// if the schema targetnamespace isn't present then add the service namespace
					body.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", operationInfo.getNamespaceURI());
				} 
				else 
				{
					// no namespaces to add
				}
			}
			
			if(operationInfo.getParametros().size()<=0)
				operationInfo.setParametros(new HashSet<Parametros>(parametros));
			
			// return the serialised dom
			operationInfo.setInputMessageText(XMLSupport.prettySerialise(document));
			
			return operationInfo;
		} 
		catch (WSDLException e) 
		{
			// should log\trace this here
			throw e;
		} 
		catch (Exception e) 
		{
			// should log\trace this here
			throw new WSDLException(e);
		}
	}

	/**
	 * Gets an XML Type from a SOAP Message Part read from WSDL
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * 
	 * @param part
	 *            The SOAP Message part
	 * 
	 * @return The corresponding XML Type is returned.
	 */
	protected XMLType getXMLType(Part part) 
	{
		
		// no defined types, Nothing to do
		if (wsdlTypes == null)
		{
			return null;
		}
		
		// find the XML type
		XMLType xmlType = null;

		// first see if there is a defined element
		if (part.getElementName() != null) 
		{
			// get the element name
			String elemName = part.getElementName().getLocalPart();

			// find the element declaration
			ElementDecl elemDecl = wsdlTypes.getElementDecl(elemName);

			if (elemDecl != null) 
			{
				// from the element declaration get the XML type
				xmlType = elemDecl.getType();
			}
		} 
		else if (part.getTypeName() != null) 
		{
			// get the type name
			String typeName = part.getTypeName().getLocalPart();

			// get the XML type
			xmlType = wsdlTypes.getType(typeName);
		}
		
		return xmlType;
	}
	
	/**
	 * Populate an element using the complex XML type passed in
	 * 
	 * @author Edwin Mejia - Avantia Consultores
	 * 
	 * @param complexType
	 *            The complex XML type to build the element for
	 * @param partElem
	 *            The element to build content for
	 */
	protected Metodos buildComplexPart(ComplexType complexType, Element partElem, Metodos operationInfo) 
	{
		try {
			List<Parametros> parametros = new ArrayList<Parametros>();
			XMLType baseType = complexType.getBaseType();
			if (baseType != null && baseType.isComplexType()) 
			{
				operationInfo = buildComplexPart((ComplexType) baseType, partElem, operationInfo);
			}

			// find the group
			Enumeration<?> particleEnum = complexType.enumerate();
			Group group = null;

			while (particleEnum.hasMoreElements()) 
			{
				Particle particle = (Particle) particleEnum.nextElement();

				if (particle instanceof Group) 
				{
					group = (Group) particle;
					break;
				}
			}

			if (group != null) 
			{
				Enumeration<?> groupEnum = group.enumerate();

				while (groupEnum.hasMoreElements()) 
				{
					Structure item = (Structure) groupEnum.nextElement();

					if (item.getStructureType() == Structure.ELEMENT) 
					{
						ElementDecl elementDecl = (ElementDecl) item;

						// build the element that will be added to the message
						Element childElem = document.createElementNS(null,elementDecl.getName());

						XMLType xmlType = elementDecl.getType();

						if (xmlType != null && xmlType.isComplexType()) 
						{
							// recurse
							operationInfo = buildComplexPart((ComplexType) xmlType, childElem, operationInfo);
						} 
						else if (xmlType != null && xmlType.isSimpleType()) 
						{
							Parametros parametro = new Parametros();
							parametro.setNombre(elementDecl.getName());
							parametro.setTipo(xmlType.getName());
							parametros.add(parametro);
							// add some default content as just a place holder
							childElem.appendChild(document.createTextNode("_*".concat(elementDecl.getName()).concat("_*")));
							
						}
						
						partElem.appendChild(childElem);
						
					}
				}
			}
			
			operationInfo.setParametros(new HashSet<Parametros>(parametros));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		return operationInfo;
	}
}
