package lekanich.eye.logic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lekanich.DeveloperUtil;
import lekanich.eye.EyeBundle;
import lekanich.eye.EyeExercise;
import lekanich.eye.settings.PluginSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lekanich
 */
public record ExerciseTuple(String exercise, long durationBreak) {
	public static ExerciseTuple findExercise() {
		final PluginSettings.PluginAppState state = PluginSettings.getInstance().getState();

		final int delta = toMinute(LocalTime.now()) - toMinute(state.getLunchTime());

		boolean isLunchExercise = showLunch(delta > 0 && delta < 60, state);
		final String message = isLunchExercise
				? getLunchTimeText()
				: findExerciseMessage();

		final long durationBreak = state.getDurationBreak() * (isLunchExercise ? 2 : 1);
		return new ExerciseTuple(message, durationBreak);
	}

	private static boolean showLunch(final boolean isLunchTime, final PluginSettings.PluginAppState state) {
		if (!isLunchTime) {
			return false;
		}

		final String lastLunchDate = state.getLastLunchDate();
		final String dateNow = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
		if (!dateNow.equals(lastLunchDate)) {
			state.setLastLunchDate(dateNow);
			return true;
		} else {
			return false;
		}
	}

	@NotNull
	private static String findExerciseMessage() {
		final List<EyeExercise> exercises = EyeExercise.findExercises();
		if (exercises.isEmpty()) {
			return EyeBundle.message("eye.dialog.exercises.dummy");
		}

		final int index = DeveloperUtil.isDebugMode()
				? 9 % exercises.size()
				: (int) (exercises.size() * Math.random());
		return exercises.get(index).getExerciseText();
	}

	public static int toMinute(final LocalTime time) {
		return time == null ? -1 : time.getHour() * 60 + time.getMinute();
	}

	private static String getLunchTimeText() {
		return EyeExercise.LUNCH_TIME.getExerciseText();
	}
}
