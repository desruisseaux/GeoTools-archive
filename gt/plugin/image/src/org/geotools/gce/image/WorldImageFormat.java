/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004 Geotools Project Managment Committee (PMC)
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

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.ParameterDescriptor;
import org.geotools.parameter.ParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.crs.GeographicCRS;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.Envelope;

import java.io.File;

import java.net.URL;

import java.util.HashMap;

/**
 * @author rgould
 *
 * A Format to allow discovery of Readers/Writers for raster images
 * that support world files containing information about the image.
 *
 * Supports .gif+.gfw, .jpg/.jpeg+.jgw, .tif/.tiff+.tfw and .png+.pgw.
 * .wld may be used in place of the format specific extension (.jpg+.wld, etc)
 *
 * Designed to be used with GridCoverageExchange.
 */
public class WorldImageFormat extends AbstractGridFormat implements Format {
    /**Format writing  parameter.
     *
     * When writing a world image we need to provide an output format in which we want
     * to encode the image itself.
     *
     * PNG is default output format.
     *
     */
    public static final ParameterDescriptor FORMAT = new ParameterDescriptor("Format",
            "Indicates the output format for this image", "png", true);
    public static final ParameterDescriptor CRS = new ParameterDescriptor("crs",
            CoordinateReferenceSystem.class, //calss of the object we will pass
            null, //list of valid values not provided
            GeographicCRS.WGS84 //default value
        );
    public static final ParameterDescriptor ENVELOPE = new ParameterDescriptor("envelope",
            Envelope.class, null,
            new GeneralEnvelope(new double[] { 0, 0 }, new double[] { 1, 1 })); //default envelope to avoid exceptions in GridCoverage2D

    /**
     * WorldImageFormat
     */
    public WorldImageFormat() {
        setInfo();
    }

    private void setInfo() {
        //information for this format
        HashMap info = new HashMap();

        info.put("name", "WorldImage");
        info.put("description",
            "A raster file accompanied by a spatial data file");
        info.put("vendor", "Geotools");
        info.put("docURL", "http://www.geotools.org/WorldImageReader+formats");
        info.put("version", "1.0");
        mInfo = info;

        //reading parameters
        readParameters = new ParameterGroup(new ParameterDescriptorGroup(
                    mInfo, new GeneralParameterDescriptor[] { CRS, ENVELOPE }));

        //writing parameters
        writeParameters = new ParameterGroup(new ParameterDescriptorGroup(
                    mInfo, new GeneralParameterDescriptor[] { FORMAT }));
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.Format#getReader(java.lang.Object)
     */

    /**
     * Call the accepts() method before asking for a reader to determine
     * if the current object is supported.
     *
     * @param source The source object to read a WorldImage from
     * @return a new WorldImageReader for the source
     */
    public GridCoverageReader getReader(Object source) {
        return new WorldImageReader(source);
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.Format#getWriter(java.lang.Object)
     */

    /**
     * Call the accepts() method before asking for a writer to determine
     * if the current object is supported.
     *
     * @param destination the destination object to write a WorldImage to
     * @return a new WorldImageWriter for the destination
     */
    public GridCoverageWriter getWriter(Object destination) {
        return new WorldImageWriter(destination);
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.Format#accepts(java.lang.Object)
     */

    /**
     * Takes the input and determines if it is a class that we can understand
     * and then futher checks the format of the class to make sure we can
     * read/write to it.
     *
     * @param input The object to check for acceptance.
     * @return true if the input is acceptable, false otherwise
     */
    public boolean accepts(Object input) {
        String pathname = "";

        if (input instanceof URL) {
            URL url = (URL) input;

            pathname = url.getFile();
        }

        if (input instanceof File) {
            File file = (File) input;

            pathname = file.getName();
        }

        if (pathname.endsWith(".gif") || pathname.endsWith(".jpg")
            || pathname.endsWith(".jpeg") || pathname.endsWith(".tif")
            || pathname.endsWith(".tiff") || pathname.endsWith(".png")) {
            return true;
        }

        return false;
    }

    /**
     * Takes an image file extension (such as .gif, including the '.') and
     * returns it's corresponding world file extension (such as .gfw).
     *
     * @param fileExtension an image file extension, including the '.'
     * @return a corresponding world file extension, including the '.'
     */
    public static String getWorldExtension(String fileExtension) {
        if (fileExtension == null) {
            return null;
        }

        if (fileExtension.equals("png")) {
            return ".pgw";
        }

        if (fileExtension.equals("gif")) {
            return ".gfw";
        }

        if (fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
            return ".jgw";
        }

        if (fileExtension.equals("tif") || fileExtension.equals("tiff")) {
            return ".tfw";
        }

        if (fileExtension.equals("bmp")) {
            return ".bfw";
        }

        if (fileExtension.equals("gif")) {
            return ".gfw";
        }

        return null;
    }
}
