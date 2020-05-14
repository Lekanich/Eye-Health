package lekanich.eye.util;

import java.awt.Window;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import lekanich.eye.EyeBundle;
import lekanich.eye.listener.EyeHelpListener;
import lekanich.eye.settings.ApplicationSettings;


/**
 * @author Lekanich
 */
public class EyeHelpDialog extends DialogWrapper {
	private static final Logger log = Logger.getInstance(EyeHelpDialog.class);
	private static final String LAST_TIME_EYE_HELP_SHOWN = "lastTimeEyeHelpWereShown";
	private static EyeHelpDialog ourInstance;
	private final EyeHelpPanel centralPanel;

	public EyeHelpDialog(@NotNull Window parent) {
		super(parent, true);
		setModal(false);
		setTitle(EyeBundle.message("eye.dialog.title"));
		this.centralPanel = new EyeHelpPanel(this);
		setCancelButtonText(EyeBundle.message("eye.dialog.later.button.txt"));
		init();
	}

	@Override
	protected Action @NotNull [] createActions() {
		return new Action[]{getCancelAction()};
	}

	@Override
	public void doCancelAction() {
		publishNextRestEvent();

		close(CANCEL_EXIT_CODE);
	}

	@NotNull
	@Override
	protected DialogStyle getStyle() {
		return DialogStyle.COMPACT;
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return this.centralPanel;
	}

	@Override
	public void show() {
		PropertiesComponent.getInstance()
				.setValue(LAST_TIME_EYE_HELP_SHOWN, String.valueOf(System.currentTimeMillis()));
		super.show();
	}

	@Nullable
	private static Project getActiveProject() {
		@NotNull Project[] projects = ProjectManager.getInstance().getOpenProjects();
		log.warn("Has opened " + projects.length + " projects.");
		return Stream.of(projects)
				.filter(project -> Optional.ofNullable(
						WindowManager.getInstance().getFrame(project))
						.map(JFrame::isActive)
						.orElse(false)
				)
				.peek(project -> log.warn(Objects.toString(project)))
				.findAny()
				.orElse(null);
	}

	public static void showForProject() {
		Project project = getActiveProject();
		Window w = WindowManagerEx.getInstanceEx().suggestParentWindow(project);
		if (w == null) w = WindowManagerEx.getInstanceEx().findVisibleFrame();
		if (ourInstance != null && ourInstance.isVisible()) {
			ourInstance.dispose();
		}

		ourInstance = new EyeHelpDialog(w);
		ourInstance.show();
	}

	public static void publishNextRestEvent() {
		// if disabled start at application start
		log.warn("try read state component: Thread: " + Thread.currentThread().toString());
		ApplicationSettings instance = ApplicationSettings.getInstance();
		if (instance == null) {
			log.warn("Cannot get state component, Thread");
			return;
		}
		ApplicationSettings.EyeHelpState state = instance.getState();
		boolean startNow = state.isEnable();
		if (!startNow) {
			return;
		}

		ApplicationManager.getApplication().getMessageBus()
				.syncPublisher(EyeHelpListener.EYE_HELP_TOPIC)
				.scheduleEyeHelp(TimeUnit.MINUTES.toSeconds(state.getWorkingTimeBetweenShortBreaksMin()));
	}

	@Override
	protected void dispose() {
		super.dispose();
	}
}
