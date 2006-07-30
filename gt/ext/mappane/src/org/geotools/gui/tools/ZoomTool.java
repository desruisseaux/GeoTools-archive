/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.gui.tools;

/**
 * A tool which provides methods for zooming.
 *
 * @author Cameron Shorter
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/module/migrate/src/org/geotools/gui/tools/ZoomTool.java $
 * @version $Id$
 */
public interface ZoomTool extends Tool {
    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in, zoomFactor=2
     * means zoom out. Defaults to 2.
     * @param zoomFactor the factor to zoom by.
     */
    void setZoomFactor(double zoomFactor);

    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in, zoomFactor=2
     * means zoom out. Defaults to 2.
     *
     * @return the factor to zoom by.
     */
    double getZoomFactor();
}
