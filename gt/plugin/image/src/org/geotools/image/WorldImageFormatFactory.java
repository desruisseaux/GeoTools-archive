/*
 * Created on Jul 22, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.coverage;

import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridFormatFactorySpi;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorldImageFormatFactory implements GridFormatFactorySpi {

	public Format createFormat() {
		return new WorldImageFormat();
	}
	public boolean isAvailable() {
		return true;
	}

}
