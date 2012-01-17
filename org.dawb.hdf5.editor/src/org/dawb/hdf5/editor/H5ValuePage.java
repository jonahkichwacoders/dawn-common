/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.hdf5.editor;

import java.util.Arrays;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;

import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H5ValuePage extends Page  implements ISelectionListener, IPartListener {

	private static Logger logger = LoggerFactory.getLogger(H5ValuePage.class);
	
	protected CLabel       label;
	protected SourceViewer sourceViewer;
	protected StructuredSelection lastSelection;

	protected Composite container;

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		
		this.container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		final GridLayout layout = (GridLayout)container.getLayout();
		layout.horizontalSpacing=0;
		layout.verticalSpacing  =0;
		layout.marginBottom     =0;
		layout.marginTop        =0;
		layout.marginLeft       =0;
		layout.marginRight      =0;
		layout.marginHeight     =0;
		layout.marginWidth      =0;
		container.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		this.label  = new CLabel(container, SWT.LEFT);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		this.sourceViewer = new SourceViewer(container, null, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY );
		sourceViewer.setEditable(false);
		sourceViewer.getTextWidget().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    		
		getSite().getPage().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		getSite().getPage().addPartListener(this);
		
		try {
			updateSelection(getActivePage().getSelection());
		} catch (Throwable ignored) {
			// There might not be a selection or page.
		}

	}

	@Override
	public Control getControl() {
		return container;
	}
	
	@Override
	public void setFocus() {
		sourceViewer.getTextWidget().setFocus();
	}

	public void dispose() {
		super.dispose();
		getSite().getPage().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		getSite().getPage().removePartListener(this);
		lastSelection=null;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		try {
			updateSelection(selection);
		} catch (Exception e) {
			logger.error("Cannot update value", e);
		}
	}

	protected void updateSelection(ISelection selection) throws Exception {
		
		if (selection instanceof StructuredSelection) {
			this.lastSelection = (StructuredSelection)selection;
			final Object sel = lastSelection.getFirstElement();
			
			updateObjectSelection(sel);				
			
			sourceViewer.refresh();
			label.getParent().layout(new Control[]{label, sourceViewer.getTextWidget()});
			
			return;
		}
		
		clear();
	}

	/**
	 * Set it back to blank
	 */
	private void clear() {
		label.setText("");
		sourceViewer.getTextWidget().setText("");
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part == this) {
			try {
				updateSelection(lastSelection);
			} catch (Throwable ignored) {
				// There might not be a selection or page.
			}
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		
	}

	public void updateObjectSelection(Object sel)  throws Exception{
		
		if (sel instanceof DefaultMutableTreeNode) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode)sel;
			final Object                 ob   = node.getUserObject();
			if (ob instanceof HObject) {
				createH5Value((HObject)ob);
			}
 		} else if (sel instanceof H5Path) { // Might be nexus part.
			
			try {
				final H5Path h5Path = (H5Path)sel;
				final String path   = h5Path.getPath();
				final IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if (part instanceof IH5Editor) {
					final String filePath = ((IH5Editor)part).getFilePath();
					final IHierarchicalDataFile file = HierarchicalDataFactory.getReader(filePath);
					final HObject ob = file.getData(path);
					createH5Value(ob);
				}
				
			} catch (Exception ne) {
				logger.error(ne.getMessage()); // Not serious, no need for stack.
			}
			
		}
	}
	
	private void createH5Value(HObject ob) throws Exception {
		
		if (ob instanceof Dataset) {
			final Dataset  set   = (Dataset)ob;
			final Datatype dType = set.getDatatype();
			final long[] shape = (long[])set.getDims();
			
			final StringBuilder buf = new StringBuilder();
			if (dType.getDatatypeClass()==Datatype.CLASS_STRING) {
				label.setText("Dataset name of '"+set.getName()+"' value:");
				final int id = set.open();
				try {
					final String[] value = (String[])set.read();
					buf.append(value[0]);
				} catch (Exception e) {
					// Ignored
				} finally {
					set.close(id);
				}
			} else if (shape!=null) {
				label.setText("Dataset name of '"+set.getName()+"' shape:");
				buf.append(Arrays.toString(shape));
				
				
			} else {
				label.setText("Dataset name of '"+set.getName()+"'");
			}
			appendAttributes(set, buf);
			sourceViewer.getTextWidget().setText(buf.toString());
			
		} if (ob instanceof Group) {
			final Group  grp   = (Group)ob;
			label.setText("Group name of '"+grp.getName()+"' children:");
			final List members = grp.getMemberList();
			
			final StringBuilder buf = (members!=null) ? new StringBuilder(members.toString()) : new StringBuilder();
			appendAttributes(grp, buf);
			sourceViewer.getTextWidget().setText(buf.toString());

		}
	}
	
	private void appendAttributes(HObject set, StringBuilder buf) throws Exception {
		
		final List meta = set.getMetadata();
		if (meta==null || meta.isEmpty()) return;
		
		buf.append("\n\nAttributes:\n");
		for (Object attribute : meta) {
			
			if (attribute instanceof Attribute) {
				Attribute a = (Attribute)attribute;
				buf.append(a.getName());
				buf.append(" = ");
				buf.append(extractValue(a.getValue()));
				buf.append("\n");
			}
		}
	}


	private Object extractValue(Object value) {
		if (value == null) {
			return "";
			
		} else if (value.getClass().isArray()) {
			return toString(value);

		} else {
			return value.toString();
		}
	}

	private static IWorkbenchPage getActivePage() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench == null) return null;
		final IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
		if (window == null) return null;
		return window.getActivePage();
	}
	

	/**
	 * Deals with primitive arrays
	 * @param value
	 */
	private static Object toString(Object value) {
		
		if (value==null) return null;
		
        if (value instanceof short[]) {
        	return Arrays.toString((short[])value);
        	
        } else if  (value instanceof int[]) {
        	return Arrays.toString((int[])value);
        	
        } else if  (value instanceof long[]) {
        	return Arrays.toString((long[])value);
        	
        } else if  (value instanceof char[]) {
        	return Arrays.toString((char[])value);
        	
        } else if  (value instanceof float[]) {
        	return Arrays.toString((float[])value);
        	
        } else if  (value instanceof double[]) {
        	return Arrays.toString((double[])value);
        	
        } else if  (value instanceof boolean[]) {
        	return Arrays.toString((boolean[])value);
        	
        } else if  (value instanceof byte[]) {
        	return Arrays.toString((byte[])value);
        	
        } else if  (value instanceof Object[]) {
        	return Arrays.toString((Object[])value);
        }
        
        return value.toString();
	}

}
