package icons;

import javax.swing.Icon;
import com.intellij.openapi.util.IconLoader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;


/**
 * @author Lekanich
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EyeHelpIcons {
	public static final Icon EYE_ON = IconLoader.getIcon("/icons/action_eye.svg", EyeHelpIcons.class);
	public static final Icon EYE_ON_WINTER = IconLoader.getIcon("/icons/action_eye_winter.svg", EyeHelpIcons.class);
	public static final Icon EYE_ON_AUTUMN = IconLoader.getIcon("/icons/action_eye_autumn.svg", EyeHelpIcons.class);
	public static final Icon EYE_ON_UKRAINE = IconLoader.getIcon("/icons/action_eye_ukraine.svg", EyeHelpIcons.class);
	public static final Icon EYE_OFF = IconLoader.getIcon("/icons/inactive_action_eye.svg", EyeHelpIcons.class);
	public static final Icon EYE_USA = IconLoader.getIcon("/icons/action_eye_usa.svg", EyeHelpIcons.class);
	public static final Icon EYE_EU = IconLoader.getIcon("/icons/action_eye_eu.svg", EyeHelpIcons.class);

	@Getter
	@RequiredArgsConstructor
	public enum EyeType implements Icon {
		DEFAULT(EYE_ON),
		AUTUMN(EYE_ON_AUTUMN),
		WINTER(EYE_ON_WINTER),
		UKRAINE(EYE_ON_UKRAINE),
		USA(EYE_USA),
		EU(EYE_EU);

		@Delegate
		private final Icon icon;
	}
}
