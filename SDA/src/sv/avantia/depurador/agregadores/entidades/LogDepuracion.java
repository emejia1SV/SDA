package sv.avantia.depurador.agregadores.entidades;

import java.io.Serializable;
import java.util.Date;

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

@Entity(name = "SDA_LOG_DEPURACION")
@Table(name = "SDA_LOG_DEPURACION", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class LogDepuracion implements Serializable {

	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy=GenerationType.AUTO, generator="Seq_Gen_Log")
    @SequenceGenerator(name="Seq_Gen_Log", sequenceName="SQ_SDA_DEPURACION_LOG")
	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "NUMERO")
	private String numero;

	@Column(name = "ID_ERROR")
	private String idError;

	@Column(name = "FECHA_PROCESAMIENTO")
	private Date fechaProcesamiento;

	@ManyToOne
	@JoinColumn(name = "ID_METODO_PROCESADO")
	private Metodos metodo;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getIdError() {
		return idError;
	}

	public void setIdError(String idError) {
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