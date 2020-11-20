package lekanich.eye;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;


/**
 * @author Lekanich
 */
public class EyeBundle extends AbstractBundle {
	/**
	 * The {@link java.util.ResourceBundle} path.
	 */
	@NonNls
	private static final String BUNDLE_NAME = "messages.EyeBundle";

	/**
	 * The {@link java.util.ResourceBundle} instance.
	 */
	private static final EyeBundle BUNDLE = new EyeBundle();

	private EyeBundle() {
		super(BUNDLE_NAME);
	}

	public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
		return BUNDLE.getMessage(key, params);
	}
}
