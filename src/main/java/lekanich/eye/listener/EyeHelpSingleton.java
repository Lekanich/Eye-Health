package lekanich.eye.listener;

import java.awt.AWTEvent;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.concurrency.EdtExecutorService;
import com.intellij.util.ui.UIUtil;
import lekanich.eye.settings.PluginSettings;
import lekanich.eye.ui.EyeHelpDialog;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;


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
	@Getter
	private static final EyeHelpSingleton instance = new EyeHelpSingleton();
	private static final IdleListener IDLE_LISTENER = new IdleListener();
	private final AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>(null);

	@Override
	public synchronized void scheduleEyeHelp(final long delayInSeconds) {
		if (future.get() != null) {
			future.get().cancel(false);
		}

		@NotNull final PluginSettings parent = PluginSettings.getInstance();
		final ShowCommand command = new ShowCommand(future, parent);
		future.set(EdtExecutorService.getScheduledExecutorInstance()
				.schedule(command, delayInSeconds, TimeUnit.SECONDS));

		UIUtil.addAwtListener(IDLE_LISTENER, EVENT_MASK, command);

		Disposer.register(parent, command);
	}

	@RequiredArgsConstructor
	private static class ShowCommand implements Runnable, Disposable {
		private final AtomicBoolean disposed = new AtomicBoolean(false);
		private final AtomicReference<ScheduledFuture<?>> future;
		private final PluginSettings parent;

		@Override
		public void run() {
			if (disposed.get()) {
				// it was disposed
				return;
			}

			Disposer.dispose(this);
			if (parent.isDisposed()) {
				return;
			}

			final long idleInMS = TimeUnit.SECONDS.toMillis(PluginSettings.getInstance().getState().getIdleTime());
			IDLE_LISTENER.checkIdleAndDisableIfNeed(idleInMS);

			EyeHelpDialog.showForProject();
		}

		@Override
		public void dispose() {
			disposed.set(true);
			Optional.ofNullable(future.get()).ifPresent(it -> it.cancel(false));
		}
	}
}
