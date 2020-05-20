package lekanich.eye.action;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import lekanich.eye.listener.EyeHelpStatusListener;
import lekanich.eye.ui.EyeHelpDialog;


/**
 * @author Lekanich
 */
public class DisableTemporaryEyeHelpAction extends AnAction {
	@NotNull
	private static final String STOP_UNTIL_THE_END_OF_THE_DAY = "stopUntilTheTime";

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		disableTemporaryEyeHelp();
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		e.getPresentation().setEnabled(!isTemporaryDisabled());
	}

	public static void removeTemporaryStopTime() {
		// set properties to end of the date so we can set end of the day in your timezone
		PropertiesComponent.getInstance()
				.setValue(STOP_UNTIL_THE_END_OF_THE_DAY, null);
	}

	public static void disableTemporaryEyeHelp() {
		long untilTimeUTC = calcMidnightTodaySeconds();

		// set properties to end of the date so we can set end of the day in your timezone
		PropertiesComponent.getInstance()
				.setValue(STOP_UNTIL_THE_END_OF_THE_DAY, String.valueOf(untilTimeUTC));

		// invoke eye help after the midnight
		EyeHelpDialog.publishNextRestEventWithDelay(untilTimeUTC - nowSeconds());

		// notify about temporary disabling
		ApplicationManager.getApplication().getMessageBus()
				.syncPublisher(EyeHelpStatusListener.EYE_HELP_STATUS_TOPIC)
				.statusChanged(EyeHelpStatusListener.Status.TEMPORARY_DISABLED);
	}

	public static long calcMidnightTodaySeconds() {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		LocalDateTime todayMidnight = LocalDateTime.of(today, LocalTime.MIDNIGHT).plusDays(1);
		return todayMidnight.toEpochSecond(ZoneOffset.UTC);
	}

	private static long nowSeconds() {
		return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
	}

	public static long getTimeToStopUntil() {
		return PropertiesComponent.getInstance().getLong(STOP_UNTIL_THE_END_OF_THE_DAY, 0L);
	}

	public static boolean isTemporaryDisabled() {
		long nowSecondsUTC = nowSeconds();
		return getTimeToStopUntil() >= nowSecondsUTC;
	}
}
