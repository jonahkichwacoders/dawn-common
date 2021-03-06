package org.dawb.common.ui.svg;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Translatable;
import org.eclipse.swt.widgets.Display;


/**
 * This class implements the MapMode interface to provide support for
 * HiMetric values.  HIMetric values are represented as 1/100 of an inch.
 * 
 * @author jschofie sshaw
 */
public class HiMetricMapMode
	implements IMapMode {

	private float dpi;
	private double scale = 1;
	private static final double UNITS_PER_INCH = 2540.0;

	public HiMetricMapMode() {
		/* This constructor is fragile and purposely does not 
		 * use DisplayUtils.getDisplay() */ 
		Display display = Display.getCurrent();
		if (display == null) {
			Display.getDefault().syncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					dpi = Display.getCurrent().getDPI().x;
				}
			});
		} else {
			dpi = display.getDPI().x;
		}
		scale = dpi / UNITS_PER_INCH;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.gef.ui.internal.figure.surface.mapmode.IMapMode#LPtoDP(int)
	 */
	public int LPtoDP(int logicalUnit) {
		Point devPt = new Point(logicalUnit, 0);
		devPt.performScale( scale );
    	return devPt.x;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.gef.ui.internal.figure.surface.mapmode.IMapMode#DPtoLP(int)
	 */
	public int DPtoLP(int deviceUnit) {
		Point logPt = new Point(deviceUnit, 0);
		logPt.performScale( 1 / scale );
		return logPt.x;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode#DPtoLP(org.eclipse.draw2d.geometry.Translatable)
	 */
	public Translatable DPtoLP(Translatable t) {
		t.performScale( 1 / scale );
		return t;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode#LPtoDP(org.eclipse.draw2d.geometry.Translatable)
	 */
	public Translatable LPtoDP(Translatable t) {
		t.performScale( scale );
		return t;
	}

}