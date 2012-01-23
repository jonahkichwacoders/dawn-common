/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.gda.extensions.plot;

import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.util.GridUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DUIAdapter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.util.PlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.util.PlotUtils;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotServerConnection;


/**
 * Link between EDNA 1D plotting and diamond plotter
 * 
 * This is the only place that the Diamond plotting is referenced in org.edna.
 * 
 * @author gerring
 *
 */
public class DiamondPlottingSystem extends AbstractPlottingSystem {

	private Logger logger = LoggerFactory.getLogger(DiamondPlottingSystem.class);
	
	private PlotServerConnection plotServerConnection;
	private PlotWindow           plotWindow;
	
	public void createPlotPart(final Composite      parent,
							   final String         plotName,
							   final IActionBars    bars,
							   final PlotType       hint,
							   final IWorkbenchPart part) {
		
		// Connect this PlotWindow to the server TODO Lazy initiation with this?
		// no point connecting it to the plot server unless absolutely necessary.
		this.plotServerConnection = new PlotServerConnection(plotName);
		
		final GuiBean     bean     = plotServerConnection.getGUIInfo();
		final GuiPlotMode plotMode = (GuiPlotMode) bean.get(GuiParameters.PLOTMODE);
		this.plotWindow = new PlotWindow(parent,
				                        plotMode,
										plotServerConnection,
										plotServerConnection,
										bars,
										part.getSite().getPage(),
										plotName);	
		plotServerConnection.setPlotWindow(plotWindow);
		
		// This block relies on the plotter always starting as 1D which it does currently
		// We intentionally fail if this is not the case so alert the test desks as to the
		// change.
		final Plot1DUIAdapter ui = (Plot1DUIAdapter)plotWindow.getPlotUI();
		ui.addPositionSwitchListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				GridUtils.setVisible(pointControls, (Boolean)event.getNewValue());
				pointControls.getParent().layout(new Control[]{pointControls});
			}
		}); 
	}
	
	/**
	 * 
	 */
	@Override
	public void createPlot(final AbstractDataset       data, 
			               final List<AbstractDataset> axes,
			               final PlotType              mode, 
			               final IProgressMonitor      monitor) {
		
		if (mode.is1D()) {
			PlotUtils.create1DPlot(data, axes, getPlotMode(mode), plotWindow, monitor);
		} else {
		    PlotUtils.createPlot(data, axes, getGuiPlotMode(mode), plotWindow, monitor);
		}
	}

	private GuiPlotMode getGuiPlotMode(PlotType type) {
		if (type == PlotType.IMAGE)   return GuiPlotMode.TWOD;
		if (type == PlotType.SURFACE) return GuiPlotMode.SURF2D;
		return null;
	}

	/**
	 * Bodge together PlotMode and PlotType
	 * @param type
	 * @return
	 */
	private PlotMode getPlotMode(PlotType type) {
		if (type == PlotType.PT1D)         return PlotMode.PM1D;
		if (type == PlotType.PT1D_STACKED) return PlotMode.PMSTACKED;
		if (type == PlotType.PT1D_3D)      return PlotMode.PM3D;
		return null;
	}

	@Override
	public void reset() {
		try {
			// BODGE - Cannot work out how to plot nothing.
			final AbstractDataset id = new IntegerDataset(new int[]{0,0},2);
			id.setName("-");
			plotWindow.getMainPlotter().replaceAllPlots(Arrays.asList(new AbstractDataset[]{id}));
			plotWindow.getMainPlotter().getColourTable().clearLegend();
			plotWindow.getMainPlotter().refresh(true);
		} catch (Throwable e) {
			logger.error("Cannot remove plots!", e);
		}
		
	}

	@Override
	public void dispose() {
    	if (plotServerConnection!=null) plotServerConnection.dispose();
    	if (plotWindow!=null)           plotWindow.dispose();
	}

}