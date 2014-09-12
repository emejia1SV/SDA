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

@Entity(name = "SDA_PARAMETROS")
@Table(name = "SDA_PARAMETROS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Parametros implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy=GenerationType.AUTO, generator="Seq_Gen_Parametro")
    @SequenceGenerator(name="Seq_Gen_Parametro", sequenceName="SQ_SDA_PARAMETROS")
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "NOMBRE", nullable = false)
	private String nombre;

	@Column(name = "TIPO", nullable = false)
	private String tipo;

	@ManyToOne
	@JoinColumn(name = "ID_METODO")
	private Metodos metodo;

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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Metodos getMetodo() {
		return metodo;
	}

	public void setMetodo(Metodos metodo) {
		this.metodo = metodo;
	}

	@Override
	public String toString() {
		return "Parametros [id=" + id + ", nombre=" + nombre + "]";
	}
}