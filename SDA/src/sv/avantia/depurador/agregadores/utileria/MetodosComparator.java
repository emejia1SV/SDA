package sv.avantia.depurador.agregadores.utileria;

import java.util.Comparator;

import sv.avantia.depurador.agregadores.entidades.Metodos;

public class MetodosComparator implements Comparator<Metodos>{

	@Override
	public int compare(Metodos o1, Metodos o2) {
		return (o1.getMetodo()>o2.getMetodo() ? -1 : (o1.getMetodo()==o2.getMetodo() ? 0 : 1));
	}

} 