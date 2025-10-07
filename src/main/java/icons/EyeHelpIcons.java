package icons;

import javax.swing.Icon;
import com.intellij.openapi.util.IconLoader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;


/**
 * @author Lekanich
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EyeHelpIcons {
	public static final Icon EYE_ON = getIcon("/icons/action_eye.svg");
	public static final Icon EYE_ON_WINTER = getIcon("/icons/action_eye_winter.svg");
	public static final Icon EYE_ON_AUTUMN = getIcon("/icons/action_eye_autumn.svg");
	public static final Icon EYE_ON_UKRAINE = getIcon("/icons/action_eye_ukraine.svg");
	public static final Icon EYE_OFF = getIcon("/icons/inactive_action_eye.svg");
	public static final Icon EYE_USA = getIcon("/icons/action_eye_usa.svg");
	public static final Icon EYE_EU = getIcon("/icons/action_eye_eu.svg");
	public static final Icon EYE_CHINA = getIcon("/icons/action_eye_china.svg");

	private static @NotNull Icon getIcon(final String path) {
		return IconLoader.getIcon(path, EyeHelpIcons.class);
	}

	@Getter
	@RequiredArgsConstructor
	public enum EyeType implements Icon {
		DEFAULT(EYE_ON, "Default"),
		AUTUMN(EYE_ON_AUTUMN, "Autumn"),
		WINTER(EYE_ON_WINTER, "Winter"),
		UKRAINE(EYE_ON_UKRAINE, "Ukraine"),
		USA(EYE_USA, "USA"),
		EU(EYE_EU, "EU"),
		CHINA(EYE_CHINA, "China");

		@Delegate
		private final Icon icon;

		private final String name;
	}
}
