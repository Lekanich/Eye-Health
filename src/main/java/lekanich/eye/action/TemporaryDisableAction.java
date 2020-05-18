package lekanich.eye.action;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;


/**
 * @author Lekanich
 */
public class TemporaryDisableAction extends AnAction {
	@NotNull
	private static final String STOP_UNTIL_THE_END_OF_THE_DAY = "stopUntilTheTime";

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		long untilTimeUTC = calcMidnightTodaySeconds();

		// set properties to end of the date so we can set end of the day in your timezone
		PropertiesComponent.getInstance()
				.setValue(STOP_UNTIL_THE_END_OF_THE_DAY, String.valueOf(untilTimeUTC));
	}

	public static long calcMidnightTodaySeconds() {
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		LocalDateTime todayMidnight = LocalDateTime.of(today, LocalTime.MIDNIGHT).plusDays(1);
		return todayMidnight.toEpochSecond(ZoneOffset.UTC);
	}

	private static long nowSeconds() {
		return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
	}

	private static long getTimeToStopUntil() {
		return Long.parseLong(PropertiesComponent.getInstance().getValue(STOP_UNTIL_THE_END_OF_THE_DAY, "0"));
	}

	public static boolean isTemporaryDisabled() {
		long nowSecondsUTC = nowSeconds();
		return getTimeToStopUntil() >= nowSecondsUTC;
	}
}
