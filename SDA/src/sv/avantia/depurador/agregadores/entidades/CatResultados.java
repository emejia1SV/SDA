package sv.avantia.depurador.agregadores.entidades;

import java.io.Serializable;
import java.util.List;

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

@Entity(name = "SDA_CAT_RESULTADOS")
@Table(name = "SDA_CAT_RESULTADOS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class CatResultados implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy = GenerationType.AUTO, generator = "Seq_Gen_CatResultado")
	@SequenceGenerator(name = "Seq_Gen_CatResultado", sequenceName = "SQ_SDA_CAT_RESULTADO")
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "DATO", nullable = false)
	private String dato;

	@Column(name = "VALOR", nullable = false)
	private String valor;
	
	@OneToMany(mappedBy="catResultado", fetch = FetchType.EAGER)
	private List<ResultadosRespuesta> resultadosRespuesta;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
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
	 * @param dato the dato to set
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
	 * @param valor the valor to set
	 */
	public void setValor(String valor) {
		this.valor = valor;
	}

	/**
	 * @return the resultadosRespuesta
	 */
	public List<ResultadosRespuesta> getResultadosRespuesta() {
		return resultadosRespuesta;
	}

	/**
	 * @param resultadosRespuesta the resultadosRespuesta to set
	 */
	public void setResultadosRespuesta(List<ResultadosRespuesta> resultadosRespuesta) {
		this.resultadosRespuesta = resultadosRespuesta;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return id.toString();
	}
}
