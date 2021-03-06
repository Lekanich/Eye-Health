package lekanich.eye.settings;

import java.awt.Component;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.swing.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import icons.EyeHelpIcons;
import icons.EyeHelpIcons.EyeType;
import lekanich.eye.EyeBundle;
import lekanich.eye.listener.EyeHelpStatusListener;
import lekanich.eye.ui.EyeHelpDialog;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author Lekanich
 */
public class PluginSettingsPage implements SearchableConfigurable {
	private static final String PAGE_NAME = "Eye Help";
	private static final IntegerNumberVerifier POSITIVE_INTEGER_VERIFIER = new IntegerNumberVerifier();
	private final PluginSettings settings;
	private JTextField durationBetweenRestTextField;
	private JCheckBox enablePluginCheckBox;
	private JPanel mainPanel;
	private JCheckBox allowPostponeTheEyeCheckBox;
	private JLabel statusLabelPostpone;
	private JLabel statusPlugin;
	private JTextField durationPostponeTextField;
	private JTextField durationOfRestTextField;
	private JTextField idleTextField;
	private JComboBox<EyeType> iconComboBox;
	private JCheckBox showMinimizedCheckBox;
	private JLabel statusLabelMinimized;

	public PluginSettingsPage() {
		this.settings = PluginSettings.getInstance();
	}

	@Override
	public @NotNull String getId() {
		return getDisplayName();
	}

	@Override
	public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
		return PAGE_NAME;
	}

	@Override
	public @Nullable JComponent createComponent() {
		// status bar icons
		iconComboBox.removeAllItems();
		Stream.of(EyeHelpIcons.EyeType.values())
				.forEach(iconComboBox::addItem);
		iconComboBox.setRenderer(new IconTextDecorator(iconComboBox.getRenderer()));

		reset();

		durationPostponeTextField.setInputVerifier(POSITIVE_INTEGER_VERIFIER);
		durationBetweenRestTextField.setInputVerifier(POSITIVE_INTEGER_VERIFIER);
		durationOfRestTextField.setInputVerifier(POSITIVE_INTEGER_VERIFIER);
		idleTextField.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				return POSITIVE_INTEGER_VERIFIER.verify(input)
						&& Long.parseLong(durationBetweenRestTextField.getText()) > Long.parseLong(((JTextField) input).getText());
			}
		});

		return mainPanel;
	}

	private void updateStatusLabel(JLabel label, boolean enable) {
		if (enable) {
			label.setText(EyeBundle.message("eye.settings.status.enabled"));
			label.setForeground(JBColor.GREEN);
		} else {
			label.setText(EyeBundle.message("eye.settings.status.disabled"));
			label.setForeground(JBColor.RED);
		}
	}

	@Override
	public boolean isModified() {
		PluginSettings.PluginAppState state = settings.getState();

		return enablePluginCheckBox.isSelected() != state.isEnable()
				|| allowPostponeTheEyeCheckBox.isSelected() != state.isPostpone()
				|| !durationOfRestTextField.getText().equals(String.valueOf(state.getDurationBreak()))
				|| !durationBetweenRestTextField.getText().equals(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getDurationWorkBeforeBreak())))
				|| !durationPostponeTextField.getText().equals(String.valueOf(state.getDurationPostpone()))
				|| !idleTextField.getText().equals(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getIdleTime())))
				|| iconComboBox.getSelectedItem() != state.getEyeType()
				|| showMinimizedCheckBox.isSelected() != state.isShowWhenMinimized();
	}

	@Override
	public void apply() throws ConfigurationException {
		PluginSettings.PluginAppState state = settings.getState();

		if (state.isEnable() != enablePluginCheckBox.isSelected()) {
			notifyAboutTurnOn();
		}

		if (state.getEyeType() != iconComboBox.getSelectedItem()) {
			// notify about widget icon change
			ApplicationManager.getApplication().getMessageBus()
					.syncPublisher(EyeHelpStatusListener.EYE_HELP_STATUS_TOPIC)
					.statusChanged(EyeHelpStatusListener.Status.ICON_CHANGE);
		}

		state.setEyeType((EyeType) iconComboBox.getSelectedItem());
		state.setEnable(enablePluginCheckBox.isSelected());
		state.setShowWhenMinimized(showMinimizedCheckBox.isSelected());
		state.setPostpone(allowPostponeTheEyeCheckBox.isSelected());
		try {
			state.setDurationBreak(Long.parseLong(durationOfRestTextField.getText()));
			state.setDurationWorkBeforeBreak(TimeUnit.MINUTES.toSeconds(Long.parseLong(durationBetweenRestTextField.getText())));
			state.setDurationPostpone(Long.parseLong(durationPostponeTextField.getText()));
			state.setIdleTime(TimeUnit.MINUTES.toSeconds(Long.parseLong(idleTextField.getText())));
		} catch (NumberFormatException e) {
			throw new ConfigurationException("Cannot apply that values", e, EyeBundle.message("eye.settings.config.error.duration"));
		}

		updateStatusLabel(statusPlugin, state.isEnable());
		updateStatusLabel(statusLabelPostpone, state.isPostpone());
		updateStatusLabel(statusLabelMinimized, state.isShowWhenMinimized());
	}

	private void notifyAboutTurnOn() {
		EyeHelpDialog.publishNextRestEvent();
	}

	@Override
	public void reset() {
		// init from config
		PluginSettings.PluginAppState state = settings.getState();

		iconComboBox.setSelectedItem(state.getEyeType());
		enablePluginCheckBox.setSelected(state.isEnable());
		showMinimizedCheckBox.setSelected(state.isShowWhenMinimized());
		allowPostponeTheEyeCheckBox.setSelected(state.isPostpone());
		durationOfRestTextField.setText(String.valueOf(state.getDurationBreak()));
		durationBetweenRestTextField.setText(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getDurationWorkBeforeBreak())));
		durationPostponeTextField.setText(String.valueOf(state.getDurationPostpone()));
		idleTextField.setText(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getIdleTime())));

		updateStatusLabel(statusPlugin, state.isEnable());
		updateStatusLabel(statusLabelPostpone, state.isPostpone());
		updateStatusLabel(statusLabelMinimized, state.isShowWhenMinimized());
	}

	private static class IntegerNumberVerifier extends InputVerifier {

		@Override
		public boolean verify(JComponent input) {
			return StringUtil.isNotNegativeNumber(((JTextField) input).getText());
		}
	}

	@RequiredArgsConstructor
	private static class IconTextDecorator implements ListCellRenderer<EyeHelpIcons.EyeType> {
		private final ListCellRenderer<? super EyeHelpIcons.EyeType> delegate;

		@Override
		public Component getListCellRendererComponent(final JList<? extends EyeType> list, final EyeType value, final int index, final boolean isSelected, final boolean cellHasFocus) {
			Component component = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (component instanceof JLabel) {
				((JLabel) component).setIcon(value.getIcon());
				((JLabel) component).setText(value.toString().toLowerCase());
			}
			return component;
		}
	}
}
