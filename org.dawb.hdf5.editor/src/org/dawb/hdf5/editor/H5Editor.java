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

import javax.swing.tree.TreeNode;

import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H5Editor extends EditorPart implements IReusableEditor, IH5Editor {

	public static final String ID = "org.dawb.hdf5.editor.raw.tree";
	
    private static final Logger logger = LoggerFactory.getLogger(H5Editor.class);
	
	private IHierarchicalDataFile  file;
	private TreeViewer tree;
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		
		setSite(site);
		super.setInput(input);
        setPartName(input.getName());
        try {
        	// More than one thr
			this.file = HierarchicalDataFactory.getReader(getFilePath(input));
		} catch (Exception e) {
			logger.error("Cannot open h5 file "+getFilePath(input), e);
		}
	}

	@Override
	public void setInput(final IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		
		try {
			file.close();
		} catch (Exception e) {
			logger.error("Cannot close file "+file.getPath());
		}
		
		try {
        	// More than one thr
			this.file = HierarchicalDataFactory.getReader(getFilePath(input));
		} catch (Exception e) {
			logger.error("Cannot open h5 file "+getFilePath(input), e);
		}
		refresh();
	}	

	/**
	 * Get the file path from a FileStoreEditorInput. Removes any "file:"
	 * from the URI to the file path if it exists.
	 * 
	 * @param fileInput
	 * @return String
	 */
	public String getFilePath(IEditorInput fileInput) {
		if (fileInput instanceof IURIEditorInput) {
			String path = ((IURIEditorInput)fileInput).getURI().toString();
			if (path.startsWith("file:")) path = path.substring(5);
			path = path.replace("%20", " ");
			return path;
		} 
		return null;
	}

	
	@Override
	public void createPartControl(final Composite parent) {
		
		parent.setLayout(new GridLayout(1, false));

//      TODO Searching not possible as tree is VIRTUAL and ILazy
//		final Text searchText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
//		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
//		searchText.setToolTipText("Search on data set name or expression value." );

		tree = new TreeViewer(parent, SWT.BORDER|SWT.VIRTUAL);
		tree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.getTree().setHeaderVisible(true);
		tree.setUseHashlookup(true);

		final String[] titles = { "Name", "Class", "Dims", "Description", "Size" };
		final int[]    widths = { 250, 120, 80, 150, 150 };
		TreeViewerColumn tVCol;
		for (int i = 0; i < titles.length; i++) {
			tVCol = new TreeViewerColumn(tree, SWT.NONE);
			TreeColumn tCol = tVCol.getColumn();
			tCol.setText(titles[i]);
			tCol.setWidth(widths[i]);
			tCol.setMoveable(true);
		}
		getSite().setSelectionProvider(tree);
		
//		final H5Filter filter = new H5Filter();
//		searchText.addModifyListener(new ModifyListener() {		
//			@Override
//			public void modifyText(ModifyEvent e) {
//				if (parent.isDisposed()) return;
//				filter.setSearchText(searchText.getText());
//				refresh();
//			}
//		});
	}
	
	@Override
	public void dispose() {
		try {
		     super.dispose();
		} finally {
			try {
				file.close();
			} catch (Exception e) {
				logger.error("Cannot close file "+file.getPath());
			}
		}
	}

	@Override
	public void setFocus() {
		
		// We only give content when the UI is actually selected
		// as this makes the system more lazy when this editor is
		// used on a multi editor as a second or greater tab.
		if (tree.getContentProvider()==null) {
			refresh();
		}
		
		tree.getControl().setFocus();
	}

	private void refresh() {
		
		final TreeNode root = file.getNode();
		tree.getTree().setItemCount(root.getChildCount());
		tree.setUseHashlookup(true);
		tree.setContentProvider(new H5ContentProvider());
		tree.setLabelProvider(new H5LabelProvider());
		tree.setInput(root);
		tree.expandToLevel(1);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		
	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

    /**
     * The adapter IContentProvider gives the value of the H5Dataset
     */
	public Object getAdapter(final Class clazz) {

		if (clazz == IContentProvider.class) {
			return new H5ValuePage();
		}

		return super.getAdapter(clazz);
	}
	
	@Override
	public String getFilePath() {
		return getFilePath(getEditorInput());
	}
}
