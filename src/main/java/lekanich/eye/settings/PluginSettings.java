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
import lekanich.eye.action.DisableTemporaryEyeHelpAction;


/**
 * @author Lekanich
 */
@State(
		name = "EyeHelpSettings",
		storages = {@Storage(value = "lekanich.eye-help.xml")}
)
public class PluginSettings implements PersistentStateComponent<PluginSettings.EyeHelpState> {
	private final EyeHelpState state = new EyeHelpState();

	public static PluginSettings getInstance() {
		return ServiceManager.getService(PluginSettings.class);
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
		private int durationPostpone = 120;

		/**
		 * in seconds
		 */
		private int durationBreak = 20;

		/**
		 * in minutes
		 */
		private int durationWorkBeforeBreak = 15;
	}

	public static boolean isDisabled() {
		EyeHelpState state = PluginSettings.getInstance().getState();
		// check if it is disabled
		if (!state.isEnable()) {
			return true;
		}

		// check if is it temporary disabled
		return DisableTemporaryEyeHelpAction.isTemporaryDisabled();
	}
}
