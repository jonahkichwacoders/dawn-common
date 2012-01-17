/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.ui.views.monitor.actions;

import org.dawb.common.ui.views.monitor.MonitorView;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PlatformUI;

/**
 *
 */
public class ClearHardwareObjectsHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		// Get current dashboard view.
		MonitorView dashboard = (MonitorView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		if (dashboard==null) return Boolean.FALSE;
		
		dashboard.clearSelectedObjects();
		
		return Boolean.TRUE;
	}

}
