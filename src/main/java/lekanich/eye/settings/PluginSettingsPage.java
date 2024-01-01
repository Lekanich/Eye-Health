package lekanich.eye.settings;

import java.awt.Component;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
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
import lekanich.eye.logic.ExerciseTuple;
import lekanich.eye.ui.EyeHelpDialog;
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
	private JCheckBox enableLunchTime;
	private JComboBox<LocalTime> timeComboBox;
	private JLabel statusLunchtime;

	public PluginSettingsPage() {
		this.settings = PluginSettings.getInstance();
	}

	@Override
	public @NotNull String getId() {
		return getDisplayName();
	}

	@Override
	public @Nls(capitalization = Nls.Capitalization.Title)
	final String getDisplayName() {
		return PAGE_NAME;
	}

	@Override
	public @Nullable JComponent createComponent() {
		// status bar icons
		iconComboBox.removeAllItems();
		Stream.of(EyeHelpIcons.EyeType.values())
				.forEach(iconComboBox::addItem);
		iconComboBox.setRenderer(new IconTextDecorator(iconComboBox.getRenderer()));
		// every 30 minutes, from 11:00
		final int hourOffset = 60 * 11;
		IntStream.range(0, 9)
				.map(i -> i * 30 + hourOffset)
				.forEach(i -> timeComboBox.addItem(LocalTime.of(i / 60, i % 60)));

		reset();

		durationPostponeTextField.setInputVerifier(POSITIVE_INTEGER_VERIFIER);
		durationBetweenRestTextField.setInputVerifier(POSITIVE_INTEGER_VERIFIER);
		durationOfRestTextField.setInputVerifier(POSITIVE_INTEGER_VERIFIER);
		idleTextField.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(final JComponent input) {
				return POSITIVE_INTEGER_VERIFIER.verify(input)
						&& Long.parseLong(durationBetweenRestTextField.getText()) > Long.parseLong(((JTextField) input).getText());
			}
		});

		return mainPanel;
	}

	private void updateStatusLabel(final JLabel label, final boolean enable) {
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
		final PluginSettings.PluginAppState state = settings.getState();

		return enablePluginCheckBox.isSelected() != state.isEnable()
				|| allowPostponeTheEyeCheckBox.isSelected() != state.isPostpone()
				|| !durationOfRestTextField.getText().equals(String.valueOf(state.getDurationBreak()))
				|| !durationBetweenRestTextField.getText().equals(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getDurationWorkBeforeBreak())))
				|| !durationPostponeTextField.getText().equals(String.valueOf(state.getDurationPostpone()))
				|| !idleTextField.getText().equals(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getIdleTime())))
				|| iconComboBox.getSelectedItem() != state.getEyeType()
				|| showMinimizedCheckBox.isSelected() != state.isShowWhenMinimized()
				|| enableLunchTime.isSelected() != state.isEnableLunchTime()
				|| ExerciseTuple.toMinute((LocalTime) timeComboBox.getSelectedItem()) != state.getLunchTimeInMinutes();
	}

	@Override
	public void apply() throws ConfigurationException {
		final PluginSettings.PluginAppState state = settings.getState();

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

		// statuses
		state.setEnable(enablePluginCheckBox.isSelected());
		state.setShowWhenMinimized(showMinimizedCheckBox.isSelected());
		state.setPostpone(allowPostponeTheEyeCheckBox.isSelected());
		state.setEnableLunchTime(enableLunchTime.isSelected());

		try {
			state.setDurationBreak(Long.parseLong(durationOfRestTextField.getText()));
			state.setDurationWorkBeforeBreak(TimeUnit.MINUTES.toSeconds(Long.parseLong(durationBetweenRestTextField.getText())));
			state.setDurationPostpone(Long.parseLong(durationPostponeTextField.getText()));
			state.setIdleTime(TimeUnit.MINUTES.toSeconds(Long.parseLong(idleTextField.getText())));
		} catch (NumberFormatException e) {
			throw new ConfigurationException("Cannot apply that values", e, EyeBundle.message("eye.settings.config.error.duration"));
		}

		Object lunchTime = timeComboBox.getSelectedItem();
		if (lunchTime instanceof LocalTime) {
			state.setLunchTimeInMinutes(ExerciseTuple.toMinute((LocalTime) lunchTime));
		}

		changeStatus(state);
	}

	private void notifyAboutTurnOn() {
		EyeHelpDialog.publishNextRestEvent();
	}

	@Override
	public void reset() {
		// init from config
		final PluginSettings.PluginAppState state = settings.getState();

		iconComboBox.setSelectedItem(state.getEyeType());
		enablePluginCheckBox.setSelected(state.isEnable());
		showMinimizedCheckBox.setSelected(state.isShowWhenMinimized());
		allowPostponeTheEyeCheckBox.setSelected(state.isPostpone());
		durationOfRestTextField.setText(String.valueOf(state.getDurationBreak()));
		durationBetweenRestTextField.setText(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getDurationWorkBeforeBreak())));
		durationPostponeTextField.setText(String.valueOf(state.getDurationPostpone()));
		idleTextField.setText(String.valueOf(TimeUnit.SECONDS.toMinutes(state.getIdleTime())));
		// Lunchtime settings
		enableLunchTime.setSelected(state.isEnableLunchTime());
		Optional.ofNullable(state.getLunchTime())
				.ifPresent(it -> timeComboBox.setSelectedItem(it));

		changeStatus(state);
	}

	private void changeStatus(final PluginSettings.PluginAppState state) {
		updateStatusLabel(statusPlugin, state.isEnable());
		updateStatusLabel(statusLabelPostpone, state.isPostpone());
		updateStatusLabel(statusLabelMinimized, state.isShowWhenMinimized());
		updateStatusLabel(statusLunchtime, state.isEnableLunchTime());
	}

	private static class IntegerNumberVerifier extends InputVerifier {

		@Override
		public boolean verify(final JComponent input) {
			return StringUtil.isNotNegativeNumber(((JTextField) input).getText());
		}
	}

	private record IconTextDecorator(ListCellRenderer<? super EyeType> delegate) implements ListCellRenderer<EyeType> {
		@Override
		public Component getListCellRendererComponent(
				final JList<? extends EyeType> list,
				final EyeType value,
				final int index,
				final boolean isSelected,
				final boolean cellHasFocus) {
			final Component component = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (component instanceof JLabel) {
				((JLabel) component).setIcon(value.getIcon());
				((JLabel) component).setText(value.toString().toLowerCase());
			}
			return component;
		}
	}
}
