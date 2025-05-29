package lekanich.eye.logic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lekanich.eye.EyeBundle;
import lekanich.eye.exercise.ExerciseKeeper;
import lekanich.eye.exercise.EyeExercise;
import lekanich.eye.settings.PluginSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lekanich
 */
public record ExerciseTuple(String exercise, long durationBreak) {
	public static ExerciseTuple findExercise(final ExerciseKeeper exercises) {
		final PluginSettings.PluginAppState state = PluginSettings.getInstance().getState();

		final int delta = toMinute(LocalTime.now()) - toMinute(state.getLunchTime());

		boolean isLunchExercise = showLunch(delta > 0 && delta < 60, state);
		final String message = isLunchExercise
				? exercises.getLunchExercise().getExerciseText()
				: findExerciseMessage(exercises);

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
	private static String findExerciseMessage(final ExerciseKeeper exercises) {
		if (exercises.isEmpty()) {
			return EyeBundle.message("eye.dialog.exercises.dummy");
		}

		final EyeExercise random = exercises.getRandom();
		return random.getExerciseText();
	}

	public static int toMinute(final LocalTime time) {
		return time == null ? -1 : time.getHour() * 60 + time.getMinute();
	}
}
