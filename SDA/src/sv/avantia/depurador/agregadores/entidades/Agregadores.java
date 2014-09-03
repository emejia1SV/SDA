package sv.avantia.depurador.agregadores.entidades;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "AGR_AGREGADORES")
@Table(name = "AGR_AGREGADORES", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Agregadores {

	@Id
	@Column(name = "ID", nullable = false)
	private int id;

	@Column(name = "ESTADO", nullable = false)
	private int estado;

	@Column(name = "NOMBRE_AGREGADOR", nullable = false)
	private String nombre_agregador;

	@ManyToOne
	@JoinColumn(name = "ID_PAIS")
	private Pais pais;

	@OneToMany(fetch = FetchType.EAGER, mappedBy="agregador", cascade={CascadeType.ALL})
	private Set<Servicios> servicios;

	public Agregadores() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEstado() {
		return estado;
	}

	public void setEstado(int estado) {
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

	public Set<Servicios> getServicios() {
		return servicios;
	}

	public void setServicios(Set<Servicios> servicios) {
		this.servicios = servicios;
	}

	@Override
	public String toString() {
		return "Agregadores [id=" + id + ", nombre_agregador=" + nombre_agregador + "]";
	}

}
