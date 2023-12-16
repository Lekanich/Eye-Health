package lekanich.eye;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;


/**
 * @author Lekanich
 */
public class EyeBundle extends AbstractBundle {
    @NonNls
    private static final String BUNDLE_NAME = "messages.EyeBundle";

    @NonNls
    private static final EyeBundle BUNDLE = new EyeBundle();

    private EyeBundle() {
        super(BUNDLE_NAME);
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE_NAME) final String key, final Object... params) {
        return BUNDLE.getMessage(key, params);
    }
}
