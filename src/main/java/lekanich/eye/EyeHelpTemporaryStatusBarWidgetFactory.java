package lekanich.eye;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
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
	public boolean isAvailable(@NotNull Project project) {
		return true;
	}

	@Override
	public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
		return new EyeTemporaryStatusBarWidget();
	}

	@Override
	public void disposeWidget(@NotNull StatusBarWidget widget) {
		Disposer.dispose(widget);
	}

	@Override
	public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
		return true;
	}
}
