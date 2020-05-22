package lekanich;

import java.util.Optional;


/**
 * @author Lekanich
 */
public class DeveloperUtil {
	public static boolean isDebugMode() {
		return Optional.ofNullable(System.getProperty("eye.debug.run"))
				.filter(value -> Boolean.TRUE.toString().toLowerCase().equals(value.toLowerCase()))
				.isPresent();
	}
}
