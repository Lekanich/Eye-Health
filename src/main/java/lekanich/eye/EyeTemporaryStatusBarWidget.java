package lekanich.eye;

import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.Icon;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.ui.UIBundle;
import com.intellij.util.Consumer;
import com.intellij.util.concurrency.EdtExecutorService;
import icons.EyeHelpIcons;
import icons.EyeHelpIcons.EyeType;
import lekanich.eye.listener.EyeHelpStatusListener;
import lekanich.eye.settings.PluginSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author Lekanich
 */
public class EyeTemporaryStatusBarWidget implements StatusBarWidget, StatusBarWidget.Multiframe, StatusBarWidget.IconPresentation, EyeHelpStatusListener {
	public static final String WIDGET_ID = "EyeHelpTemporaryDisable";
	private StatusBar statusBar;

	@Override
	public @NotNull String ID() {
		return WIDGET_ID;
	}

	@Override
	public @Nullable WidgetPresentation getPresentation() {
		return this;
	}

	@Override
	public void install(@NotNull StatusBar statusBar) {
		this.statusBar = statusBar;
		statusBar.updateWidget(ID());

		Disposer.register(statusBar, this);

		ApplicationManager.getApplication().getMessageBus()
				.connect()
				.subscribe(EyeHelpStatusListener.EYE_HELP_STATUS_TOPIC, this);
	}

	@Override
	public void statusChanged(Status status) {
		if (status == Status.ACTIVE) {
			PluginSettings.TemporaryDisableEyeHelpSetting.reactivate();
		} else if (status == Status.TEMPORARY_DISABLED) {
			PluginSettings.TemporaryDisableEyeHelpSetting.deactivateEyeHelp();
		}
		// ICON_CHANGE (just refresh)
		update();
	}

	private void update() {
		EdtExecutorService.getInstance()
				.execute(() -> Optional.ofNullable(statusBar).ifPresent(bar -> bar.updateWidget(ID())));
	}

	@Override
	public void dispose() {
		statusBar = null;
	}

	@Override
	public StatusBarWidget copy() {
		return new EyeTemporaryStatusBarWidget();
	}

	@Nullable
	private Project getProject() {
		return statusBar != null ? statusBar.getProject() : null;
	}

	@Override
	public @Nullable Consumer<MouseEvent> getClickConsumer() {
		return mouseEvent -> {
			try {
				// notify about temporary disabling
				EyeHelpStatusListener publisher = ApplicationManager.getApplication().getMessageBus()
						.syncPublisher(EyeHelpStatusListener.EYE_HELP_STATUS_TOPIC);
				if (PluginSettings.TemporaryDisableEyeHelpSetting.isTemporaryDisabled()) {
					publisher.statusChanged(Status.ACTIVE);
				} else {
					publisher.statusChanged(Status.TEMPORARY_DISABLED);
				}
			} catch (Exception e) {
				Messages.showMessageDialog(getProject(), e.getMessage(), UIBundle.message("error.dialog.title"), Messages.getErrorIcon());
			}
		};
	}

	@Override
	public @Nullable @Nls(capitalization = Nls.Capitalization.Sentence) String getTooltipText() {
		return PluginSettings.TemporaryDisableEyeHelpSetting.isTemporaryDisabled()
				? EyeBundle.message("eye.widget.temporary.enable")
				: EyeBundle.message("eye.widget.temporary.disable");
	}

	@Override
	public @NotNull Icon getIcon() {
		return PluginSettings.TemporaryDisableEyeHelpSetting.isTemporaryDisabled()
				? EyeHelpIcons.EYE_OFF
				: Optional.ofNullable(PluginSettings.getInstance())
					.map(PluginSettings::getState)
					.map(PluginSettings.PluginAppState::getEyeType)
					.map(EyeType::getIcon)
					.orElse(EyeHelpIcons.EYE_ON);
	}
}
