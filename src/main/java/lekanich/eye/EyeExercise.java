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
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


/**
 * @author Lekanich
 */
@ToString
@RequiredArgsConstructor
public class EyeExercise {
    private static final String MAIN_FOLDER = "/exercises";
	@Attribute("file")
	public final String fileName;

	public static List<EyeExercise> findExercises() {
		String text;
		try {
			URL resource = ResourceUtil.getResource(EyeExercise.class, MAIN_FOLDER, "list.txt");
			text = ResourceUtil.loadText(resource);
		} catch (IOException e) {
			text = "";
		}
		return Stream.of(text.split("\\s"))
				.map(EyeExercise::new)
				.collect(Collectors.toList());
	}

	public String getExerciseText() {
		String dummyMessage = "Please report this to plugin provider";
		try {
			StringBuilder text = new StringBuilder();
			String cssText;
			File file = new File(fileName);
			if (file.isAbsolute() && file.exists()) {
				text.append(FileUtil.loadFile(file));
				cssText = FileUtil.loadFile(new File(file.getParentFile(), StartupUiUtil.isUnderDarcula()
						? "css/tips_darcula.css" : "css/tips.css"));
			} else {
				InputStream stream = ResourceUtil.getResourceAsStream(getClass(), MAIN_FOLDER, fileName);
				if (stream == null) {
					return dummyMessage;
				}
				text.append(ResourceUtil.loadText(stream));
				InputStream cssResourceStream = ResourceUtil.getResourceAsStream(getClass(), "/tips/", StartupUiUtil.isUnderDarcula()
						? "css/tips_darcula.css" : "css/tips.css");
				cssText = cssResourceStream != null ? ResourceUtil.loadText(cssResourceStream) : "";
			}

			String inlinedCSS = cssText + "\nbody {background-color:#" + ColorUtil.toHex(UIUtil.getTextFieldBackground()) + ";overflow:hidden;}";
			return text.toString().replaceFirst("<link.*\\.css\">", "<style type=\"text/css\">\n" + inlinedCSS + "\n</style>");
		} catch (IOException e) {
			return dummyMessage;
		}
	}

}
