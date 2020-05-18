package lekanich.eye.listener;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.concurrency.EdtScheduledExecutorService;
import org.jetbrains.annotations.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lekanich.eye.util.EyeHelpDialog;


/**
 * @author Lekanich
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EyeHelpSingleton implements EyeHelpListener {
	private static final Logger log = Logger.getInstance(EyeHelpSingleton.class);
	private static final EyeHelpSingleton instance = new EyeHelpSingleton();
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
					// TODO: PropertiesComponent.getInstance().getLong(LAST_TIME_TIPS_WERE_SHOWN, 0) temporary storage
					if (application.isDisposed()) {
						return;
					}

					EyeHelpDialog.showForProject();
				}, delayInSeconds, TimeUnit.SECONDS);

		Disposable disposable = () -> {
			disposableRef.set(null);
			if (future != null) {
				future.cancel(false);
			}
		};
		disposableRef.set(disposable);
		Disposer.register(application, disposable);
	}
}
