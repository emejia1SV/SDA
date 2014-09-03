package sv.avantia.depurador.agregadores.utileria;

import java.net.URL;

import org.apache.log4j.PropertyConfigurator;

public class Log4jInit {

	public static final String propertiesFile = "log4j.properties";
	private static final boolean watch = true;
	private static boolean instance = false;

	public static void init() {
		if (!instance) {
			URL propsURL = Log4jInit.class.getClassLoader().getResource(
					propertiesFile);

			if (propsURL == null) {
				System.err.println(propertiesFile + "No se encontro archivo de propiedades");
				return;
			}

			if (watch) {
				PropertyConfigurator.configureAndWatch(propsURL.getFile());
			} else {
				PropertyConfigurator.configure(propsURL.getFile());
			}
			instance = true;
		}

	}
}
