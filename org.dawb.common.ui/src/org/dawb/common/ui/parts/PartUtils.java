package org.dawb.common.ui.parts;

import org.dawnsci.plotting.api.IPlottingContainer;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.MultiPageEditorPart;

public class PartUtils {

	/**
	 * Attempt to get a plotting system from a part
	 * @param part
	 * @return plotting system (may return null)
	 */
	public static IPlottingSystem getPlottingSystem(IWorkbenchPart part) {
		if (part instanceof MultiPageEditorPart) {
			MultiPageEditorPart mpp = (MultiPageEditorPart) part;
			Object page = mpp.getSelectedPage();
			
			if (page instanceof IEditorPart) {
				part = (IWorkbenchPart) page;
			} else {
				return null;
			}
		}

		IPlottingSystem system = (IPlottingSystem)part.getAdapter(IPlottingSystem.class);
		
		if (system==null && part instanceof IPlottingContainer) {
			system = ((IPlottingContainer) part).getPlottingSystem();
		}

		return system;
	}
}