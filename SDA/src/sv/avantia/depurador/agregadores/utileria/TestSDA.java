package sv.avantia.depurador.agregadores.utileria;

import java.util.ArrayList;
import java.util.List;

import sv.avantia.depurador.agregadores.hilo.GestionarParametrizacion;

public class TestSDA {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> moviles = new ArrayList<String>();
		moviles.add("50257128949");
		GestionarParametrizacion gestion = new GestionarParametrizacion();
		System.out.println(gestion.depuracionBajaMasiva(moviles,"Servicio Web", true));
	}
}
