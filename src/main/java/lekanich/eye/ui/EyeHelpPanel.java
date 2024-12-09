package lekanich.eye.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
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
import com.intellij.util.ResourceUtil;
import com.intellij.util.concurrency.EdtExecutorService;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.StyleSheetUtil;
import com.intellij.util.ui.UIUtil;
import lekanich.eye.EyeBundle;
import lekanich.eye.logic.ExerciseTuple;
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

		public JClockPanel(final long secondsToRest) {
			this.secondsToRest = secondsToRest;

			setLayout(new BorderLayout());

			this.counterLabel = new JBLabel(UIUtil.ComponentStyle.LARGE);
			this.counterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
				revalidate();
				repaint();
				return true;
			} else {
				return false;
			}
		}

		public boolean isUp() {
			return secondsToRest > 0;
		}
	}

	public EyeHelpPanel(final EyeHelpDialog eyeHelpDialog) {
		this.dialog = eyeHelpDialog;

		withPreferredWidth(DEFAULT_WIDTH);

		final GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		setBorder(JBUI.Borders.empty());

		final ExerciseTuple exercise = ExerciseTuple.findExercise();

		// configure exercise panel
		final JEditorPane browser = createBrowser();
		browser.setText(exercise.exercise());
		browser.setBorder(JBUI.Borders.compound(
				JBUI.Borders.customLine(DIVIDER_COLOR, 0, 0, 4, 0),
				JBUI.Borders.empty(8, 12)
		));
		browser.setBackground(UIUtil.getPanelBackground());

		add(browser);
		layout.setConstraints(browser, new GridBagConstraints(0, 0,
				1, 2,
				0, 0,
				GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
				JBUI.emptyInsets(), 0, 0));

		final JPanel panel = new JPanel();
		panel.setLayout(new VerticalLayout(10, SwingConstants.CENTER));
		panel.add(new JBLabel(EyeBundle.message("eye.dialog.timer.topic.label")), VerticalLayout.TOP);

		this.clockPanel = new JClockPanel(exercise.durationBreak());
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


	private void startRefreshSeconds() {
		if (!clockPanel.isUp()) {
			return;
		}

		// should be stopped if dialog is not visible
		final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(() -> {
			if (!clockPanel.tick()) {
				// close dialog after counter is down
				EdtExecutorService.getInstance().execute(dialog::doCancelAction);
				service.shutdown();
			}
		}, 0, 1, TimeUnit.SECONDS);

		Disposer.register(this, service::shutdown);
	}

	public void closeParent(final Integer keyCode) {
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
		final JEditorPane pane = new JEditorPane() {
			@Override
			public Caret getCaret() {
				final DefaultCaret caret = new DefaultCaret();
				caret.setVisible(false);
				caret.setSelectionVisible(false);
				return caret;
			}
		};
		pane.setEditable(false);
		pane.setBackground(UIUtil.getTextFieldBackground());

		final HTMLEditorKit kit = new HTMLEditorKitBuilder()
				.withGapsBetweenParagraphs()
				.build();
		try {
			final String cssResource = cssResourceFileName();
			final StyleSheet styleSheet = loadStyleSheet(cssResource);
			if (styleSheet != null) {
				kit.getStyleSheet().addStyleSheet(styleSheet);
			}
		} catch (IOException e) {
			log.warn("Cannot load stylesheet ", e);
		}

		pane.setEditorKit(kit);
		return pane;
	}

	private static StyleSheet loadStyleSheet(final String fileName) throws IOException {
		final byte[] data = ResourceUtil.getResourceAsBytes(fileName, EyeHelpPanel.class.getClassLoader());
		if (data == null) {
			return null;
		}

		return StyleSheetUtil.loadStyleSheet(new ByteArrayInputStream(data));
	}

	@NotNull
	public static String cssResourceFileName() {
		return "exercises/css/" + (isDark() ? "exercise_darcula.css" : "exercise.css");
	}

	private static boolean isDark() {
		return StartupUiUtil.isUnderDarcula();
	}
}
