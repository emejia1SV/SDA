package sv.avantia.depurador.agregadores.entidades;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "SDA_CAT_RESPUESTAS")
@Table(name = "SDA_CAT_RESPUESTAS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class CatRespuestas implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy = GenerationType.AUTO, generator = "Seq_Gen_CatRespuesta")
	@SequenceGenerator(name = "Seq_Gen_CatRespuesta", sequenceName = "SQ_SDA_CAT_RESPUESTAS")
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "NOMBRE", nullable = false)
	private String nombre;
	
	@OneToOne(fetch = FetchType.EAGER, mappedBy = "catRespuestas", cascade = CascadeType.ALL)
	private Respuesta respuesta;

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
	 * @return the nombre
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * @param nombre
	 *            the nombre to set
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CatRespuestas [id=" + id + "]";
	}
}