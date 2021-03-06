package werkzeugkasten.dirbuildpath.action;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

import werkzeugkasten.common.resource.ResourceUtil;
import werkzeugkasten.common.runtime.AdaptableUtil;
import werkzeugkasten.dirbuildpath.job.RemoveDirBuildpathJob;

public class RemoveClasspathDirAction implements IActionDelegate {

	protected ISelection selection;

	public void run(IAction action) {
		if (this.selection.isEmpty()) {
			return;
		}
		IStructuredSelection ss = AdaptableUtil.to(this.selection,
				IStructuredSelection.class);
		if (ss == null) {
			return;
		}

		Map<IProject, List<IPath>> map = ResourceUtil.toProjectPathMap(ss
				.iterator());
		new RemoveDirBuildpathJob(map).schedule();

	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
