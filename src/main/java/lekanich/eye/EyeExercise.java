package lekanich.eye;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.ColorUtil;
import com.intellij.util.ResourceUtil;
import com.intellij.util.io.URLUtil;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;


/**
 * @author Lekanich
 */
public record EyeExercise(@Attribute("file") String fileName) {
	private static final String MAIN_FOLDER = "/exercises";

	public static final EyeExercise LUNCH_TIME = new EyeExercise("lunch.html");

	public static List<EyeExercise> findExercises() {
		String text;
		try {
			final URL resource = ResourceUtil.getResource(EyeExercise.class.getClassLoader(), MAIN_FOLDER, "list.txt");
			text = ResourceUtil.loadText(URLUtil.openStream(resource));
		} catch (IOException e) {
			text = "";
		}
		return Stream.of(text.split("\\s"))
				.map(EyeExercise::new)
				.collect(Collectors.toList());
	}

	public String getExerciseText() {
		final String dummyMessage = "Please report this to plugin provider";
		try {
			final StringBuilder text = new StringBuilder();
			String cssText;
			final File file = new File(fileName);
			if (file.isAbsolute() && file.exists()) {
				text.append(FileUtil.loadFile(file));
				final URL cssResource = cssResource();
				cssText = ResourceUtil.loadText(URLUtil.openStream(cssResource));
			} else {
				final InputStream stream = ResourceUtil.getResourceAsStream(getClass().getClassLoader(), MAIN_FOLDER, fileName);
				if (stream == null) {
					return dummyMessage;
				}
				text.append(ResourceUtil.loadText(stream));
				final String cssFileName = isDark() ? "css/tips_darcula.css" : "css/tips.css";
				final InputStream cssResourceStream = ResourceUtil.getResourceAsStream(getClass().getClassLoader(), "/tips/", cssFileName);
				cssText = cssResourceStream != null ? ResourceUtil.loadText(cssResourceStream) : "";
			}

			final String inlinedCSS = cssText + "\nbody {background-color:#" + ColorUtil.toHex(UIUtil.getTextFieldBackground()) + ";overflow:hidden;}";
			return text.toString().replaceFirst("<link.*\\.css\">", "<style type=\"text/css\">\n" + inlinedCSS + "\n</style>");
		} catch (IOException e) {
			return dummyMessage;
		}
	}

	public static URL cssResource() {
		final String cssFileName = isDark() ? "exercise_darcula.css" : "exercise.css";
		return ResourceUtil.getResource(EyeExercise.class.getClassLoader(), "/exercises/css/", cssFileName);
	}

	@NotNull
	public static String cssResourceFileName() {
		return "exercises/css/" + (isDark() ? "exercise_darcula.css" : "exercise.css");
	}

	private static boolean isDark() {
		return StartupUiUtil.INSTANCE.isDarkTheme();
	}
}
