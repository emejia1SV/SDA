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

@Entity(name = "AGR_SERVICIOS")
@Table(name = "AGR_SERVICIOS", uniqueConstraints = { @UniqueConstraint(columnNames = { "ID" }) })
public class Servicios {

	@Id
	@Column(name = "ID", nullable = false)
	private int id;

	@Column(name = "WSDL_AGREGADOR", nullable = false)
	private String wsdl_Agregador;

	@Column(name = "USUARIO", nullable = false)
	private String usuario;

	@Column(name = "CONTRASENIA", nullable = false)
	private String contrasenia;

	@ManyToOne
	@JoinColumn(name = "ID_AGREGADOR")
	private Agregadores agregador;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "servicio", cascade = { CascadeType.ALL })
	private Set<Metodos> metodos;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getWsdl_Agregador() {
		return wsdl_Agregador;
	}

	public void setWsdl_Agregador(String wsdl_Agregador) {
		this.wsdl_Agregador = wsdl_Agregador;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getContrasenia() {
		return contrasenia;
	}

	public void setContrasenia(String contrasenia) {
		this.contrasenia = contrasenia;
	}

	public Agregadores getAgregador() {
		return agregador;
	}

	public void setAgregador(Agregadores agregador) {
		this.agregador = agregador;
	}

	public Set<Metodos> getMetodos() {
		return metodos;
	}

	public void setMetodos(Set<Metodos> metodos) {
		this.metodos = metodos;
	}

	@Override
	public String toString() {
		return "Servicios [id=" + this.id + "]";
	}
}
