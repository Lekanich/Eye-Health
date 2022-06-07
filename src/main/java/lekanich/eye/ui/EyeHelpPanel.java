package lekanich.eye.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import com.intellij.ide.util.TipUIUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.concurrency.EdtExecutorService;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.intellij.util.ui.JBUI;
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

		withPreferredWidth(DEFAULT_WIDTH);

		final GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		setBorder(JBUI.Borders.empty());

		final String exercise = findExerciseMessage();

		// configure exercise panel
		final JEditorPane browser = createBrowser();
		browser.setText(exercise);
		browser.setBorder(JBUI.Borders.compound(
				JBUI.Borders.customLine(DIVIDER_COLOR, 0, 0, 3, 0),
				JBUI.Borders.empty(8, 12)
		));
		browser.setBackground(UIUtil.getPanelBackground());

		add(browser);
		layout.setConstraints(browser, new GridBagConstraints(0, 0,
				1, 2,
				0, 0,
				GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
				JBUI.emptyInsets(), 0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new VerticalLayout(10, SwingConstants.CENTER));
		panel.add(new JBLabel(EyeBundle.message("eye.dialog.timer.topic.label")), VerticalLayout.TOP);

		this.clockPanel = new JClockPanel(Optional.ofNullable(PluginSettings.getInstance())
				.map(PluginSettings::getState)
				.map(PluginSettings.PluginAppState::getDurationBreak)
				.orElse(0L));
		panel.add(clockPanel, VerticalLayout.BOTTOM);

		add(panel);
		layout.setConstraints(panel, new GridBagConstraints(0, 2,
				1, 1,
				0, 0,
				GridBagConstraints.SOUTH, GridBagConstraints.NONE,
				JBUI.insets(10), 0, 0));

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

		int index = DeveloperUtil.isDebugMode()
				? 2
				: (int) (exercises.size() * Math.random());
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

	public void closeParent(Integer keyCode) {
		if (KeyEvent.VK_ESCAPE == keyCode) {
			dialog.doCancelAction();
		}
	}

	/**
	 * Inspired by {@link TipUIUtil.Browser}
	 *
	 * @return pane-browser
	 */
	private JEditorPane createBrowser() {
		JEditorPane pane = new JEditorPane();
		pane.setEditable(false);
		pane.setBackground(UIUtil.getTextFieldBackground());

		HTMLEditorKit kit = new HTMLEditorKitBuilder()
				.withGapsBetweenParagraphs()
				.build();
		pane.setEditorKit(kit);
		try {
			URL cssResource = EyeExercise.cssResource();
			if (cssResource != null) {
				kit.getStyleSheet().addStyleSheet(loadStyleSheet(cssResource));
			}
		} catch (IOException e) {
			log.warn("Cannot load stylesheet ", e);
		}

		return pane;
	}

	private static StyleSheet loadStyleSheet(URL url) throws IOException {
		StyleSheet result = new StyleSheet();
		result.loadRules(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8), url);
		return result;
	}
}
