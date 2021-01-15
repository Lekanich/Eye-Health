package lekanich.eye.listener;

import java.awt.AWTEvent;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.intellij.openapi.Disposable;
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
	private static final long EVENT_MASK = AWTEvent.MOUSE_EVENT_MASK
			| AWTEvent.MOUSE_MOTION_EVENT_MASK
			| AWTEvent.MOUSE_WHEEL_EVENT_MASK
			| AWTEvent.KEY_EVENT_MASK
			| AWTEvent.FOCUS_EVENT_MASK;
	private static final EyeHelpSingleton instance = new EyeHelpSingleton();
	private static final IdleListener IDLE_LISTENER = new IdleListener();
	private final AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>(null);

	public static EyeHelpSingleton getInstance() {
		return instance;
	}

	@Override
	public synchronized void scheduleEyeHelp(long delayInSeconds) {
		if (future.get() != null) {
			future.get().cancel(false);
		}

		@NotNull Disposable parent = PluginSettings.getInstance();
		AtomicReference<Disposable> disposableRef = new AtomicReference<>();
		future.set(EdtScheduledExecutorService.getInstance()
				.schedule(() -> {
					Disposable disposable = disposableRef.getAndSet(null);
					if (disposable == null) {
						return;
					}

					Disposer.dispose(disposable);
					if (Disposer.isDisposed(parent)) {
						return;
					}

					long idleInMS = TimeUnit.SECONDS.toMillis(PluginSettings.getInstance().getState().getIdleTime());
					IDLE_LISTENER.checkIdleAndDisableIfNeed(idleInMS);

					EyeHelpDialog.showForProject();
				}, delayInSeconds, TimeUnit.SECONDS));

		Disposable disposable = () -> {
			disposableRef.set(null);
			if (future.get() != null) {
				future.get().cancel(false);
			}
		};
		disposableRef.set(disposable);
		UIUtil.addAwtListener(IDLE_LISTENER, EVENT_MASK, disposable);
		Disposer.register(parent, disposable);
	}
}
