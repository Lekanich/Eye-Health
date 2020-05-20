package lekanich.eye;

import java.util.concurrent.TimeUnit;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionNotApplicableException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;
import lombok.SneakyThrows;
import lekanich.eye.listener.EyeHelpListener;
import lekanich.eye.listener.EyeHelpSingleton;
import lekanich.eye.ui.EyeHelpDialog;


/**
 * @author Lekanich
 */
public class PluginStartupActivity implements StartupActivity, StartupActivity.Background {

	public PluginStartupActivity() {
		if (ApplicationManager.getApplication().isUnitTestMode()) {
			throw ExtensionNotApplicableException.INSTANCE;
		}

		// register topic and subscribe listener to do payload
		ApplicationManager.getApplication().getMessageBus()
				.connect()
				.subscribe(EyeHelpListener.EYE_HELP_TOPIC, EyeHelpSingleton.getInstance());
	}

	@SneakyThrows
	@Override
	public void runActivity(@NotNull Project project) {
		EyeHelpDialog.publishNextRestEvent();
	}
}
