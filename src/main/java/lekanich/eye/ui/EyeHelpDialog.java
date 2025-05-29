package lekanich.eye.ui;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import java.awt.Window;
import java.util.Optional;
import java.util.stream.Stream;
import lekanich.eye.EyeBundle;
import lekanich.eye.listener.EyeHelpListener;
import lekanich.eye.listener.EyeHelpSingleton;
import lekanich.eye.listener.EyeHelpStatusListener;
import lekanich.eye.settings.PluginSettings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author Lekanich
 */
public class EyeHelpDialog extends DialogWrapper {
    private static final Logger log = Logger.getInstance(EyeHelpDialog.class);
    private static final String LAST_TIME_EYE_HELP_SHOWN = "lastTimeEyeHelpWereShown";
    private static EyeHelpDialog ourInstance;
    private final EyeHelpPanel centralPanel;
    private final boolean show;

    public EyeHelpDialog(@NotNull final Window parent) {
        super(parent, true);
        setModal(false);
        setTitle(EyeBundle.message("eye.dialog.title"));
        this.show = parent.isActive() || PluginSettings.isActiveWhenMinimized();
        log.info("Window '" + parent.getName() + "' is " + this.show);
        this.centralPanel = new EyeHelpPanel(this, EyeHelpSingleton.getInstance().getExercises());
        Optional.ofNullable(PluginSettings.getInstance())
                .ifPresent(it -> Disposer.register(it, centralPanel));
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
        // se widget to active since we back to show "exercise message"
        ApplicationManager.getApplication().getMessageBus()
                .syncPublisher(EyeHelpStatusListener.EYE_HELP_STATUS_TOPIC)
                .statusChanged(EyeHelpStatusListener.Status.ACTIVE);

        PropertiesComponent.getInstance()
                .setValue(LAST_TIME_EYE_HELP_SHOWN, String.valueOf(System.currentTimeMillis()));

        if (show) {
            super.show();
        }
    }

    @NonNls
    private static Optional<Project> getActiveProject() {
        @NotNull final Project[] projects = ProjectManager.getInstance().getOpenProjects();
        return Stream.of(projects)
                .filter(project -> Optional.ofNullable(
                        WindowManager.getInstance().getFrame(project))
                        .map(JFrame::isActive)
                        .orElse(false)
                )
                .findAny();
    }

    public static void showForProject() {
        final Window w = getActiveProject()
                .map(project -> WindowManagerEx.getInstanceEx().suggestParentWindow(project))
                .orElseGet(() -> Optional.ofNullable(WindowManagerEx.getInstanceEx().getMostRecentFocusedWindow())
                        .orElseGet(() -> WindowManagerEx.getInstanceEx().findVisibleFrame())
                );
        if (ourInstance != null && ourInstance.isVisible()) {
            ourInstance.dispose();
        }

        if (w == null) {
            return;
        }

        if (PluginSettings.isDisabled()) {
            return;
        }

        ourInstance = new EyeHelpDialog(w);
        ourInstance.show();
    }

    public static void hideForProject() {
        if (ourInstance != null) {
            ourInstance.dispose();
            ourInstance = null;
        }
    }

    public static void publishNextRestEvent() {
        publishNextRestEventWithDelay(0);
    }

    public static void publishNextRestEventWithDelay(final long delayInSeconds) {
        // if disabled start at application start
        final PluginSettings instance = PluginSettings.getInstance();
        if (instance == null) {
            log.warn("Cannot get state component, Thread: " + Thread.currentThread());
            return;
        }

        final PluginSettings.PluginAppState state = instance.getState();
        // check if it is disabled
        if (!state.isEnable()) {
            return;
        }

        ApplicationManager.getApplication().getMessageBus()
                .syncPublisher(EyeHelpListener.EYE_HELP_TOPIC)
                .scheduleEyeHelp(state.getDurationWorkBeforeBreak() + delayInSeconds);
    }

    @Override
    protected void dispose() {
        super.dispose();
    }
}
