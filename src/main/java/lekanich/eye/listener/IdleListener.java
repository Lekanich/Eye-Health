package lekanich.eye.listener;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import com.intellij.openapi.application.ApplicationManager;
import lekanich.eye.settings.PluginSettings;


/**
 * @author Lekanich
 */
public class IdleListener implements AWTEventListener {
	private long lastEventTime = 0;
	private boolean isIdleDisabled = true;

	@Override
	public void eventDispatched(AWTEvent event) {
		// we will track all UI events and will save the date
		lastEventTime = System.currentTimeMillis();
		if (isIdleDisabled) {
			isIdleDisabled = false;

			ApplicationManager.getApplication().getMessageBus()
					.syncPublisher(EyeHelpStatusListener.EYE_HELP_STATUS_TOPIC)
					.statusChanged(EyeHelpStatusListener.Status.ACTIVE);
		}
	}

	/**
	 * @param idleTimeMs time after which plugin decide that it was idle time (in ms)
	 */
	public void testIdleAndDisableIfNeed(long idleTimeMs) {
		long dTimeInMs = System.currentTimeMillis() - lastEventTime;
		boolean isIdle = dTimeInMs > idleTimeMs;
		if (isIdle && !PluginSettings.isDisabled()) {
			isIdleDisabled = true;

			// notify about temporary disabling
			ApplicationManager.getApplication().getMessageBus()
					.syncPublisher(EyeHelpStatusListener.EYE_HELP_STATUS_TOPIC)
					.statusChanged(EyeHelpStatusListener.Status.TEMPORARY_DISABLED);
		}
	}
}
