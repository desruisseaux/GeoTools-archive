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

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.stream.IOExchange;
import org.geotools.gc.GridCoverage;
import org.geotools.parameter.ParameterDescriptor;
import org.geotools.parameter.ParameterGroupDescriptor;
import org.geotools.parameter.ParameterValue;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.geotools.data.coverage.grid.Format;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.InvalidParameterNameException;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.OperationParameter;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;

/**
 * This class can read a arc grid data source and create a grid coverage from
 * the data.
 * 
 * @author jeichar
 */
public class ArcGridReader implements GridCoverageReader {
    private Object mSource;

    private Reader mReader;

    boolean compress = false;
    boolean GRASS = false;
    /*
    static class InternalParam{
        boolean compress = false;
        boolean GRASSFormatEnabled = false;
    }    
    private InternalParam params=new InternalParam();
    */
    private IOExchange mExchange = IOExchange.getIOExchange();

    /** Default color ramp */
    private Color[] demColors = new Color[] { Color.BLUE, Color.WHITE,
            Color.RED };

    /** The coordinate system associated to the returned GridCoverage */
    private CoordinateSystem coordinateSystem = GeographicCoordinateSystem.WGS84;

    /** The grid coverage read from the data file */
    private java.lang.ref.SoftReference gridCoverage = null;

    /** The raster read from the data file */
    private ArcGridRaster arcGridRaster = null;

    private String name;

    /**
     * Creates a new instance of an ArcGridReader
     * 
     * @param aSource
     *            URL referencing the location of the Arc Grid Data
     */
    public ArcGridReader(Object aSource) {
        mSource = aSource;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataNames()
     */
    public String[] getMetadataNames() throws IOException {
        // Metadata has not been handled at this point ie there is not spec on
        // where it should be obtained
        return null;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
     */
    public String getMetadataValue(String name) throws IOException,
            MetadataNameNotFoundException {
        throw new MetadataNameNotFoundException(name
                + " is not a valid metadata name for ArcGridReader");
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#listSubNames()
     */
    public String[] listSubNames() throws IOException {
        return null;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
     */
    public Format getFormat() {
        return (new ArcGridFormatFactory()).createFormat();
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getSource()
     */
    public Object getSource() {
        return mSource;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getCurrentSubname()
     */
    public String getCurrentSubname() throws IOException {
        return null;
    }

    /**
     * Note: The geotools GridCoverage does not implement the geoapi
     * GridCoverage Interface so this method shows an error. All other methods
     * are using the geotools GridCoverage class
     * 
     * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
     */
    public GridCoverage read( ParameterValueGroup params )
            throws InvalidParameterNameException,
            InvalidParameterValueException, ParameterNotFoundException,
            IOException {
        setEnvironment("ArcGrid", params );

        return getGridCoverage();
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#dispose()
     */
    public void dispose() throws IOException {
        if (mReader != null) {
            mReader.close();
        }
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getGridCoverageCount()
     */
    public int getGridCoverageCount() {
        return 0;
    }

    /**
     * Sets up the object's environment based on the Parameters passed to it by
     * the client
     * 
     * @param name
     *            A name for the gridCoverage
     * @param parameters
     *            The parameters from a read() method
     * 
     * @throws InvalidParameterNameException
     *             Thrown if a parameter was passed to the reader that is not
     *             expected
     * @throws InvalidParameterValueException
     *             Thrown if a boolean value is not valid for the parameter
     *             passed
     * @throws IOException
     *             Thrown for any other unexpected exception
     */
    private void setEnvironment(String name, ParameterValueGroup parameters)
            throws InvalidParameterNameException,
            InvalidParameterValueException, IOException {
        this.name = name;
        if (parameters == null ){
            compress = false;
            GRASS = false;
        }
        else {
            compress = parameters.getValue( "Compress" ).booleanValue();
            GRASS = parameters.getValue( "GRASS" ).booleanValue();
        }              
        if ( compress )
            mReader= mExchange.getGZIPReader(mSource);
        else
            mReader = mExchange.getReader(mSource);

    }
    boolean parseBoolean( ParameterValueGroup params, String name ){
        ParameterGroupDescriptor info = getFormat().getReadParameters();        
        if( params == null ){
            throw new InvalidParameterValueException(
                    "A Parameter group was expected",
                    null, null );            
        }
        OperationParameter targetInfo = info.getParameter( name );
        if( targetInfo == null ){
            throw new InvalidParameterNameException( name, "Not a ArcGrid paramerter" );
        }
        org.opengis.parameter.ParameterValue target = params.getValue( name );
        if (target == null ){
            throw new InvalidParameterValueException(
                    "Parameter "+name+ "is requried",
                    null, null ); 
        }
        return target.booleanValue();        
    }
    /*
    static void parseParameter(ParameterValue parameter, 
            InternalParam params)
            throws InvalidParameterNameException,
            InvalidParameterValueException {
        try {
            if (parameter.getDescriptor().getName(null).equals(
                    "GRASSFormatEnabled")) {

                params.GRASSFormatEnabled = parameter.booleanValue();

                return;
            } else if (parameter.getDescriptor().getName(null).equals(
                    "Compressed")) {
                params.compress = parameter.booleanValue();
                return;
            }
        } catch (InvalidParameterTypeException e) {
            throw new InvalidParameterValueException(
                    "A Boolean value was expected", parameter.getDescriptor()
                            .getName(null), 0);
        }

        throw new InvalidParameterNameException(parameter.getDescriptor()
                .getName(null)
                + " is not a valid parameter for ArcGrid", null);
    }
    */
    
    /**
     * Returns the ArcGridRaster read by the datasource. Use it only for
     * specific needs, it's not a datasource independent method.
     * 
     * @return the ArcGridRaster read by the datasource
     * 
     * @throws java.io.IOException
     *             Thrown in the case of an unexpected exception
     */
    public ArcGridRaster openArcGridRaster() throws java.io.IOException {
        if (arcGridRaster == null) {
            try {
                if (GRASS) {
                    arcGridRaster = new GRASSArcGridRaster(mReader, compress);
                } else {
                    arcGridRaster = new ArcGridRaster(mReader, compress);
                }
            } catch (Exception e) {
                throw new DataSourceException("Unexpected exception", e);
            }
        }

        return arcGridRaster;
    }

    /**
     * Returns the GridCoverage read by the datasource. Use it if you want to
     * avoid unpacking the getFeatures returned feature collection. Use only for
     * specific needs, it's not a datasource independent method.
     * 
     * @return the GridCoverage read by the datasource
     * 
     * @throws java.io.IOException
     *             Thrown in the case of an unexpected exception
     */
    private GridCoverage getGridCoverage() throws java.io.IOException {
        if ((gridCoverage == null) || (gridCoverage.get() == null)) {
            gridCoverage = new java.lang.ref.SoftReference(createCoverage());
        }

        return (GridCoverage) gridCoverage.get();
    }

    private GridCoverage createCoverage() throws java.io.IOException {
        java.awt.image.WritableRaster raster = null;
        raster = openArcGridRaster().readRaster();

        double[] min = new double[] { arcGridRaster.getMinValue() };
        double[] max = new double[] { arcGridRaster.getMaxValue() };
        CoordinateSystem coordinateSystem = getCoordinateSystem();

        if (coordinateSystem == null) {
            coordinateSystem = GeographicCoordinateSystem.WGS84;
        }

        return new GridCoverage(name, raster, coordinateSystem,
                convertEnvelope(getBounds()), min, max, null,
                new Color[][] { getColors() }, null);
    }

    /**
     * Gets the coordinate system that will be associated to the GridCoverage.
     * The WGS84 coordinate system is used by default
     * 
     * @return the coordinate system for GridCoverage creation
     */
    private CoordinateSystem getCoordinateSystem() {
        return this.coordinateSystem;
    }

    /**
     * Gets the bounding box of this datasource using the default speed of this
     * datasource as set by the implementer.
     * 
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     */
    private Envelope getBounds() {
        com.vividsolutions.jts.geom.Envelope env = null;
        double xmin = arcGridRaster.getXlCorner();
        double ymin = arcGridRaster.getYlCorner();
        double xmax = xmin
                + (arcGridRaster.getNCols() * arcGridRaster.getCellSize());
        double ymax = ymin
                + (arcGridRaster.getNRows() * arcGridRaster.getCellSize());
        env = new com.vividsolutions.jts.geom.Envelope(xmin, xmax, ymin, ymax);

        return env;
    }

    /**
     * Gets the default color ramp used to depict the GridCoverage
     * 
     * @return the color ramp
     */
    private Color[] getColors() {
        return demColors;
    }

    /**
     * Converts a JTS Envelope into an org.geotools.pt.Envelope
     * 
     * @param source
     *            the jts envelope
     * 
     * @return the equivalent geotools envelope
     */
    private org.geotools.pt.Envelope convertEnvelope(
            com.vividsolutions.jts.geom.Envelope source) {
        double[] min = new double[] { source.getMinX(), source.getMinY() };
        double[] max = new double[] { source.getMaxX(), source.getMaxY() };

        return new org.geotools.pt.Envelope(min, max);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.coverage.grid.GridCoverageReader#hasMoreGridCoverages()
     */
    public boolean hasMoreGridCoverages() throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opengis.coverage.grid.GridCoverageReader#skip()
     */
    public void skip() throws IOException {
        // TODO Auto-generated method stub

    }
}