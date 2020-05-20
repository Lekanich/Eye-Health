package lekanich.eye.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.concurrency.EdtExecutorService;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import lombok.SneakyThrows;
import lekanich.eye.EyeBundle;
import lekanich.eye.settings.PluginSettings;
import static java.beans.EventHandler.create;


/**
 * @author Lekanich
 */
public class EyeHelpPanel extends JBPanel<EyeHelpPanel> {
	private final EyeHelpDialog parent;
	private final JBLabel exerciseLabel;
	private final JBLabel timerInfoLabel;
	private final JClockPanel clockPanel;

	private static final class JClockPanel extends JPanel {
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
				counterLabel.setText(String.valueOf(secondsToRest--));
				roundedBorder.setColor(createCurrentTickColor());
				counterLabel.updateUI();
				return true;
			} else {
				return false;
			}
		}

		@NotNull
		private Color createCurrentTickColor() {
			return ColorUtil.mix(JBColor.black, JBColor.foreground(), 1 - secondsToRest * dColorBalance);
		}

		public boolean isUp() {
			return secondsToRest > 0;
		}
	}

	public EyeHelpPanel(EyeHelpDialog eyeHelpDialog) {
		setLayout(new VerticalLayout(10, SwingConstants.CENTER));

		this.parent = eyeHelpDialog;

		this.exerciseLabel = new JBLabel(findExerciseMessage());
		this.exerciseLabel.setBorder(JBUI.Borders.empty(8, 21));
		this.timerInfoLabel = new JBLabel(EyeBundle.message("eye.dialog.timer.topic.label"));

		this.clockPanel = new JClockPanel(Optional.ofNullable(PluginSettings.getInstance())
				.map(PluginSettings::getState)
				.map(PluginSettings.EyeHelpState::getDurationBreak)
				.orElse(0));

		add(exerciseLabel, VerticalLayout.TOP);
		add(timerInfoLabel, VerticalLayout.CENTER);
		add(clockPanel, VerticalLayout.BOTTOM);

		parent.addKeyListener(create(KeyListener.class, this, "closeParent", "keyCode", "keyPressed"));

		startRefreshSeconds();
	}

	@NotNull
	private String findExerciseMessage() {
		List<String> exercises = EyeBundle.getExercises();
		return exercises.get((int) (exercises.size() * Math.random()));
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

	public void closeParent(Integer keyCode) {
		if (KeyEvent.VK_ESCAPE == keyCode) {
			parent.doCancelAction();
		}
	}
}
