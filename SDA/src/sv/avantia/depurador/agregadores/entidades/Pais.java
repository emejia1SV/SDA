package sv.avantia.depurador.agregadores.entidades;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "AGR_PAISES")
@Table(name = "AGR_PAISES", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Pais {

	@Id
	@Column(name = "ID", nullable = false)
	private int id;

	@Column(name = "PAIS", nullable = false)
	private String nombre;

	@Column(name = "CODIGO", nullable = false)
	private String codigo;

	@Column(name = "STATUS", nullable = false)
	private int estado;

	@OneToMany(fetch = FetchType.EAGER, mappedBy="pais", cascade={CascadeType.ALL})
	private Set<Agregadores> agregadores;

	public int getId() {
		return id;
	}

	public void setId(int id) {
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

	public int getEstado() {
		return estado;
	}

	public void setEstado(int estado) {
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
