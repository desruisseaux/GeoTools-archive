/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, 2004 Geotools Project Managment Committee (PMC)
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
package org.geotools.gce.image;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.GeographicCRS;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.OperationNotFoundException;
import org.opengis.spatialschema.geometry.Envelope;
import org.geotools.parameter.Parameter;

/**
 * @author rgould
 * @author simone giannecchini (simboss_ml@tiscali.it)
 * @author alessio fabiani (alessio.fabiani@gmail.com)
 *
 * Reads a GridCoverage from a given source. WorldImage sources
 * only support one GridCoverage so hasMoreGridCoverages() will
 * return true until the only GridCoverage is read.
 *
 * No metadata is currently supported, so all those methods return null.
 */
public class WorldImageReader implements GridCoverageReader {

    public static int WORLD_WLD = 1;
    public static int WORLD_META = 2;
    public static int WORLD_BASE = 3;

    /**Format for this reader*/
    private Format format=new WorldImageFormat();
    /*Source to read from*/
    private Object source;

    private boolean gridLeft = true;

    /**
     * Construct a new ImageWorldReader to read a GridCoverage from the
     * source object. The source must point to the raster file itself,
     * not the world file.
     *
     * @param source The source of a GridCoverage
     */
	public WorldImageReader (Object source) {
		this.source = source;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageReader#getFormat()
	 */
	/**
	 * Returns the format that this Reader accepts.
	 * @return a new WorldImageFormat class
	 */
	public Format getFormat() {
		return this.format;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageReader#getSource()
	 */
	/**
	 * Returns the source object containing the GridCoverage. Note that it
	 * points to the raster, and not the world file.
	 * @return the source object containing the GridCoverage.
	 */
	public Object getSource() {
		return source;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageReader#getMetadataNames()
	 */
	/**
	 * Metadata is not suported. Returns null.
	 */
	public String[] getMetadataNames() throws IOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
	 */
	/**
	 * Metadata is not supported. Returns null.
	 */
	public String getMetadataValue(String name) throws IOException,
			MetadataNameNotFoundException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageReader#listSubNames()
	 */
	/**
	 * WorldImage GridCoverages are not named. Returns null.
	 */
	public String[] listSubNames() throws IOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageReader#getCurrentSubname()
	 */
	/**
	 * WorldImage GridCoverages are not named. Returns null.
	 */
	public String getCurrentSubname() throws IOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageReader#hasMoreGridCoverages()
	 */
	/**
	 * Returns true until read has been called, as World Image files only
	 * support one GridCoverage.
	 */
	public boolean hasMoreGridCoverages() throws IOException {
		return gridLeft;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
	 */
	/**Reads an image from a source stream.
         *
	 * Loads an image from a source stream, then loads the values from the world file
	 * and constructs a new GridCoverage from this information.
	 *
	 * When reading from a remote stream we do not look for a world fiel but we suppose those information comes from
         * a different way (xml, gml, pigeon?)
	 *
	 * @param parameters WorldImageReader supports no parameters, it just ignores them.
	 * @return a new GridCoverage read from the source.
	 */
	public GridCoverage read( GeneralParameterValue[] parameters)
			throws IllegalArgumentException, IOException {

        //do we have paramters to use for reading from the specified source
        if(parameters!=null)
        {
            //they will be ignored if we will find a world file
            this.format.getReadParameters().parameter("crs").setValue(((Parameter)
                    parameters[0]).getValue());
            this.format.getReadParameters().parameter("envelope").setValue(((Parameter)
                    parameters[1]).getValue());
        }
        URL sourceURL = null;

        double xMin = 0.0;
        double yMax = 0.0;
        double xMax = 0.0;
        double yMin = 0.0;
        float xPixelSize = 0;
        float rotation1 = 0;
        float rotation2 = 0;
        float yPixelSize = 0;
        float xLoc = 0;
        float yLoc = 0;
        int world_type = -1;
        BufferedImage image = null;
        CoordinateReferenceSystem crs = GeographicCRS.WGS84;
        Envelope envelope = null;

        //are we reading from a file?
        //in such a case we will look for the associated world file
        if (source instanceof File)
        {


            sourceURL = ((File) source).toURL();

            String sourceAsString = sourceURL.toExternalForm();
            int index = sourceAsString.lastIndexOf(".");
            String base = sourceAsString.substring(0, index);
            String fileExtension = sourceAsString.substring(index);

            //We can now construct the baseURL from this string.



            BufferedReader in = null;


            try {
                URL worldURL = new URL(base + ".wld");
                in = new BufferedReader(new InputStreamReader(worldURL.
                        openStream()));
                world_type = WORLD_WLD;
            } catch (FileNotFoundException e1) {
                try {
                    //.wld extension not found, go for .meta.
                    URL worldURL = new URL(base + ".meta");
                    in = new BufferedReader(new InputStreamReader(worldURL.
                            openStream()));
                    world_type = WORLD_META;
                } catch (FileNotFoundException e2) {
                    //.wld & .meta extension not found, go for file based one.
                    URL worldURL = new URL(base +
                                           WorldImageFormat.
                                           getWorldExtension(fileExtension));
                    in = new BufferedReader(new InputStreamReader(worldURL.
                            openStream()));
                    world_type = WORLD_BASE;
                }
            }

            String str;
            index = 0;

            while ((str = in.readLine()) != null) {

                if (world_type == WORLD_WLD || world_type == WORLD_BASE) {
                    float value = Float.parseFloat(str.trim());
                    switch (index) {
                    case 0:
                        xPixelSize = value;
                        break;
                    case 1:
                        rotation1 = value;
                        break;
                    case 2:
                        rotation2 = value;
                        break;
                    case 3:
                        yPixelSize = value;
                        break;
                    case 4:
                        xLoc = value;
                        break;
                    case 5:
                        yLoc = value;
                        break;
                    default:
                        break;
                    }
                } else if (world_type == WORLD_META) {
                    String line = str;
                    rotation1 = 0.0f;
                    rotation2 = 0.0f;
                    double value;
                    switch (index) {
                    case 1:
                        value = Double.parseDouble(line.substring(
                                "Origin Longitude = ".length()));
                        xMin = value;
                        break;
                    case 2:
                        value = Double.parseDouble(line.substring(
                                "Origin Latitude = ".length()));
                        yMin = value;
                        break;
                    case 3:
                        value = Double.parseDouble(line.substring(
                                "Corner Longitude = ".length()));
                        xMax = value;
                        break;
                    case 4:
                        value = Double.parseDouble(line.substring(
                                "Corner Latitude = ".length()));
                        yMax = value;
                        break;
                    default:
                        break;
                    }
                }
                index++;
            }
            in.close();

            //building up envelope
            if (world_type == WORLD_WLD || world_type == WORLD_BASE) {
                xMin = xLoc;
                yMax = yLoc;
                xMax = xLoc + (image.getWidth() * xPixelSize);
                yMin = yLoc + (image.getHeight() * yPixelSize);
            }

            envelope = new GeneralEnvelope(new double[] {xMin, yMin},
                                           new double[] {xMax, yMax});

        }
        else {

            //well it seems we are not reading from a file
            //therefore we need the parameters
            //if(parameters==null)
                //throw new IllegalArgumentException("To read froma a source which is not a file please provided read parameters!");

            sourceURL = (URL) source;
            //getting crs
            crs = (CoordinateReferenceSystem) this.format.getReadParameters().parameter("crs").
                  getValue();
            //getting envelope
            envelope = (Envelope)  this.format.getReadParameters().parameter("envelope").getValue();

            if(envelope==null|| crs==null)
                throw new IllegalArgumentException("To read froma a source which is not a file please provided read parameters!");

        }


        //reading the image as given
        image = ImageIO.read(sourceURL);


        //no more grid left
        gridLeft = false;

        //building up a coverage
        GridCoverage coverage = null;
        try {
            coverage = new GridCoverage2D(
                    sourceURL.getFile(), image, crs, envelope);
        }
        catch (NoSuchElementException e1) {

            throw new IOException(e1.getMessage());

        }
        return coverage;
  }

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageReader#skip()
	 */
	/**
	 * Not supported, does nothing.
	 */
	public void skip() throws IOException {

	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageReader#dispose()
	 */
	/**
	 * Cleans up the Reader (currently does nothing)
	 */
	public void dispose() throws IOException {

	}

}
