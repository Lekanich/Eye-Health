package lekanich.eye;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionNotApplicableException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.util.messages.MessageBus;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import lekanich.eye.listener.EyeHelpListener;
import lekanich.eye.listener.EyeHelpSingleton;
import lekanich.eye.ui.EyeHelpDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author Lekanich
 */
public class PluginStartupActivity implements ProjectActivity {

	public PluginStartupActivity() {
		if (ApplicationManager.getApplication().isUnitTestMode()) {
			throw ExtensionNotApplicableException.create();
		}

		// register topic and subscribe listener to do payload
		MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
		messageBus.connect()
				.subscribe(EyeHelpListener.EYE_HELP_TOPIC, EyeHelpSingleton.getInstance());
	}

	@Nullable
	@Override
	public Object execute(@NotNull final Project project, @NotNull final Continuation<? super Unit> continuation) {
		EyeHelpDialog.publishNextRestEvent();
		return null;
	}
}
