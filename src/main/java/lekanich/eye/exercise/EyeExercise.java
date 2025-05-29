package lekanich.eye.exercise;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ResourceUtil;
import com.intellij.util.io.URLUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;


/**
 * @author Lekanich
 */
@ToString
@RequiredArgsConstructor
public class EyeExercise {
	private static final Logger log = Logger.getInstance(EyeExercise.class);

	private static final String MAIN_FOLDER = "/exercises";

	@Attribute("file")
	private final String fileName;

	private volatile SoftReference<String> cache = null;

	public static List<EyeExercise> findExercises() {
		String text;
		try {
			final URL resource = ResourceUtil.getResource(EyeExercise.class.getClassLoader(), MAIN_FOLDER, "list.txt");
			text = ResourceUtil.loadText(URLUtil.openStream(resource));
		} catch (IOException e) {
			text = "";
		}
		return Stream.of(text.split("\\s")).map(EyeExercise::new).collect(Collectors.toList());
	}

	public String getExerciseText() {
		final String value = getCachedValue();
		if (value != null) {
			return value;
		}

		synchronized (this) {
			final String value2 = getCachedValue();
			if (value2 != null) {
				return value2;
			}

			final String loaded = loadExerciseText();
			final String dummyMessage = "Please report this to plugin provider";
			if (loaded == null) {
				log.warn("Exercise text for '" + fileName + "' is not found. " + dummyMessage);
				return dummyMessage;
			}

			cache = new SoftReference<>(loaded);
			return loaded;
		}
	}

	private @Nullable String getCachedValue() {
		return cache != null ? cache.get() : null;
	}

	private String loadExerciseText() {
		try {
			final StringBuilder text = new StringBuilder();
			final File file = new File(fileName);
			if (file.isAbsolute() && file.exists()) {
				text.append(FileUtil.loadFile(file));
			} else {
				final InputStream stream = ResourceUtil.getResourceAsStream(getClass().getClassLoader(), MAIN_FOLDER, fileName);
				if (stream == null) {
					return null;
				}
				text.append(ResourceUtil.loadText(stream));
			}

			return text.toString();
		} catch (IOException e) {
			log.error("Error loading exercise text from " + fileName, e);
			return null;
		}
	}
}
