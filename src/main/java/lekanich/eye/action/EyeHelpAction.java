package lekanich.eye.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import lekanich.eye.util.EyeHelpDialog;


/**
 * @author Lekanich
 */
public class EyeHelpAction extends AnAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		EyeHelpDialog.showForProject();
	}
}
