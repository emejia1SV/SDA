package sv.avantia.depurador.agregadores.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = "AGR_DEPURACION_BCK")
@Table(name = "AGR_DEPURACION_BCK", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Depuracion_bck {

	@Id
	@Column(name = "ID", nullable = false)
	private int id;

	@Column(name = "NUMERO", nullable = false)
	private String numero;

	@Column(name = "ID_ERROR", nullable = false)
	private int idError;

	@Column(name = "FECHA_PROCESAMIENTO", nullable = false)
	private Date fechaProcesamiento;

	@ManyToOne
	@JoinColumn(name = "ID_METODO_PROCESADO")
	private Metodos metodo;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public int getIdError() {
		return idError;
	}

	public void setIdError(int idError) {
		this.idError = idError;
	}

	public Date getFechaProcesamiento() {
		return fechaProcesamiento;
	}

	public void setFechaProcesamiento(Date fechaProcesamiento) {
		this.fechaProcesamiento = fechaProcesamiento;
	}

	public Metodos getMetodo() {
		return metodo;
	}

	public void setMetodo(Metodos metodo) {
		this.metodo = metodo;
	}

	@Override
	public String toString() {
		return "Depuracion_bck [id=" + id + "]";
	}

}
