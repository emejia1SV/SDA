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

@Entity(name = "AGR_METODOS")
@Table(name = "AGR_METODOS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Metodos {

	@Id
	@Column(name = "ID", nullable = false)
	private int id;

	@Column(name = "NOMBRE", nullable = false)
	private String nombre;

	@ManyToOne
	@JoinColumn(name = "ID_SERVICIOS")
	private Servicios servicio;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<Parametros> parametros;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<Respuesta> respuestas;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "metodo", cascade = { CascadeType.ALL })
	private Set<Depuracion_bck> depuraciones;

	// private OperationInfo operacionSRV;

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

	public Servicios getServicio() {
		return servicio;
	}

	public void setServicio(Servicios servicio) {
		this.servicio = servicio;
	}

	public Set<Parametros> getParametros() {
		return parametros;
	}

	public void setParametros(Set<Parametros> parametros) {
		this.parametros = parametros;
	}

	public Set<Respuesta> getRespuestas() {
		return respuestas;
	}

	public void setRespuestas(Set<Respuesta> respuestas) {
		this.respuestas = respuestas;
	}

	public Set<Depuracion_bck> getDepuraciones() {
		return depuraciones;
	}

	public void setDepuraciones(Set<Depuracion_bck> depuraciones) {
		this.depuraciones = depuraciones;
	}

	@Override
	public String toString() {
		return "Metodos [id=" + id + ", nombre=" + nombre + "]";
	}
	/*
	 * public OperationInfo getOperacionSRV() { return operacionSRV; }
	 * 
	 * public void setOperacionSRV(OperationInfo operacionSRV) {
	 * this.operacionSRV = operacionSRV; }
	 */
}
