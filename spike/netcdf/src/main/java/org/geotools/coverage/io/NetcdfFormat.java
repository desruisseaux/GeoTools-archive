/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Geomatys
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
package org.geotools.coverage.io;

// J2SE dependencies
import java.util.HashMap;

// OpenGIS dependencies
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;


/**
 * Description of NetCDF format.
 *
 * @author Cédric Briançon
 */
public class NetcdfFormat extends AbstractGridFormat implements Format {
    /**
     * Creates a new instance of NetcdfFormat.
     * Contains the main information about the NetCDF format.
     */
    public NetcdfFormat() {
        writeParameters = null;
        mInfo = new HashMap();
        mInfo.put("name", "NetCDF");
        mInfo.put("description", "NetCDF reader");
        mInfo.put("vendor", "Geomatys");
        mInfo.put("version", "1.0.0");
        mInfo.put("docURL", "http://ftp.unidata.ucar.edu/software/netcdf-java/v2.2.16/javadoc/index.html");
        readParameters = new ParameterGroup(
                new DefaultParameterDescriptorGroup(mInfo,
                new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D }));
    }

    /**
     * Gets a reader for the netCDF file specified.
     *
     * @param object May be a netCDF file, or an URL for a netCDF file.
     *
     * @deprecated
     */
    public GridCoverageReader getReader(final Object object) {
        return getReader(object, null);
    }

    /**
     * Gets a reader for the netCDF file specified.
     *
     * @param object May be a netCDF file, or an URL for a netCDF file.
     */
    public GridCoverageReader getReader(final Object object, final Hints hints) {
        try {
            return new NetcdfReader(this, object, null);
        } catch (DataSourceException ex) {
            throw new RuntimeException(ex); // TODO: trouver une meilleur exception.
        }
    }

    /**
     * Gets a writer for the netCDF file.
     * Not used in our implementation.
     * @param object The source in which we will write.
     */
    public GridCoverageWriter getWriter(Object object) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Specifies if the source is a netCDF file, and by the way is available.
     *
     * @param object The source to test.
     *
     * @todo Not yet implemented (previous implementation was useless).
     */
    public boolean accepts(Object object) {
        return true;
    }
}
