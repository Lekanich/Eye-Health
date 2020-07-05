package lekanich.eye.listener;

import com.intellij.util.messages.Topic;


/**
 * @author Lekanich
 */
public interface EyeHelpListener {
	Topic<EyeHelpListener> EYE_HELP_TOPIC = Topic.create("Your eyes need to rest", EyeHelpListener.class);

	void scheduleEyeHelp(long delayInSeconds);
}
