package lekanich.eye.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import lekanich.eye.listener.EyeHelpStatusListener;
import lekanich.eye.settings.PluginSettings;
import lekanich.eye.util.EyeHelpDialog;


/**
 * @author Lekanich
 */
public class EyeHelpAction extends AnAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		// automatic enable functionality
		PluginSettings.getInstance()
				.getState()
				.setEnable(true);

		EyeHelpDialog.publishNextRestEvent();

		// notify about temporary disabling
		ApplicationManager.getApplication().getMessageBus()
				.syncPublisher(EyeHelpStatusListener.EYE_HELP_STATUS_TOPIC)
				.statusChanged(EyeHelpStatusListener.Status.ACTIVE);
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);

		e.getPresentation().setEnabled(PluginSettings.isDisabled());
	}
}
