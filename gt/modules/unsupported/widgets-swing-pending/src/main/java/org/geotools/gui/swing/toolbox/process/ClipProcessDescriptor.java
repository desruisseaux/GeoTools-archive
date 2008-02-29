/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.toolbox.process;

import java.util.Map;
import org.geotools.gui.swing.toolbox.Parameter;
import org.opengis.util.ProgressListener;

/**
 *
 * @author johann sorel
 */
public class ClipProcessDescriptor implements ProcessDescriptor{

    public String getTitle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isValid(Map parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Process createProcess(Map parameters) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Process create(Map parameters, ProgressListener monitor) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Parameter[] getParametersInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
