package sv.avantia.depurador.agregadores.entidades;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "SDA_USUARIO_SISTEMA")
@Table(name = "SDA_USUARIO_SISTEMA", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class UsuarioSistema implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "Seq_Gen_Parametros_Sistema", sequenceName = "SQ_SDA_PARAMETROS_SISTEMA")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Seq_Gen_Parametros_Sistema")
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "USUARIO", nullable = false)
	private String usuario;

	@Column(name = "ESTADO", nullable = false)
	private Integer estado;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "usuarioSistema", cascade = { CascadeType.ALL })
	private Set<LogDepuracion> depuraciones;

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
	 * @return the usuario
	 */
	public String getUsuario() {
		return usuario;
	}

	/**
	 * @param usuario
	 *            the usuario to set
	 */
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	/**
	 * @return the estado
	 */
	public Integer getEstado() {
		return estado;
	}

	/**
	 * @param estado
	 *            the estado to set
	 */
	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	/**
	 * @return the depuraciones
	 */
	public Set<LogDepuracion> getDepuraciones() {
		return depuraciones;
	}

	/**
	 * @param depuraciones
	 *            the depuraciones to set
	 */
	public void setDepuraciones(Set<LogDepuracion> depuraciones) {
		this.depuraciones = depuraciones;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UsuarioSistema [id=" + id + ", usuario=" + usuario + "]";
	}

}
