package org.dawb.common.ui.plot.annotation;

import org.dawb.common.ui.plot.IPlottingSystem;

public class AnnotationUtils {

	/**
	 * Call to get a unique annotation name 
	 * @param nameStub
	 * @param system
	 * @return
	 */
	public static String getUniqueAnnotation(final String nameStub, final IPlottingSystem system) {
		int i = 1;
		while(system.getAnnotation(nameStub+" "+i)!=null) {
			++i;
			if (i>10000) break; // something went wrong!
		}
		return nameStub+" "+i;
	}

}
