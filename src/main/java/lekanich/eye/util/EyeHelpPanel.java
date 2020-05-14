package lekanich.eye.util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingConstants;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.concurrency.EdtExecutorService;
import com.intellij.util.ui.JBUI;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lekanich.eye.EyeBundle;
import lekanich.eye.settings.ApplicationSettings;
import static java.beans.EventHandler.create;


/**
 * @author Lekanich
 */
@Slf4j
public class EyeHelpPanel extends JBPanel<EyeHelpPanel> {
	private final EyeHelpDialog parent;
	private final ApplicationSettings component;
	private final JBLabel exerciseLabel;
	private final JBLabel timerInfoLabel;
	private final JBLabel timerLabel;
	private final AtomicInteger secondsToRest = new AtomicInteger();

	public EyeHelpPanel(EyeHelpDialog eyeHelpDialog) {
		setLayout(new VerticalLayout(10, SwingConstants.CENTER));

		this.component = ApplicationSettings.getInstance();
		this.parent = eyeHelpDialog;

		this.exerciseLabel = new JBLabel(EyeBundle.message("eye.dialog.info.exercise"));
		this.exerciseLabel.setBorder(JBUI.Borders.empty(8, 21));
		this.timerInfoLabel = new JBLabel(EyeBundle.message("eye.dialog.timer.topic.label"));
		this.timerLabel = new JBLabel(EyeBundle.message("eye.dialog.timer.body.label", secondsToRest.get()));

		add(exerciseLabel, VerticalLayout.TOP);
		add(timerInfoLabel, VerticalLayout.CENTER);
		add(timerLabel, VerticalLayout.BOTTOM);

		parent.addKeyListener(create(KeyListener.class, this, "closeParent", "keyCode", "keyPressed"));

		if (component != null) {
			startRefreshSeconds();
		}
	}

	private void startRefreshSeconds() {
		secondsToRest.set(component.getState().getDurationBreak());

		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(new Runnable() {
			@SneakyThrows
			@Override
			public void run() {
				if (secondsToRest.get() > 0) {
					timerLabel.setText(EyeBundle.message("eye.dialog.timer.body.label", secondsToRest.decrementAndGet()));
					timerLabel.updateUI();
				} else {
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
