package sv.avantia.depurador.agregadores.entidades;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.FilterDef;

@Entity(name = "SDA_METODOS")
@FilterDef(name="SDA_METODOS_FILTER", defaultCondition="ESTADO = 1")
@Table(name = "SDA_METODOS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Metodos implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@GeneratedValue(strategy=GenerationType.AUTO, generator="Seq_Gen_Metodo")
    @SequenceGenerator(name="Seq_Gen_Metodo", sequenceName="SQ_SDA_METODOS")
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "METODO", nullable = false)
	private Integer metodo;

	@Column(name = "USUARIO")
	private String usuario;

	@Column(name = "CONTRASENIA")
	private String contrasenia;
	
	@Column(name = "END_POINT", nullable = false)
	private String endPoint;
	
	@Column(name = "SEGURIDAD")
	private Integer seguridad;
	
	@Column(name = "INPUTMESSAGETEXT", nullable = false)
	private String inputMessageText;
	
	@Column(name = "INPUTMESSAGENAME")
	private String inputMessageName;
		
	@Column(name = "SERVICE_NAME")
	private String serviceName;

	@Column(name = "SOAPACTIONURI")
	private String soapActionURI;
	
	@Column(name = "NAMESPACEURI")
	private String namespaceURI;

	@Column(name = "STYLE")
	private String style;

	@Column(name = "TARGETMETHODNAME")
	private String targetMethodName;

	@Column(name = "TARGETOBJECTURI")
	private String targetObjectURI;

	@Column(name = "TARGETURL")
	private String targetURL;

	@Column(name = "WSDL_AGREGADOR")
	private String wsdl_Agregador;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ID_AGREGADOR")
	private Agregadores agregador;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<Parametros> parametros;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<Respuesta> respuestas;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<LogDepuracion> depuraciones;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Agregadores getAgregador() {
		return agregador;
	}

	public void setAgregador(Agregadores agregador) {
		this.agregador = agregador;
	}

	public String getInputMessageName() {
		return inputMessageName;
	}

	public void setInputMessageName(String inputMessageName) {
		this.inputMessageName = inputMessageName;
	}

	public String getInputMessageText() {
		return inputMessageText;
	}

	public void setInputMessageText(String inputMessageText) {
		this.inputMessageText = inputMessageText;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	public String getSoapActionURI() {
		return this.soapActionURI;
	}

	public void setSoapActionURI(String soapActionURI) {
		this.soapActionURI = soapActionURI;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getTargetMethodName() {
		return targetMethodName;
	}

	public void setTargetMethodName(String targetMethodName) {
		this.targetMethodName = targetMethodName;
	}

	public String getTargetObjectURI() {
		return targetObjectURI;
	}

	public void setTargetObjectURI(String targetObjectURI) {
		this.targetObjectURI = targetObjectURI;
	}

	public String getTargetURL() {
		return targetURL;
	}

	public void setTargetURL(String targetURL) {
		this.targetURL = targetURL;
	}

	public String getWsdl_Agregador() {
		return wsdl_Agregador;
	}

	public void setWsdl_Agregador(String wsdl_Agregador) {
		this.wsdl_Agregador = wsdl_Agregador;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getContrasenia() {
		return contrasenia;
	}

	public void setContrasenia(String contrasenia) {
		this.contrasenia = contrasenia;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Set<Parametros> getParametros() {
		if(parametros == null)
			parametros = new HashSet<Parametros>();
		return parametros;
	}

	public void setParametros(Set<Parametros> parametros) {
		this.parametros = parametros;
	}

	public Set<Respuesta> getRespuestas() {
		return respuestas;
	}

	public void setRespuestas(Set<Respuesta> respuestas) {
		this.respuestas = respuestas;
	}

	public Set<LogDepuracion> getDepuraciones() {
		return depuraciones;
	}

	public void setDepuraciones(Set<LogDepuracion> depuraciones) {
		this.depuraciones = depuraciones;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}
	
	public Integer getSeguridad() {
		return seguridad;
	}

	public void setSeguridad(Integer seguridad) {
		this.seguridad = seguridad;
	}
	

	/**
	 * @return the metodo
	 */
	public Integer getMetodo() {
		return metodo;
	}

	/**
	 * @param metodo the metodo to set
	 */
	public void setMetodo(Integer metodo) {
		this.metodo = metodo;
	}
	
	@Override
	public String toString() {
		return "Metodos [id=" + id + ", metodo=" + metodo + "]";
	}
}