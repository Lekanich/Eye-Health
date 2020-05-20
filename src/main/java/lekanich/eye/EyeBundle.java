package lekanich.eye;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;


/**
 * @author Lekanich
 */
public class EyeBundle {
	private static final String EXERCISE_PREFIX = "eye.dialog.exercise.";

	/**
	 * The {@link java.util.ResourceBundle} path.
	 */
	@NonNls
	private static final String BUNDLE_NAME = "messages.EyeBundle";

	/**
	 * The {@link java.util.ResourceBundle} instance.
	 */
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
		return AbstractBundle.message(BUNDLE, key, params);
	}

	public static List<String> getExercises() {
		return BUNDLE.keySet().stream()
				.filter(key -> key.startsWith(EXERCISE_PREFIX))
				.map(EyeBundle::message)
				.collect(Collectors.toList());
	}
}
