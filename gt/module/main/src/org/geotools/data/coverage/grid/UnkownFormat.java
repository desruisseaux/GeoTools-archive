/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
package org.geotools.data.coverage.grid;

import org.geotools.parameter.ParameterGroupDescriptor;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.OperationParameterGroup;


/**
 * DOCUMENT ME!
 *
*  @author Jesse Eichar
 * @author $author$ (Last Modified)
 * @version $Revision: 1.9 $
 */
public class UnkownFormat implements Format {
    /**
     * Creates a new UnkownFormat object.
     */
    public UnkownFormat() {
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#getName()
     */
    public String getName() {
        return "Unkown Format";
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#getDescription()
     */
    public String getDescription() {
        return "This format describes all unknown formats";
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#getVendor()
     */
    public String getVendor() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#getDocURL()
     */
    public String getDocURL() {
        return null;
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#getVersion()
     */
    public String getVersion() {
        return null;
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#getReadParameters()
     */
    public OperationParameterGroup getReadParameters() {
        return null;
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#getWriteParameters()
     */
    public OperationParameterGroup getWriteParameters() {
        return null;
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#getReader(java.lang.Object)
     */
    public GridCoverageReader getReader(Object source) {
        return null;
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#getWriter(java.lang.Object)
     */
    public GridCoverageWriter getWriter(Object destination) {
        return null;
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#accepts(java.lang.Object)
     */
    public boolean accepts(Object input) {
        return true;
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#equals(org.geotools.data.coverage.grid.Format)
     */
    public boolean equals(Format f) {
        if (f.getClass() == getClass() )
            return true;
        return false;
    }
    
    
}
