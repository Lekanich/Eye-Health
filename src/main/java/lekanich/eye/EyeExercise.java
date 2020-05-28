package lekanich.eye;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.ColorUtil;
import com.intellij.util.ResourceUtil;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import lombok.ToString;


/**
 * @author Lekanich
 */
@ToString
public class EyeExercise extends AbstractExtensionPointBean {
	public static final ExtensionPointName<EyeExercise> EP_NAME = ExtensionPointName.create("lekanich.eye-health.exercises");

	@Attribute("file")
	public String fileName;

	public static List<EyeExercise> findExercises() {
		return EP_NAME.getExtensionList();
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
				PluginDescriptor pluginDescriptor = getPluginDescriptor();
				ClassLoader loader = Optional.ofNullable(pluginDescriptor)
						.map(PluginDescriptor::getPluginClassLoader)
						.orElse(getClass().getClassLoader());

				InputStream stream = ResourceUtil.getResourceAsStream(loader, "exercises", fileName);
				if (stream == null) {
					return dummyMessage;
				}
				text.append(ResourceUtil.loadText(stream));
				InputStream cssResourceStream = ResourceUtil.getResourceAsStream(loader, "/tips/", StartupUiUtil.isUnderDarcula()
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
