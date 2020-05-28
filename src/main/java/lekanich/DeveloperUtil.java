package lekanich;

import java.util.Optional;


/**
 * @author Lekanich
 */
public class DeveloperUtil {
	private static final boolean debugMode;

	static {
		/*
		 * static debug mode
		 */
		debugMode = Optional.ofNullable(System.getProperty("eye.debug.run"))
				.filter(value -> Boolean.TRUE.toString().toLowerCase().equals(value.toLowerCase()))
				.isPresent();
	}

	public static boolean isDebugMode() {
		return debugMode;
	}
}
