package lekanich.eye;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;


/**
 * @author Lekanich
 */
public class EyeHelpTemporaryStatusBarWidgetFactory implements StatusBarWidgetFactory {

	@Override
	public @NotNull String getId() {
		return EyeTemporaryStatusBarWidget.WIDGET_ID;
	}

	@Override
	public @Nls @NotNull String getDisplayName() {
		return EyeBundle.message("eye.widget.name");
	}

	@Override
	public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
		return new EyeTemporaryStatusBarWidget();
	}
}
