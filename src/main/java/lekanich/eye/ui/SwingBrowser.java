package lekanich.eye.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.TipUIUtil;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.util.ResourceUtil;
import com.intellij.util.SVGLoader;
import com.intellij.util.io.IOUtil;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBHtmlEditorKit;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import static com.intellij.util.ui.UIUtil.drawImage;


/**
 * It's a copy of {@link com.intellij.ide.util.TipUIUtil.SwingBrowser} with changed css file
 *
 * @author Lekanich
 */
public class SwingBrowser extends JEditorPane implements TipUIUtil.Browser {

	public SwingBrowser() {
		setEditable(false);
		setBackground(UIUtil.getTextFieldBackground());
		addHyperlinkListener(e -> {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				BrowserUtil.browse(e.getURL());
			}
		});
		URL resource = cssResource();
		HTMLEditorKit kit = new JBHtmlEditorKit(false) {
			private final ViewFactory myFactory = createViewFactory();

			//SVG support
			private ViewFactory createViewFactory() {
				return new HTMLEditorKit.HTMLFactory() {
					@Override
					public View create(Element elem) {
						View view = super.create(elem);
						if (view instanceof ImageView) {
							String src = (String) view.getElement().getAttributes().getAttribute(HTML.Attribute.SRC);
							if (src != null /*&& src.endsWith(".svg")*/) {
								final Image image;
								try {
									final URL url = new URL(src);
									Dictionary cache = (Dictionary) elem.getDocument().getProperty("imageCache");
									if (cache == null) {
										elem.getDocument().putProperty("imageCache", cache = new Dictionary() {
											private final HashMap myMap = new HashMap();

											@Override
											public int size() {
												return myMap.size();
											}

											@Override
											public boolean isEmpty() {
												return size() == 0;
											}

											@Override
											public Enumeration keys() {
												return Collections.enumeration(myMap.keySet());
											}

											@Override
											public Enumeration elements() {
												return Collections.enumeration(myMap.values());
											}

											@Override
											public Object get(Object key) {
												return myMap.get(key);
											}

											@Override
											public Object put(Object key, Object value) {
												return myMap.put(key, value);
											}

											@Override
											public Object remove(Object key) {
												return myMap.remove(key);
											}
										});
									}
									image = src.endsWith(".svg")
											? SVGLoader.load(url, JBUI.isPixHiDPI((Component) null) ? 2f : 1f)
											: Toolkit.getDefaultToolkit().createImage(url);
									cache.put(url, image);
									if (src.endsWith(".svg"))
										return new ImageView(elem) {
											@Override
											public Image getImage() {
												return image;
											}

											@Override
											public URL getImageURL() {
												return url;
											}

											@Override
											public void paint(Graphics g, Shape a) {
												Rectangle bounds = a.getBounds();
												int width = (int) getPreferredSpan(View.X_AXIS);
												int height = (int) getPreferredSpan(View.Y_AXIS);
												@SuppressWarnings("UndesirableClassUsage")
												BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
												Graphics2D graphics = buffer.createGraphics();
												super.paint(graphics, new Rectangle(buffer.getWidth(), buffer.getHeight()));
												drawImage(g, ImageUtil.ensureHiDPI(image, ScaleContext.create((Component) null)), bounds.x, bounds.y, null);
											}

											@Override
											public float getMaximumSpan(int axis) {
												return getPreferredSpan(axis);
											}

											@Override
											public float getMinimumSpan(int axis) {
												return getPreferredSpan(axis);
											}

											@Override
											public float getPreferredSpan(int axis) {
												return (axis == View.X_AXIS ? image.getWidth(null) : image.getHeight(null)) / JBUIScale.sysScale();
											}
										};
								} catch (IOException e) {
									//ignore
								}
							}
						}
						return view;
					}
				};
			}

			@Override
			public ViewFactory getViewFactory() {
				return myFactory;
			}
		};
		kit.getStyleSheet().addStyleSheet(UIUtil.loadStyleSheet(resource));
		setEditorKit(kit);
	}

	private URL cssResource() {
//		String cssFileName =  StartupUiUtil.isUnderDarcula() ? "tips_darcula.css" : "tips.css";
		String cssFileName = "exercise.css";
		return ResourceUtil.getResource(SwingBrowser.class, "/exercises/css/", cssFileName);
	}

	@Override
	public void setText(String t) {
		super.setText(t);
		if (t != null && t.length() > 0) {
			setCaretPosition(0);
		}
	}

	@Override
	public void load(String url) throws IOException {
		setText(IOUtil.readString(new DataInputStream(new URL(url).openStream())));
	}

	@Override
	public JComponent getComponent() {
		return this;
	}
}
