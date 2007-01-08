/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data;

import java.net.URL;

/**
 * 
 * 
 * @author Thomas Marti
 * @author Stefan Schmid
 * 
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/module/render/src/org/geotools/renderer/lite/StreamingRenderer.java $
 * @version $Id: StreamingRenderer.java 23200 2006-12-04 21:25:42Z jgarnett $
 */

public class JpoxConnectionInfo {
	private URL url;
	
	public URL getUrl() {
		return url;
	}
	
	public void setUrl( URL url ) {
		this.url = url;
	}
}
