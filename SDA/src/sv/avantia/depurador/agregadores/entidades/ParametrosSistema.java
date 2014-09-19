package sv.avantia.depurador.agregadores.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "SDA_PARAMETROS_SISTEMA")
@Table(name = "SDA_PARAMETROS_SISTEMA", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class ParametrosSistema implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "Seq_Gen_Parametros_Sistema", sequenceName = "SQ_SDA_PARAMETROS_SISTEMA")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Seq_Gen_Parametros_Sistema")
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "KEY", nullable = false)
	private String dato;

	@Column(name = "VALUE", nullable = false)
	private String valor;

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
	 * @return the dato
	 */
	public String getDato() {
		return dato;
	}

	/**
	 * @param dato
	 *            the dato to set
	 */
	public void setDato(String dato) {
		this.dato = dato;
	}

	/**
	 * @return the valor
	 */
	public String getValor() {
		return valor;
	}

	/**
	 * @param valor
	 *            the valor to set
	 */
	public void setValor(String valor) {
		this.valor = valor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ParametrosSistema [id=" + id + ", dato=" + dato + "]";
	}

}
