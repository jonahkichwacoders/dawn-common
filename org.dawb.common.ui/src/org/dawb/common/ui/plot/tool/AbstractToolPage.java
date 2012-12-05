package org.dawb.common.ui.plot.tool;

import java.util.Collection;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.util.text.StringUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;

/**
 * 
Page to extend for adding a tool to the plotting.

There are three possibilities for a tool page:

1.	One instance per plotting system and dynamically chosen by the user (default)
2.	Open in a dedicated view, one instance per plotting system but the view can only be this tool.
OR
3.	Static tool, one instance per view it's open in.


3 works as follows: When the tool is open in a dedicated view, if it implements the method isStaticTool() to return true, 
it will exist as one instance. The tool will have to set things up to respond to plotting system changing in this case.

Tools can be added to a perspective in 2 or 3 form in the prespective by adding the “org.dawb.workbench.plotting.views.toolPageView.fixed”
view to the perspective and setting the secondary id to the tool id required. For instance ‘org.dawb.workbench.plotting.tools.azimuthalProfileTool’. 
Or by programmatically when opening a view by id. 

 @author fcp94556
 
 */
public abstract class AbstractToolPage extends Page implements IToolPage, IAdaptable {

	private IToolPageSystem toolSystem;
	private IPlottingSystem plotSystem;
	private IWorkbenchPart  part;
	private String          title;
	private String          unique_id;
	private String          cheatSheetId;
	private ImageDescriptor imageDescriptor;
	private IViewPart       viewPart;

	public AbstractToolPage() {
		this.unique_id = StringUtils.getUniqueId(AbstractToolPage.class);
	}

	@Override
	public void setPlottingSystem(IPlottingSystem system) {
		this.plotSystem = system;
	}

	@Override
    public IPlottingSystem getPlottingSystem() {
    	if (getLinkedToolPlot()!=null) return getLinkedToolPlot();
    	return plotSystem;
    }

	@Override
	public IToolPageSystem getToolSystem() {
		return toolSystem;
	}
	@Override
	public void setToolSystem(IToolPageSystem system) {
		this.toolSystem = system;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	private boolean isActive = false;
	public boolean isActive() {
		return isActive;
	}
	
	/**
	 * Does nothing by default - optionally override.
	 */
	public void activate() {
		isActive = true;
	}

	/**
	 * Does nothing by default - optionally override.
	 */
	public void deactivate() {
		isActive = false;
	}
	
	private boolean isDisposed = false;
	public void dispose() {
		deactivate();
		super.dispose();
		toolSystem = null;
	    plotSystem = null;
		part       = null;
		isDisposed = true;
	}
		
	public boolean isDisposed() {
		return isDisposed;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((unique_id == null) ? 0 : unique_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractToolPage other = (AbstractToolPage) obj;
		if (unique_id == null) {
			if (other.unique_id != null)
				return false;
		} else if (!unique_id.equals(other.unique_id))
			return false;
		return true;
	}

	public String toString(){
		if (isDisposed) return "Disposed '"+getTitle()+"'";
		if (getTitle()!=null) return getTitle();
		return super.toString();
	}

	@Override
	public IWorkbenchPart getPart() {
		return part;
	}

	public void setPart(IWorkbenchPart part) {
		this.part = part;
	}

	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	public void setImageDescriptor(ImageDescriptor imageDescriptor) {
		this.imageDescriptor = imageDescriptor;
	}
	
	/**
	 * Default does nothing
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
		return null;
	}

	public IViewPart getViewPart() {
		return viewPart;
	}

	public void setViewPart(IViewPart viewPart) {
		this.viewPart = viewPart;
	}
	
	/**
	 * this method gives access to the image trace plotted in the
	 * main plotter or null if one is not plotted.
	 * @return
	 */
	public IImageTrace getImageTrace() {
		IPlottingSystem plotting = getPlottingSystem();
		if (plotting == null) return null;

		final Collection<ITrace> traces = plotting.getTraces(IImageTrace.class);
		if (traces==null || traces.size()==0) return null;
		final ITrace trace = traces.iterator().next();
		return trace instanceof IImageTrace ? (IImageTrace)trace : null;
	}
	
	private String toolId; // You can only set this once!
	public String getToolId() {
		return toolId;
	}
	public void setToolId(String id) {
		if (toolId!=null) throw new RuntimeException("The tool id is already set and cannot be changed!");
		this.toolId = id;
	}
		
	public IToolPage cloneTool() throws Exception {
		
		final IToolPage clone = getClass().newInstance();
		clone.setToolSystem(getToolSystem());
		clone.setPlottingSystem(getPlottingSystem());
		clone.setTitle(getTitle());
		clone.setPart(getPart());
		clone.setToolId(getToolId());
		clone.setImageDescriptor(getImageDescriptor());
	    clone.setCheatSheetId(getCheatSheetId());
		return clone;
	}
	
	/**
	 * Override in your tool page. If the page is opened in a dedicated view,
	 * a new version of the tool is created. However there may be data in the original
	 * linked tool that you wish to sync. In this case override sync and a reference
	 * to the original tool will be provided when it is opened in a dedicated view.
	 * 
	 * @param with
	 */
	@Override
	public void sync(IToolPage with) {
		
	}

	public String getCheatSheetId() {
		return cheatSheetId;
	}

	public void setCheatSheetId(String cheatSheetId) {
		this.cheatSheetId = cheatSheetId;
	}

	/**
	 * Override to return true if the tool, when opened should always be
	 * in a popped out view
	 * @return
	 */
	public boolean isAlwaysSeparateView() {
		return false;
	}
	
	public boolean isDedicatedView() {
		final String id = getViewPart().getSite().getId();
		return "org.dawb.workbench.plotting.views.toolPageView.fixed".equals(id);
	}
	
	@Override
	public boolean isStaticTool() {
		return false;
	}
	
	/**
	 * returns true if we are linked to an IToolPage
	 * @return
	 */
	protected boolean isLinkedToolPage() {
		return getLinkedToolPage()!=null;
	}

	/**
	 * Returns tool page we are a sub tool of or null if we are not
	 * @return
	 */
    protected IToolPage getLinkedToolPage() {
    	final IWorkbenchPart part = getPart();
        if (part instanceof IToolContainer) {
    		// Go back up one so that history of profiles can be done.
        	IToolContainer tView  = (IToolContainer)getPart();
    		if (tView.getActiveTool() !=null) {
    			final IToolPage tPage = (IToolPage)tView.getActiveTool();
    			return tPage;
    		}
    	}
   	    return null;
    }
    
    protected IPlottingSystem getLinkedToolPlot() {
    	IToolPage linkedTool = getLinkedToolPage();
    	return linkedTool !=null ? linkedTool.getToolPlottingSystem() : null;
    }
    	
	@Override
	public IPlottingSystem getToolPlottingSystem() {
		return null;
	}
	
	/**
	 * Optionally override to provide setting of data on a tool externally
	 * 
	 * By default does nothing.
	 * 
	 * @param data
	 */
	public void setData(Object data) {
		
	}

}
