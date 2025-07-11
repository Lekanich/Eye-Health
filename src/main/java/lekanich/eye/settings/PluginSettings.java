package lekanich.eye.settings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import icons.EyeHelpIcons.EyeType;
import lekanich.DeveloperUtil;
import lekanich.eye.ui.EyeHelpDialog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;


/**
 * @author Lekanich
 */
@State(
		name = "EyeHelpSettings",
		storages = {@Storage(value = "lekanich.eye-help.xml")}
)
public class PluginSettings implements PersistentStateComponent<PluginSettings.PluginAppState>, Disposable {
	private final PluginAppState state = new PluginAppState();

	private final AtomicBoolean disposed = new AtomicBoolean(false);

	public static PluginSettings getInstance() {
		return ApplicationManager.getApplication().getService(PluginSettings.class);
	}

	@NotNull
	@Override
	public PluginSettings.PluginAppState getState() {
		return state;
	}

	@Override
	public void loadState(@NotNull final PluginSettings.PluginAppState state) {
		XmlSerializerUtil.copyBean(state, this.state);
	}

	@Override
	public void dispose() {
		disposed.set(true);
	}

	public boolean isDisposed() {
		return disposed.get();
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class PluginAppState {
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
		private long durationPostpone = TimeUnit.MINUTES.toSeconds(2);

		/**
		 * in seconds
		 */
		private long durationBreak = 30;

		/**
		 * It should be less than durationWorkBeforeBreak
		 * in seconds
		 */
		private long idleTime = TimeUnit.MINUTES.toSeconds(10);

		/**
		 * in seconds
		 */
		private long durationWorkBeforeBreak = TimeUnit.MINUTES.toSeconds(30);

		/**
		 * Look and feel of eye icon
		 */
		private EyeType eyeType = EyeType.DEFAULT;

		/**
		 * Show when the window of IDE is minimized
		 */
		private boolean showWhenMinimized = false;

		/**
		 * Enable lunchtime specific settings
		 */
		private boolean enableLunchTime = false;

		/**
		 * Lunchtime in the minutes from the start of the day
		 */
		private int lunchTimeInMinutes = -1;

		/**
		 * To keep if we should show the lunchtime exercise today
		 */
		private String lastLunchDate = null;

		public long getDurationWorkBeforeBreak() {
			return DeveloperUtil.isDebugMode() ? 15 : durationWorkBeforeBreak;
		}

		public long getIdleTime() {
			return DeveloperUtil.isDebugMode() ? 13 : idleTime;
		}

		public long getDurationBreak() {
			return DeveloperUtil.isDebugMode() ? 10 : durationBreak;
		}

		public LocalTime getLunchTime() {
			return lunchTimeInMinutes > 0 ? LocalTime.of(lunchTimeInMinutes / 60, lunchTimeInMinutes % 60) : null;
		}
	}

	public static boolean isDisabled() {
		final PluginAppState state = PluginSettings.getInstance().getState();
		// check if it is disabled
		if (!state.isEnable()) {
			return true;
		}

		// check if is it temporary disabled
		return TemporaryDisableEyeHelpSetting.isTemporaryDisabled();
	}

	public static boolean isActiveWhenMinimized() {
		return PluginSettings.getInstance().getState().isShowWhenMinimized();
	}

	public static final class TemporaryDisableEyeHelpSetting {
		@NotNull
		private static final String STOP_UNTIL_THE_END_OF_THE_DAY = "stopUntilTheTime";

		public static void reactivate() {
			// set properties to end of the date so we can set end of the day in your timezone
			PropertiesComponent.getInstance()
					.setValue(STOP_UNTIL_THE_END_OF_THE_DAY, null);

			EyeHelpDialog.publishNextRestEvent();
		}

		public static void deactivateEyeHelp() {
			final long untilTimeUTC = calcMidnightTodaySeconds();

			// set properties to end of the date so we can set end of the day in your timezone
			PropertiesComponent.getInstance()
					.setValue(STOP_UNTIL_THE_END_OF_THE_DAY, String.valueOf(untilTimeUTC));

			// invoke eye help after the midnight
			EyeHelpDialog.publishNextRestEventWithDelay(untilTimeUTC - nowSeconds());
		}

		private static long calcMidnightTodaySeconds() {
			final LocalDate today = LocalDate.now(ZoneId.systemDefault());
			final LocalDateTime todayMidnight = LocalDateTime.of(today, LocalTime.MIDNIGHT).plusDays(1);
			return todayMidnight.toEpochSecond(ZoneOffset.UTC);
		}

		private static long nowSeconds() {
			return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
		}

		public static long getTimeToStopUntil() {
			return PropertiesComponent.getInstance().getLong(STOP_UNTIL_THE_END_OF_THE_DAY, 0L);
		}

		public static boolean isTemporaryDisabled() {
			final long nowSecondsUTC = nowSeconds();
			return getTimeToStopUntil() >= nowSecondsUTC;
		}
	}
}
