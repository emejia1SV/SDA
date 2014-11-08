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

@Entity(name = "SDA_METODOS")
@Table(name = "SDA_METODOS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Metodos implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy = GenerationType.AUTO, generator = "Seq_Gen_Metodo")
	@SequenceGenerator(name = "Seq_Gen_Metodo", sequenceName = "SQ_SDA_METODOS")
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

	@Column(name = "SOAPACTIONURI")
	private String soapActionURI;

	@Column(name = "CONTENT_TYPE")
	private String contentType;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ID_AGREGADOR")
	private Agregadores agregador;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<Parametros> parametros;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<Respuesta> respuestas;


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

	public String getInputMessageText() {
		return inputMessageText;
	}

	public void setInputMessageText(String inputMessageText) {
		this.inputMessageText = inputMessageText;
	}

	public String getSoapActionURI() {
		return this.soapActionURI;
	}

	public void setSoapActionURI(String soapActionURI) {
		this.soapActionURI = soapActionURI;
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

	public Set<Parametros> getParametros() {
		if (parametros == null)
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
	 * @param metodo
	 *            the metodo to set
	 */
	public void setMetodo(Integer metodo) {
		this.metodo = metodo;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public String toString() {
		return "Metodos [id=" + id + ", metodo=" + metodo + "]";
	}
}