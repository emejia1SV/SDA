package sv.avantia.depurador.agregadores.entidades;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "AGR_METODOS")
@Table(name = "AGR_METODOS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Metodos {

	@Id
	@Column(name = "ID", nullable = false)
	private int id;

	@ManyToOne
	@JoinColumn(name = "ID_AGREGADOR")
	private Agregadores agregador;

	@Column(name = "NOMBRE", nullable = false)
	private String nombre;

	@Column(name = "INPUTMESSAGENAME", nullable = false)
	private String inputMessageName;

	@Column(name = "INPUTMESSAGETEXT", nullable = false)
	private String inputMessageText;

	@Column(name = "NAMESPACEURI", nullable = false)
	private String namespaceURI;

	@Column(name = "SOAPACTIONURI", nullable = false)
	private String SoapActionURI;

	@Column(name = "STYLE", nullable = true)
	private String style;

	@Column(name = "TARGETMETHODNAME", nullable = true)
	private String targetMethodName;

	@Column(name = "TARGETOBJECTURI", nullable = true)
	private String targetObjectURI;

	@Column(name = "TARGETURL", nullable = true)
	private String targetURL;

	@Column(name = "WSDL_AGREGADOR", nullable = true)
	private String wsdl_Agregador;

	@Column(name = "USUARIO", nullable = true)
	private String usuario;

	@Column(name = "CONTRASENIA", nullable = true)
	private String contrasenia;

	@Column(name = "SERVICE_NAME", nullable = true)
	private String serviceName;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<Parametros> parametros;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<Respuesta> respuestas;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<Depuracion_bck> depuraciones;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Agregadores getAgregador() {
		return agregador;
	}

	public void setAgregador(Agregadores agregador) {
		this.agregador = agregador;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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
		return SoapActionURI;
	}

	public void setSoapActionURI(String soapActionURI) {
		SoapActionURI = soapActionURI;
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

	public Set<Depuracion_bck> getDepuraciones() {
		return depuraciones;
	}

	public void setDepuraciones(Set<Depuracion_bck> depuraciones) {
		this.depuraciones = depuraciones;
	}

	@Override
	public String toString() {
		return "Metodos [id=" + id + ", nombre=" + nombre + "]";
	}

}
