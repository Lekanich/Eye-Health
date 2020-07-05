package lekanich.eye.listener;

import com.intellij.util.messages.Topic;


/**
 * @author Lekanich
 */
public interface EyeHelpStatusListener {
	Topic<EyeHelpStatusListener> EYE_HELP_STATUS_TOPIC = Topic.create("Topic to observe changes of the status", EyeHelpStatusListener.class);

	void statusChanged(Status status);

	enum Status {
		ACTIVE, TEMPORARY_DISABLED, DISABLED
	}
}
