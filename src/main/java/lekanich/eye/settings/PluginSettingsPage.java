package lekanich.eye.settings;

import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import lombok.RequiredArgsConstructor;
import lekanich.eye.EyeBundle;
import lekanich.eye.ui.EyeHelpDialog;


/**
 * @author Lekanich
 */
@RequiredArgsConstructor
public class PluginSettingsPage implements SearchableConfigurable {
	private static final String NAME = "Eye Help";
	private final PluginSettings settings;
	private JTextField durationBetweenRestTextField;
	private JCheckBox enablePluginCheckBox;
	private JPanel mainPanel;
	private JCheckBox allowPostponeTheEyeCheckBox;
	private JLabel statusLabelPostpone;
	private JLabel statusPlugin;
	private JTextField durationPostponeTextField;
	private JTextField durationOfRestTextField;

	@Override
	public @NotNull String getId() {
		return getDisplayName();
	}

	@Override
	public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
		return NAME;
	}

	@Override
	public @Nullable JComponent createComponent() {
		reset();

		durationPostponeTextField.setInputVerifier(new IntegerNumberVerifier());
		durationBetweenRestTextField.setInputVerifier(new IntegerNumberVerifier());
		durationOfRestTextField.setInputVerifier(new IntegerNumberVerifier());

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
				|| !durationBetweenRestTextField.getText().equals(String.valueOf(state.getDurationWorkBeforeBreak()))
				|| !durationPostponeTextField.getText().equals(String.valueOf(state.getDurationPostpone()));
	}

	@Override
	public void apply() throws ConfigurationException {
		PluginSettings.PluginAppState state = settings.getState();

		if (state.isEnable() != enablePluginCheckBox.isSelected()) {
			notifyAboutTurnOn();
		}

		state.setEnable(enablePluginCheckBox.isSelected());
		state.setPostpone(allowPostponeTheEyeCheckBox.isSelected());
		try {
			state.setDurationBreak(Integer.parseInt(durationOfRestTextField.getText()));
			state.setDurationWorkBeforeBreak(Integer.parseInt(durationBetweenRestTextField.getText()));
			state.setDurationPostpone(Integer.parseInt(durationPostponeTextField.getText()));
		} catch (NumberFormatException e) {
			throw new ConfigurationException("Cannot apply that values", e, "need to check durations");
		}

		updateStatusLabel(statusPlugin, state.isEnable());
		updateStatusLabel(statusLabelPostpone, state.isPostpone());
	}

	private void notifyAboutTurnOn() {
		EyeHelpDialog.publishNextRestEvent();
	}

	@Override
	public void reset() {
		// init from config
		PluginSettings.PluginAppState state = settings.getState();

		enablePluginCheckBox.setSelected(state.isEnable());
		allowPostponeTheEyeCheckBox.setSelected(state.isPostpone());
		durationOfRestTextField.setText(String.valueOf(state.getDurationBreak()));
		durationBetweenRestTextField.setText(String.valueOf(state.getDurationWorkBeforeBreak()));
		durationPostponeTextField.setText(String.valueOf(state.getDurationPostpone()));

		updateStatusLabel(statusPlugin, state.isEnable());
		updateStatusLabel(statusLabelPostpone, state.isPostpone());
	}

	private static class IntegerNumberVerifier extends InputVerifier {

		@Override
		public boolean verify(JComponent input) {
			return StringUtil.isNotNegativeNumber(((JTextField) input).getText());
		}
	}
}
