package lekanich.eye.listener;

import com.intellij.util.messages.Topic;


/**
 * @author Lekanich
 */
public interface EyeHelpStatusListener {
	public static final Topic<EyeHelpStatusListener> EYE_HELP_STATUS_TOPIC = Topic.create("Topic to observe changes of the status", EyeHelpStatusListener.class);

	public void statusChanged(Status status);

	public static enum Status {
		ACTIVE, TEMPORARY_DISABLED, DISABLED
	}
}
