package lekanich.eye.exercise;

import java.util.List;
import lekanich.DeveloperUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Lekanich
 */
@Getter
@RequiredArgsConstructor
public class ExerciseKeeper {
	private final List<EyeExercise> exercises;
	private final EyeExercise lunchExercise = new EyeExercise("lunch.html");

	public boolean isEmpty() {
		return exercises.isEmpty();
	}

	public EyeExercise getRandom() {
		final int index = DeveloperUtil.isDebugMode()
				? 9 % exercises.size()
				: (int) (Math.random() * exercises.size());
		return exercises.get(index);
	}
}
