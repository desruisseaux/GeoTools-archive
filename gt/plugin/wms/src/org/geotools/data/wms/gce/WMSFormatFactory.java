/*
 * Created on Jul 22, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.gce;

import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridFormatFactorySpi;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSFormatFactory implements GridFormatFactorySpi {

	public Format createFormat() {
		return new WMSFormat();
	}

	public boolean isAvailable() {
		return true;
	}

}
