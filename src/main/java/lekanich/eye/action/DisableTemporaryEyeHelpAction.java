package lekanich.eye.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import lekanich.eye.listener.EyeHelpStatusListener;
import lekanich.eye.settings.PluginSettings;


/**
 * @author Lekanich
 */
public class DisableTemporaryEyeHelpAction extends AnAction {

	@Override
	public void actionPerformed(@NotNull final AnActionEvent e) {
		// notify about temporary disabling
		ApplicationManager.getApplication().getMessageBus()
				.syncPublisher(EyeHelpStatusListener.EYE_HELP_STATUS_TOPIC)
				.statusChanged(EyeHelpStatusListener.Status.TEMPORARY_DISABLED);
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

	@Override
	public void update(@NotNull final AnActionEvent e) {
		super.update(e);
		e.getPresentation().setEnabled(!PluginSettings.TemporaryDisableEyeHelpSetting.isTemporaryDisabled());
	}
}
