package lekanich.eye.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import com.intellij.ide.util.TipUIUtil;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.concurrency.EdtExecutorService;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import lombok.SneakyThrows;
import lekanich.DeveloperUtil;
import lekanich.eye.EyeBundle;
import lekanich.eye.EyeExercise;
import lekanich.eye.settings.PluginSettings;
import static java.beans.EventHandler.create;


/**
 * @author Lekanich
 */
public class EyeHelpPanel extends JBPanel<EyeHelpPanel> {
	private static final JBColor DIVIDER_COLOR = new JBColor(0xd9d9d9, 0x515151);
	private static final int DEFAULT_WIDTH = 400;
	private static final int DEFAULT_HEIGHT = 200;
	private final EyeHelpDialog parent;
	private final JClockPanel clockPanel;

	private static final class JClockPanel extends JBPanel<JClockPanel> {
		private final JBLabel counterLabel;
		private final RoundedLineBorder roundedBorder;
		private final double dColorBalance;
		private int secondsToRest;

		public JClockPanel(int secondsToRest) {
			this.secondsToRest = secondsToRest;

			setLayout(new BorderLayout());

			this.dColorBalance = 1.0 / secondsToRest;
			this.roundedBorder = IdeBorderFactory.createRoundedBorder(60, 3);
			this.roundedBorder.setColor(createCurrentTickColor());
			setBorder(JBUI.Borders.merge(roundedBorder, JBUI.Borders.empty(10, 4), true));

			this.counterLabel = new JBLabel(UIUtil.ComponentStyle.LARGE);
			this.counterLabel.setBorder(JBUI.Borders.empty(13, 15));
			add(counterLabel, BorderLayout.CENTER);
		}

		/**
		 * @return true if timer isn't equal to zero
		 */
		public boolean tick() {
			if (secondsToRest > 0) {
				String value = String.valueOf(secondsToRest--);
				counterLabel.setText(value);
				counterLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
				roundedBorder.setColor(createCurrentTickColor());
				updateUI();
				return true;
			} else {
				return false;
			}
		}

		@NotNull
		private Color createCurrentTickColor() {
			return ColorUtil.mix(JBColor.black, getBackground(), 1 - secondsToRest * dColorBalance);
		}

		public boolean isUp() {
			return secondsToRest > 0;
		}
	}

	public EyeHelpPanel(EyeHelpDialog eyeHelpDialog) {
		this.parent = eyeHelpDialog;
		setLayout(new GridLayoutManager(3, 1));

		String exercise = findExerciseMessage();

		// configure exercise panel
		TipUIUtil.Browser browser = TipUIUtil.createBrowser();
		browser.setText(exercise);
		browser.getComponent().setBorder(JBUI.Borders.empty(8, 12));
		browser.getComponent().setBackground(UIUtil.getPanelBackground());
		JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(browser.getComponent(), true);
		scrollPane.setBorder(JBUI.Borders.customLine(DIVIDER_COLOR, 0, 0, 1, 0));

		JBLabel timerInfoLabel = new JBLabel(EyeBundle.message("eye.dialog.timer.topic.label"));

		this.clockPanel = new JClockPanel(Optional.ofNullable(PluginSettings.getInstance())
				.map(PluginSettings::getState)
				.map(PluginSettings.EyeHelpState::getDurationBreak)
				.orElse(0));

		add(scrollPane, new GridConstraints(0, 0, 2, 1,
				GridConstraints.ALIGN_CENTER,
				GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_FIXED,
				null, null, null
		));

		JPanel panel = new JPanel();
		panel.setLayout(new VerticalLayout(10, SwingConstants.CENTER));
		panel.add(timerInfoLabel, VerticalLayout.TOP);
		panel.add(clockPanel, VerticalLayout.BOTTOM);
		add(panel, new GridConstraints(2, 0, 1, 1,
				GridConstraints.ALIGN_CENTER,
				GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_FIXED,
				null, null, null
		));

		parent.addKeyListener(create(KeyListener.class, this, "closeParent", "keyCode", "keyPressed"));

		startRefreshSeconds();
	}

	@NotNull
	private String findExerciseMessage() {
		List<EyeExercise> exercises = EyeExercise.findExercises();
		if (exercises.isEmpty()) {
			return EyeBundle.message("eye.dialog.exercises.dummy");
		}

		int index = DeveloperUtil.isDebugMode() ? 0 : (int) (exercises.size() * Math.random());
		return exercises.get(index).getExerciseText();
	}

	private void startRefreshSeconds() {
		if (!clockPanel.isUp()) {
			return;
		}

		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(new Runnable() {
			@SneakyThrows
			@Override
			public void run() {
				if (!clockPanel.tick()) {
					// close dialog after counter is down
					EdtExecutorService.getInstance()
							.execute(parent::doCancelAction);
					service.shutdown();
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	@Override
	public Dimension getPreferredSize() {
		return new JBDimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public void closeParent(Integer keyCode) {
		if (KeyEvent.VK_ESCAPE == keyCode) {
			parent.doCancelAction();
		}
	}
}
