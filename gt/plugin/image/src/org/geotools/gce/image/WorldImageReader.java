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

import javax.imageio.ImageIO;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * @author rgould
 *
 * Reads a GridCoverage from a given source. WorldImage sources
 * only support one GridCoverage so hasMoreGridCoverages() will 
 * return true until the only GridCoverage is read.
 * 
 * No metadata is currently supported, so all those methods return null.
 */
public class WorldImageReader implements GridCoverageReader {

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
		return new WorldImageFormat();
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
	/**
	 * Reads an image from the source, then reads the values from the world file
	 * and constructs a new GridCoverage from this information.
	 * 
	 * If it cannot find a world file, it will throw a FileNotFoundException.
	 * 
	 * @param parameters WorldImage supports no parameters, it ignores this param
	 * @return a new GridCoverage read from the source
	 */
	public GridCoverage read( ParameterValueGroup parameters)
			throws IllegalArgumentException, IOException {
		
		URL sourceURL = null;
		if (source instanceof File) {
			sourceURL = ((File) source).toURL();
		} else {
			sourceURL = (URL) source;
		}

		String sourceAsString = sourceURL.toExternalForm();
		int index = sourceAsString.lastIndexOf(".");
		String base = sourceAsString.substring(0, index);
		String fileExtension = sourceAsString.substring(index);

		//We can now construct the baseURL from this string.
		
		float xPixelSize = 0;
		float rotation1 = 0;
		float rotation2 = 0;
		float yPixelSize = 0;
		float xLoc = 0;
		float yLoc = 0;
		
        BufferedReader in = null;

        try {
        	URL worldURL = new URL(base+".wld");
        	in = new BufferedReader(new InputStreamReader(worldURL.openStream()));
		} catch (FileNotFoundException e) {
			//.wld extension not found, go for file based one.
			URL worldURL = new URL(base+WorldImageFormat.getWorldExtension(fileExtension));
			in = new BufferedReader(new InputStreamReader(worldURL.openStream()));
		}
        
        String str;
        index = 0;
        while ((str = in.readLine()) != null) {
        	float value = Float.parseFloat(str.trim());
        	switch(index) {
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
        	index++;
        }
        in.close();

		BufferedImage image = null;
		image = ImageIO.read(sourceURL);
		
		CoordinateSystem cs = GeographicCoordinateSystem.WGS84;
		Envelope envelope = null;
		
		double xMin = xLoc;
		double yMax = yLoc;
		double xMax = xLoc + (image.getWidth()*xPixelSize);
		double yMin = yLoc + (image.getHeight()*yPixelSize);

		envelope = new Envelope(new double[] {xMin, yMin}, new double[] {xMax, yMax});

		gridLeft = false;
		
		GridCoverage coverage = new GridCoverage(sourceURL.getFile(), image, cs, envelope);
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
