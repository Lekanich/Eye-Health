package lekanich.eye.listener;

import java.awt.AWTEvent;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.concurrency.EdtScheduledExecutorService;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lekanich.eye.settings.PluginSettings;
import lekanich.eye.ui.EyeHelpDialog;


/**
 * @author Lekanich
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EyeHelpSingleton implements EyeHelpListener {
	private static final Logger log = Logger.getInstance(EyeHelpSingleton.class);
	private static final long EVENT_MASK = AWTEvent.MOUSE_EVENT_MASK
			| AWTEvent.MOUSE_MOTION_EVENT_MASK
			| AWTEvent.MOUSE_WHEEL_EVENT_MASK
			| AWTEvent.KEY_EVENT_MASK
			| AWTEvent.FOCUS_EVENT_MASK;
	private static final EyeHelpSingleton instance = new EyeHelpSingleton();
	private static final IdleListener IDLE_LISTENER = new IdleListener();
	private volatile ScheduledFuture<?> future = null;

	public static EyeHelpSingleton getInstance() {
		return instance;
	}

	@Override
	public synchronized void scheduleEyeHelp(long delayInSeconds) {
		if (future != null) {
			future.cancel(false);
		}

		@NotNull Application application = ApplicationManager.getApplication();
		AtomicReference<Disposable> disposableRef = new AtomicReference<>();
		future = EdtScheduledExecutorService.getInstance()
				.schedule(() -> {
					Disposable disposable = disposableRef.getAndSet(null);
					if (disposable == null) {
						return;
					}

					Disposer.dispose(disposable);
					if (application.isDisposed()) {
						return;
					}

					long idleInMS = TimeUnit.SECONDS.toMillis(PluginSettings.getInstance().getState().getIdleTime());
					IDLE_LISTENER.testIdleAndDisableIfNeed(idleInMS);

					EyeHelpDialog.showForProject();
				}, delayInSeconds, TimeUnit.SECONDS);

		Disposable disposable = () -> {
			disposableRef.set(null);
			if (future != null) {
				future.cancel(false);
			}
		};
		disposableRef.set(disposable);
		UIUtil.addAwtListener(IDLE_LISTENER, EVENT_MASK, disposable);
		Disposer.register(application, disposable);
	}
}
