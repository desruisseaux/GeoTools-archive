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

import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.imageio.ImageIO;

import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.gc.GridCoverage;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.FileFormatNotCompatibleWithGridCoverageException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * @author rgould
 *
 * Writes a GridCoverage to a raster image file and an accompanying world file.
 * The destination specified must point to the location of the raster file to
 * write to, as this is how the format is determined. The directory that file is
 * located in must also already exist.
 */
public class WorldImageWriter implements GridCoverageWriter {
	
	private Object destination;
	
	/**
	 * Destination must be a File. The directory it resides in must already exist.
	 * It must point to where the raster image is to be located. The world image will
	 * be derived from there.
	 * 
	 * @param destination
	 */
	public WorldImageWriter (Object destination) {
		this.destination = destination;
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageWriter#getFormat()
	 */
	/**
	 * Returns the format supported by this WorldImageWriter, a new WorldImageFormat
	 */
	public Format getFormat() {
		return new WorldImageFormat();
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageWriter#getDestination()
	 */
	/**
	 * Returns the location of the raster that the GridCoverage will be written to.
	 */
	public Object getDestination() {
		return destination;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageWriter#getMetadataNames()
	 */
	/**
	 * Metadata is not supported. Returns null.
	 */
	public String[] getMetadataNames() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String, java.lang.String)
	 */
	/**
	 * Metadata not supported, does nothing.
	 */
	public void setMetadataValue(String name, String value) throws IOException,
			MetadataNameNotFoundException {
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
	 */
	/**
	 * Raster images don't support names. Does nothing.
	 */
	public void setCurrentSubname(String name) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageWriter#write(org.geotools.gc.GridCoverage, org.opengis.parameter.GeneralParameterValue[])
	 */
	/**
	 * Takes a GridCoverage and writes the image to the destination file.
	 * It then reads the format of the file and writes an accompanying world file.
	 * It will throw a FileFormatNotCompatibleWithGridCoverageException if Destination
	 * is not a File (URL is a read-only format!).
	 * 
	 * @param coverage the GridCoverage to write.
	 * @param parameters no parameters are accepted. Currently ignored.
	 */
	public void write(GridCoverage coverage, ParameterValueGroup parameters)
			throws IllegalArgumentException, IOException {
		
		RenderedImage image = coverage.getRenderedImage();
		
		Rectangle2D box = coverage.getEnvelope().toRectangle2D();
		double xMin = box.getMinX();
		double yMin = box.getMinY();
		double xMax = box.getMaxX();
		double yMax = box.getMaxY();
		
		double xPixelSize = (xMax-xMin)/image.getWidth();
		double rotation1 = 0;
		double rotation2 = 0;
		double yPixelSize = (yMax-yMin)/image.getHeight();
		double xLoc = xMin;
		double yLoc = yMax;
		
		if (!(destination instanceof File)) {
			throw new FileFormatNotCompatibleWithGridCoverageException("Not a valid format.");
		} else if (destination instanceof URL) {
			throw new FileFormatNotCompatibleWithGridCoverageException("URL is a read-only format");
		}
		
		File imageFile = (File) destination;
		String path = imageFile.getAbsolutePath();
		int index = path.lastIndexOf(".");
		String baseFile = path.substring(0, index);
		String extension = path.substring(index);
		File worldFile = new File(baseFile+WorldImageFormat.getWorldExtension(extension));
		
		imageFile.createNewFile();
		worldFile.createNewFile();

		ImageIO.write(image, extension.substring(1), imageFile);
		
		PrintWriter out = new PrintWriter(new FileWriter(worldFile));
		out.println(xPixelSize);
		out.println(rotation1);
		out.println(rotation2);
		out.println("-"+yPixelSize);
		out.println(xLoc);
		out.println(yLoc);
		out.close();
	}

	/* (non-Javadoc)
	 * @see org.geotools.data.coverage.grid.GridCoverageWriter#dispose()
	 */
	/**
	 * Cleans up the writer. Currently does nothing.
	 */
	public void dispose() throws IOException {
	}

}
