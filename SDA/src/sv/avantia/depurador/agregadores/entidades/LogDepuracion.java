package sv.avantia.depurador.agregadores.entidades;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "SDA_LOG_DEPURACION")
@Table(name = "SDA_LOG_DEPURACION", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class LogDepuracion implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy = GenerationType.AUTO, generator = "Seq_Gen_Log")
	@SequenceGenerator(name = "Seq_Gen_Log", sequenceName = "SQ_SDA_DEPURACION_LOG")
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "SUSCRIPTOR")
	private String numero;

	@Column(name = "RESPUESTA")
	private String respuesta;

	@Column(name = "ESTADO_TRANSACCION")
	private String estadoTransaccion;

	@Column(name = "FECHA_TRANSACCION")
	private Date fechaTransaccion;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ID_METODO")
	private Metodos metodo;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ID_USUARIO_SISTEMA")
	private UsuarioSistema usuarioSistema;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the numero
	 */
	public String getNumero() {
		return numero;
	}

	/**
	 * @param numero
	 *            the numero to set
	 */
	public void setNumero(String numero) {
		this.numero = numero;
	}

	/**
	 * @return the respuesta
	 */
	public String getRespuesta() {
		return respuesta;
	}

	/**
	 * @param respuesta
	 *            the respuesta to set
	 */
	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

	/**
	 * @return the estadoTransaccion
	 */
	public String getEstadoTransaccion() {
		return estadoTransaccion;
	}

	/**
	 * @param estadoTransaccion
	 *            the estadoTransaccion to set
	 */
	public void setEstadoTransaccion(String estadoTransaccion) {
		this.estadoTransaccion = estadoTransaccion;
	}

	/**
	 * @return the fechaTransaccion
	 */
	public Date getFechaTransaccion() {
		return fechaTransaccion;
	}

	/**
	 * @param fechaTransaccion
	 *            the fechaTransaccion to set
	 */
	public void setFechaTransaccion(Date fechaTransaccion) {
		this.fechaTransaccion = fechaTransaccion;
	}

	/**
	 * @return the metodo
	 */
	public Metodos getMetodo() {
		return metodo;
	}

	/**
	 * @param metodo
	 *            the metodo to set
	 */
	public void setMetodo(Metodos metodo) {
		this.metodo = metodo;
	}

	/**
	 * @return the usuarioSistema
	 */
	public UsuarioSistema getUsuarioSistema() {
		return usuarioSistema;
	}

	/**
	 * @param usuarioSistema
	 *            the usuarioSistema to set
	 */
	public void setUsuarioSistema(UsuarioSistema usuarioSistema) {
		this.usuarioSistema = usuarioSistema;
	}

	@Override
	public String toString() {
		return "LogDepuracion [id=" + id + "]";
	}
}