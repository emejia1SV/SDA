package sv.avantia.depurador.agregadores.entidades;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "SDA_RESPUESTAS")
@Table(name = "SDA_RESPUESTAS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Respuesta implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy=GenerationType.AUTO, generator="Seq_Gen_Respuesta")
    @SequenceGenerator(name="Seq_Gen_Respuesta", sequenceName="SQ_SDA_RESPUESTAS")
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "TIPO", nullable = false)
	private String tipo;

	@Column(name = "NOMBRE", nullable = false)
	private String nombre;

	@ManyToOne
	@JoinColumn(name = "ID_METODO")
	private Metodos metodo;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
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