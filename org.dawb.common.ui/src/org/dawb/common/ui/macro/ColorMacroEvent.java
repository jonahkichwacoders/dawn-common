package org.dawb.common.ui.macro;

import org.eclipse.dawnsci.macro.api.MethodEventObject;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.swt.graphics.Color;

public class ColorMacroEvent extends MethodEventObject<Color>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2659758628871704094L;


	public ColorMacroEvent(String varName, Object source, Color... args) {
		super(varName, Thread.currentThread().getStackTrace(), source, args);
	}
	public ColorMacroEvent(String varName, String methodName, Object source, Color... args) {
		super(varName, methodName, source, args);
	}

	protected String createPythonCommand(String varName, String methodName, Object source, Color... args) {
		
		if (args==null || args.length<1) {
			throw new RuntimeException("No colors passed to "+getClass().getSimpleName());
		}
		
		StringBuilder buf = new StringBuilder();
		
		for (int i = 1; i <= args.length; i++) {
			Color col = args[i-1];
			buf.append("color");
			buf.append(i);
			buf.append(" = dnp.plot.createColor(");
			buf.append(col.getRed());
			buf.append(", ");
			buf.append(col.getGreen());
			buf.append(", ");
			buf.append(col.getBlue());
			buf.append(")\n");
		}
				
		// Make method call
		buf.append(varName);
		buf.append(".");
		
		if (source instanceof IAxis) {
			IAxis axis = (IAxis)source;
			String toAdd = null;
        	if (axis.isPrimaryAxis()) {
        		toAdd = axis.isYAxis() ? "getSelectedYAxis()."
        				               : "getSelectedXAxis().";
        		
        	} else if (axis.getTitle()!=null && !"".equals(axis.getTitle())) {
        		toAdd = "getAxis(\""+axis.getTitle()+"\").";
        	}
            if (toAdd!=null) buf.append(toAdd);	
		}
		
		buf.append(methodName);
		buf.append("(");
		for (int i = 1; i <= args.length; i++) {
			buf.append("color");
			buf.append(i);
			if (i<args.length) buf.append(", ");
		}
		buf.append(")");
		
		return buf.toString();
	}

}
