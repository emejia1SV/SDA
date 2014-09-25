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

@Entity(name = "SDA_AGREGADORES")
@FilterDef(name="SDA_AGREGADORES_FILTER", defaultCondition="ESTADO = 1")
@Table(name = "SDA_AGREGADORES", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Agregadores implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy=GenerationType.AUTO, generator="Seq_Gen_Agregador")
    @SequenceGenerator(name="Seq_Gen_Agregador", sequenceName="SQ_SDA_AGREGADORES")
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "ESTADO", nullable = false)
	private Integer estado;

	@Column(name = "NOMBRE_AGREGADOR", nullable = false)
	private String nombre_agregador;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ID_PAIS")
	private Pais pais;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "agregador", cascade = { CascadeType.ALL })
	private Set<Metodos> metodos;

	public Agregadores() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEstado() {
		return estado;
	}

	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	public String getNombre_agregador() {
		return nombre_agregador;
	}

	public void setNombre_agregador(String nombre_agregador) {
		this.nombre_agregador = nombre_agregador;
	}

	public Pais getPais() {
		return pais;
	}

	public void setPais(Pais pais) {
		this.pais = pais;
	}

	public Set<Metodos> getMetodos() {
		if(metodos == null)
			metodos = new HashSet<Metodos>();
		return metodos;
	}

	public void setMetodos(Set<Metodos> metodos) {
		this.metodos = metodos;
	}

	@Override
	public String toString() {
		return "Agregadores [id=" + id + ", nombre_agregador=" + nombre_agregador + "]";
	}
}