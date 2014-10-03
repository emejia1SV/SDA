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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity(name = "SDA_RESPUESTAS")
@Table(name = "SDA_RESPUESTAS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Respuesta implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy=GenerationType.AUTO, generator="Seq_Gen_Respuesta")
    @SequenceGenerator(name="Seq_Gen_Respuesta", sequenceName="SQ_SDA_RESPUESTAS")
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "NOMBRE", nullable = false)
	private String nombre;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ID_METODO")
	private Metodos metodo;

	@ManyToOne(fetch = FetchType.EAGER)
	@OnDelete(action=OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "ID_RESPUESTA")
	private CatRespuestas catRespuesta;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "respuesta", cascade = { CascadeType.ALL })
	private Set<ResultadosRespuesta> resultadosRespuestas;

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
	 * @return the nombre
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * @param nombre the nombre to set
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * @return the metodo
	 */
	public Metodos getMetodo() {
		return metodo;
	}

	/**
	 * @param metodo the metodo to set
	 */
	public void setMetodo(Metodos metodo) {
		this.metodo = metodo;
	}

	/**
	 * @return the resultadosRespuestas
	 */
	public Set<ResultadosRespuesta> getResultadosRespuestas() {
		return resultadosRespuestas;
	}

	/**
	 * @param resultadosRespuestas the resultadosRespuestas to set
	 */
	public void setResultadosRespuestas(
			Set<ResultadosRespuesta> resultadosRespuestas) {
		this.resultadosRespuestas = resultadosRespuestas;
	}

	/**
	 * @return the catRespuesta
	 */
	public CatRespuestas getCatRespuesta() {
		return catRespuesta;
	}

	/**
	 * @param catRespuesta the catRespuesta to set
	 */
	public void setCatRespuesta(CatRespuestas catRespuesta) {
		this.catRespuesta = catRespuesta;
	}
	
	@Override
	public String toString() {
		return "Respuesta [id=" + id + ", nombre=" + nombre + "]";
	}
}