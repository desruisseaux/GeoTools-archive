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

import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import java.util.HashMap;
/**
 * DOCUMENT ME!
 *
*  @author Jesse Eichar
 * @author $author$ (Last Modified)
 * @author <a href="mailto:simboss_ml@tiscali.it">Simone Giannecchini (simboss)</a>
 * @version $Revision: 1.9 $
 */
public class UnknownFormat extends AbstractGridFormat implements Format {
    /**
     * Creates a new UnknownFormat object.
     */
    public UnknownFormat() {
            mInfo= new HashMap();
            mInfo.put("name", "Unkown Format");
            mInfo.put("description", "This format describes all unknown formats");
            mInfo.put("vendor", null);
            mInfo.put("docURL", null);
            mInfo.put("version",null);
            readParameters = null;
            writeParameters = null;

    }



    /**
     * @see org.geotools.data.coverage.grid.Format#getReader()
     */
    public GridCoverageReader getReader(java.lang.Object source) {
        return null;
    }

    /**
     * @see org.geotools.data.coverage.grid.Format#getWriter()
     */
    public GridCoverageWriter getWriter(Object destination) {
        return null;
    }


    /**
     * @see org.geotools.data.coverage.grid.Format#accepts(java.lang.Object)
     */
    public boolean accepts(Object input) {
        return false;
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
