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
package org.geotools.data.arcgrid;

import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.data.coverage.grid.stream.IOExchange;
import org.geotools.gc.GridCoverage;
import org.geotools.parameter.ParameterValue;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.geotools.data.coverage.grid.Format;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;

import java.io.IOException;
import java.io.PrintWriter;


/**
 * ArcGridWriter Supports writing of an ArcGrid GridCoverage to an Desination
 * object, provided the desination object can be converted to a PrintWriter
 * with the IOExchange
 *
 * @author jeichar
 */
public class ArcGridWriter implements GridCoverageWriter {
    /** the destination object where we will do the writing */
    private Object destination;

    ArcGridReader.InternalParam params=new ArcGridReader.InternalParam();

    transient ArcGridRaster arcGridRaster;
    transient PrintWriter mWriter;

    /**
     * an IOExchange used to figure out the the Writer class to retrieve from
     * destination
     */
    private IOExchange ioexchange;

    /**
     * the name of the grid to be written, this is not particularily meaningful
     * in ArcGrid
     */
    private String name;

    /**
     * Takes either a URL or a String that points to an ArcGridCoverage file
     * and converts it to a URL that can then be written to.
     *
     * @param destination the URL or String pointing to the file to load the
     *        ArcGrid
     *
     */
    public ArcGridWriter(Object destination) {
        this.destination = destination;
        ioexchange = IOExchange.getIOExchange();
    }

    /**
     * Implementation of getMetadataNames.  
     * Currently unimplemented because it has not been specified where to retrieve
     * the metadata
     *
     * @return
     *      null
     * @see org.opengis.coverage.grid.GridCoverageWriter#getMetadataNames()
     */
    public String[] getMetadataNames() {
        return null;
    }

    /**
     * Creates a Format object describing the Arc Grid Format
     *
     * @return the format of the data source we will write to. (ArcGridFormat
     *         in this case)
     *
     * @see org.opengis.coverage.grid.GridCoverageWriter#getFormat()
     */
    public Format getFormat() {
        return (new ArcGridFormatFactory()).createFormat();
    }

    /**
     * Returns the destination object passed to it by the GridCoverageExchange
     *
     * @return the destination that this writer is configured to write to.
     *
     * @see org.opengis.coverage.grid.GridCoverageWriter#getDestination()
     */
    public Object getDestination() {
        return destination;
    }

    /**
     * Sets up the object's environment based on the Parameters passed to it by
     * the client
     *
     * @param parameters The parameters from a write() or add() method
     *
     * @throws InvalidParameterNameException Thrown if a parameter was passed
     *         to the reader that is not expected
     * @throws InvalidParameterValueException Thrown if a boolean value is not
     *         valid for the parameter passed
     * @throws IOException 
     *          Thrown for any other unexpected exception
     */
    private void setEnvironment(ParameterValue[] parameters)
        throws InvalidParameterNameException, InvalidParameterValueException, 
            IOException {
        if (parameters == null) {
            parameters=new ParameterValue[0];
        }


        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] instanceof ParameterValueGroup) {
                GeneralParameterValue[] paramValues = ((ParameterValueGroup) parameters[i]).getValues();
                for (int j=0; j < paramValues.length; j++) {
                    ArcGridReader.parseParameter((ParameterValue) paramValues[j], params);
                }
            } else {
                ArcGridReader.parseParameter((ParameterValue) parameters[i], params);
            }
        }

        if( params.compress )
            mWriter=ioexchange.getGZIPPrintWriter(destination);
        else
            mWriter = ioexchange.getPrintWriter(destination);

    }


    /**
     * This method was copied from ArcGridData source
     *
     * @param gc the grid coverage that will be written to the destination
     *
     * @throws DataSourceException indicates an unexpected exception
     *
     * @see ArcGridDataSource#setFeatures(FeatureCollection)
     */
    private void writeGridCoverage(GridCoverage gc) throws DataSourceException {
        java.awt.image.Raster data = gc.getRenderedImage().getData();
        org.geotools.pt.Envelope bounds = gc.getGridGeometry().getEnvelope();
        double xl = bounds.getMinimum(0);
        double yl = bounds.getMinimum(1);
        double cellsize = Math.max((bounds.getMaximum(0) - xl) / data.getWidth(),
                (bounds.getMaximum(1) - yl) / data.getHeight());

        try {
            if (params.GRASSFormatEnabled) {
                arcGridRaster = new GRASSArcGridRaster(mWriter);
            } else {
                arcGridRaster = new ArcGridRaster(mWriter);
            }

            arcGridRaster.writeRaster(data, xl, yl, cellsize, params.compress);
        } catch (java.io.IOException ioe) {
            throw new DataSourceException("IOError writing", ioe);
        }
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String,
     *      java.lang.String)
     */
    public void setMetadataValue(String name, String value)
        throws IOException, MetadataNameNotFoundException {
        // Metadata has not been handled at this point ie there is not spec on
        // where it should be written
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
     */
    public void setCurrentSubname(String name) throws IOException {
        this.name = name;
    }

    /**
     * Note: The geotools GridCoverage class does not implement the geoAPI
     * GridCoverage Interface so this method shows an error. All other methods
     * are using the geotools GridCoverage class
     *
     * @see org.opengis.coverage.grid.GridCoverageWriter#write(org.opengis.coverage.grid.GridCoverage,
     *      org.opengis.parameter.GeneralParameterValue[])
     */
    public void write(GridCoverage coverage,
        GeneralParameterValue[] parameters)
        throws InvalidParameterNameException, InvalidParameterValueException, 
            ParameterNotFoundException, IOException {
        setEnvironment((ParameterValue[]) parameters);
        writeGridCoverage(coverage);
        mWriter.close();
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#dispose()
     */
    public void dispose() throws IOException {
        if (mWriter != null) {
            mWriter.close();
        }
    }
}
