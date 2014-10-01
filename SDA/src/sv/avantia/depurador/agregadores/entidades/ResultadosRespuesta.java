package sv.avantia.depurador.agregadores.entidades;

import java.io.Serializable;

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

@Entity(name = "SDA_RESULTADOS_RESPUESTA")
@Table(name = "SDA_RESULTADOS_RESPUESTA", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class ResultadosRespuesta implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy = GenerationType.AUTO, generator = "Seq_Gen_Result_Respuesta")
	@SequenceGenerator(name = "Seq_Gen_Result_Respuesta", sequenceName = "SQ_SDA_RESULTADO_RESPUESTAS")
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ID_RESPUESTA")
	private Respuesta respuesta;

	@Column(name = "VALOR", nullable = false)
	private String valor;
	
	@Column(name = "DATO", nullable = false)
	private String dato;

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
	 * @return the respuesta
	 */
	public Respuesta getRespuesta() {
		return respuesta;
	}

	/**
	 * @param respuesta
	 *            the respuesta to set
	 */
	public void setRespuesta(Respuesta respuesta) {
		this.respuesta = respuesta;
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

	/**
	 * @return the dato
	 */
	public String getDato() {
		return dato;
	}

	/**
	 * @param dato the dato to set
	 */
	public void setDato(String dato) {
		this.dato = dato;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResultadosRespuesta [id=" + id + "]";
	}
}
