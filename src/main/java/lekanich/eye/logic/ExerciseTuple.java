package lekanich.eye.logic;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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
		final LocalTime lunchTime = Optional.ofNullable(PluginSettings.getInstance())
				.map(PluginSettings::getState)
				.map(PluginSettings.PluginAppState::getLunchTime)
				.orElse(null);
		final int delta = toMinute(LocalTime.now()) - toMinute(lunchTime);
		final boolean isLunch = delta > 0 && delta < 60;
		final String message = isLunch
				? getLunchTimeText()
				: findExerciseMessage();

		long durationBreak = Optional.ofNullable(PluginSettings.getInstance())
				.map(PluginSettings::getState)
				.map(PluginSettings.PluginAppState::getDurationBreak)
				.orElse(0L);
		durationBreak *= isLunch ? 2 : 1;

		return new ExerciseTuple(message, durationBreak);
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
