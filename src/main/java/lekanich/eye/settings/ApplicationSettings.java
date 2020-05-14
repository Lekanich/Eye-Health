package lekanich.eye.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * @author Lekanich
 */
@State(
		name = "EyeHelpSettings",
		storages = {@Storage(value = "lekanich.eye-help.xml")}
)
public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings.EyeHelpState> {
	private final EyeHelpState state = new EyeHelpState();

	public static ApplicationSettings getInstance() {
		return ServiceManager.getService(ApplicationSettings.class);
	}

	@NotNull
	@Override
	public EyeHelpState getState() {
		return state;
	}

	@Override
	public void loadState(@NotNull EyeHelpState state) {
		XmlSerializerUtil.copyBean(state, this.state);
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class EyeHelpState {
		/**
		 * turn on\off Eye Help
		 */
		private boolean enable = true;

		/**
		 * Allow to postpone eye exercise on 1 minute or chosen time
		 */
		private boolean postpone = true;

		/**
		 * shift of your eye exercise on that time
		 * in seconds
		 */
		private int postponeTimeSec = 60;

		/**
		 * in seconds
		 */
		private int shortBreakDurationSec = 15;

		/**
		 * in minutes
		 * TODO: change
		 */
		private int workingTimeBetweenShortBreaksMin = 15;
	}
}
