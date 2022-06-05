package lekanich.eye.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import com.intellij.ide.util.TipUIUtil;
import com.intellij.ide.util.TipUIUtil.Browser;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.concurrency.EdtExecutorService;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StyleSheetUtil;
import com.intellij.util.ui.UIUtil;
import lekanich.DeveloperUtil;
import lekanich.eye.EyeBundle;
import lekanich.eye.EyeExercise;
import lekanich.eye.settings.PluginSettings;
import org.jetbrains.annotations.NotNull;
import static java.beans.EventHandler.create;


/**
 * @author Lekanich
 */
public class EyeHelpPanel extends JBPanel<EyeHelpPanel> implements Disposable {
	private static final Logger log = Logger.getInstance(EyeHelpPanel.class);
	private static final JBColor DIVIDER_COLOR = new JBColor(0xd9d9d9, 0x515151);
	private static final int DEFAULT_WIDTH = 400;
	private static final int DEFAULT_HEIGHT = 200;
	private final EyeHelpDialog dialog;
	private final JClockPanel clockPanel;

	private static final class JClockPanel extends JBPanel<JClockPanel> {
		private final JBLabel counterLabel;
		private long secondsToRest;

		public JClockPanel(long secondsToRest) {
			this.secondsToRest = secondsToRest;

			setLayout(new BorderLayout());

			this.counterLabel = new JBLabel(UIUtil.ComponentStyle.LARGE);
			this.counterLabel.setBorder(JBUI.Borders.empty(0, 4, 20, 4));
			add(counterLabel, BorderLayout.CENTER);
		}

		/**
		 * @return true if timer isn't equal to zero
		 */
		public boolean tick() {
			if (secondsToRest > 0) {
				String value = String.valueOf(secondsToRest--);
				counterLabel.setText(value);
				counterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
				updateUI();
				return true;
			} else {
				return false;
			}
		}

		public boolean isUp() {
			return secondsToRest > 0;
		}
	}

	public EyeHelpPanel(EyeHelpDialog eyeHelpDialog) {
		this.dialog = eyeHelpDialog;
		setLayout(new GridLayoutManager(3, 1));

		String exercise = findExerciseMessage();

		// configure exercise panel
		TipUIUtil.Browser browser = createBrowser();
		browser.setText(exercise);
		browser.getComponent().setBorder(JBUI.Borders.empty(8, 12));
		browser.getComponent().setBackground(UIUtil.getPanelBackground());
		JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(browser.getComponent(), true);
		scrollPane.setBorder(JBUI.Borders.customLine(DIVIDER_COLOR, 0, 0, 1, 0));

		JBLabel timerInfoLabel = new JBLabel(EyeBundle.message("eye.dialog.timer.topic.label"));

		this.clockPanel = new JClockPanel(Optional.ofNullable(PluginSettings.getInstance()).map(PluginSettings::getState).map(PluginSettings.PluginAppState::getDurationBreak).orElse(0L));

		add(scrollPane, new GridConstraints(0, 0, 2, 1, GridConstraints.ALIGN_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));

		JPanel panel = new JPanel();
		panel.setLayout(new VerticalLayout(10, SwingConstants.CENTER));
		panel.add(timerInfoLabel, VerticalLayout.TOP);
		panel.add(clockPanel, VerticalLayout.BOTTOM);
		add(panel, new GridConstraints(2, 0, 1, 1, GridConstraints.ALIGN_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null));

		dialog.addKeyListener(create(KeyListener.class, this, "closeParent", "keyCode", "keyPressed"));

		startRefreshSeconds();
	}

	@Override
	public void dispose() {
		/*NOP*/
	}

	@NotNull
	private String findExerciseMessage() {
		List<EyeExercise> exercises = EyeExercise.findExercises();
		if (exercises.isEmpty()) {
			return EyeBundle.message("eye.dialog.exercises.dummy");
		}

		int index = DeveloperUtil.isDebugMode() ? 2 : (int) (exercises.size() * Math.random());
		return exercises.get(index).getExerciseText();
	}

	private void startRefreshSeconds() {
		if (!clockPanel.isUp()) {
			return;
		}

		// should be stopped if dialog is not visible
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(() -> {
			if (!clockPanel.tick()) {
				// close dialog after counter is down
				EdtExecutorService.getInstance().execute(dialog::doCancelAction);
				service.shutdown();
			}
		}, 0, 1, TimeUnit.SECONDS);

		Disposer.register(this, service::shutdown);
	}

	@Override
	public Dimension getPreferredSize() {
		return new JBDimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public void closeParent(Integer keyCode) {
		if (KeyEvent.VK_ESCAPE == keyCode) {
			dialog.doCancelAction();
		}
	}

	private TipUIUtil.Browser createBrowser() {
		Browser browser = TipUIUtil.createBrowser();
		EditorKit kit = ((JEditorPane) browser.getComponent()).getEditorKit();
		if (kit instanceof HTMLEditorKit) {
			URL cssResource = EyeExercise.cssResource();
			if (cssResource != null) {
				try {
					((HTMLEditorKit) kit).getStyleSheet().addStyleSheet(
							StyleSheetUtil.loadStyleSheet(cssResource.openStream(), cssResource)
					);
				} catch (IOException e) {
					log.warn(e);
				}
			}
		} else {
			log.warn("Kit inside internal browser wasn't HTML. It was: " + kit.getClass().getCanonicalName());
		}

		return browser;
	}
}
