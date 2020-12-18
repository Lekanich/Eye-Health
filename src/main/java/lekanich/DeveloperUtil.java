package lekanich;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * @author Lekanich
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeveloperUtil {
	private static final boolean DEBUG_MODE;

	static {
		/*
		 * static debug mode
		 */
		DEBUG_MODE = Optional.ofNullable(System.getProperty("eye.debug.run"))
				.filter(value -> Boolean.TRUE.toString().equalsIgnoreCase(value))
				.isPresent();
	}

	public static boolean isDebugMode() {
		return DEBUG_MODE;
	}
}
