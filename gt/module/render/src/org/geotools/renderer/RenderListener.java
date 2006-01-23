/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2002, Geotools Project Managment
 * Committee (PMC) This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * version 2.1 of the License. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.geotools.renderer;

import org.geotools.feature.Feature;

/**
 * A RenderListener is notified each time a feature is rendered and each time
 * an error occurs during rendering.  Therefore <b>VERY LITTLE WORK</b> should be done in the listener!!!
 * 
 * @author jeichar
 * @source $URL$
 */
public interface RenderListener {
    public void featureRenderer( Feature feature );
    public void errorOccurred( Exception e);
}
