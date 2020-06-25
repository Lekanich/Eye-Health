package lekanich.eye.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import lekanich.eye.settings.PluginSettings;


/**
 * @author Lekanich
 */
public class DisableTemporaryEyeHelpAction extends AnAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		PluginSettings.TemporaryDisableEyeHelpSetting.disableTemporaryEyeHelp();
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		e.getPresentation().setEnabled(!PluginSettings.TemporaryDisableEyeHelpSetting.isTemporaryDisabled());
	}
}
