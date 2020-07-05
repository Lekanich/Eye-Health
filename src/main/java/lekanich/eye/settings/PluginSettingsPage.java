package lekanich.eye.settings;

import java.util.concurrent.TimeUnit;
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
				|| !idleTextField.getText().equals(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getIdleTime())));
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
			state.setDurationBreak(Long.parseLong(durationOfRestTextField.getText()));
			state.setDurationWorkBeforeBreak(TimeUnit.MINUTES.toSeconds(Long.parseLong(durationBetweenRestTextField.getText())));
			state.setDurationPostpone(Long.parseLong(durationPostponeTextField.getText()));
			state.setIdleTime(TimeUnit.MINUTES.toSeconds(Long.parseLong(idleTextField.getText())));
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
		durationBetweenRestTextField.setText(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getDurationWorkBeforeBreak())));
		durationPostponeTextField.setText(String.valueOf(state.getDurationPostpone()));
		idleTextField.setText(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getIdleTime())));

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
