package sv.avantia.depurador.agregadores.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "AGR_RESPUESTAS")
@Table(name = "AGR_RESPUESTAS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Respuesta {

	@Id
	@Column(name = "ID", nullable = false)
	private int id;

	@Column(name = "TIPO", nullable = false)
	private String tipo;

	@Column(name = "NOMBRE", nullable = false)
	private String nombre;

	@Column(name = "POSICION", nullable = false)
	private int posicion;

	@ManyToOne
	@JoinColumn(name = "ID_METODO")
	private Metodos metodo;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getPosicion() {
		return posicion;
	}

	public void setPosicion(int posicion) {
		this.posicion = posicion;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Metodos getMetodo() {
		return metodo;
	}

	public void setMetodo(Metodos metodo) {
		this.metodo = metodo;
	}

	@Override
	public String toString() {
		return "Respuesta [id=" + id + ", nombre=" + nombre + "]";
	}

}
