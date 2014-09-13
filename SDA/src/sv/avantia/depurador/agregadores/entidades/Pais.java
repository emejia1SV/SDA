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

@Entity(name = "SDA_PAISES")
@Table(name = "SDA_PAISES", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Pais implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(name="Seq_Gen_Pais", sequenceName="SQ_SDA_PAIS")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="Seq_Gen_Pais")
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "PAIS", nullable = false)
	private String nombre;

	@Column(name = "CODIGO", nullable = false)
	private String codigo;

	@Column(name = "STATUS", nullable = false)
	private Integer estado;

	@OneToMany(fetch = FetchType.EAGER, mappedBy="pais", cascade={CascadeType.ALL})
	private Set<Agregadores> agregadores;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Integer getEstado() {
		return estado;
	}

	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	public Set<Agregadores> getAgregadores() {
		return agregadores;
	}

	public void setAgregadores(Set<Agregadores> agregadores) {
		this.agregadores = agregadores;
	}

	@Override
	public String toString() {
		return "Pais [id=" + this.id + ", nombre=" + this.nombre + "]";
	}
}