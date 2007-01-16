/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import javax.imageio.ImageReader;
import javax.media.jai.PlanarImage;

// OpenGIS dependencies
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.image.io.netcdf.AbstractReaderSpi;


/**
 * A reader of NetCDF files, to obtain a Grid Coverage from these files.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class NetcdfReader extends AbstractGridCoverage2DReader implements GridCoverageReader {
    /**
     * The entry to log messages during the process.
     */
    private static final Logger LOGGER = Logger.getLogger(NetcdfReader.class.toString());

    /**
     * The reader Spi for netCDF images.
     */
    private AbstractReaderSpi readerSpi; 

    /**
     * The format that created this reader.
     */
    private final Format format;

    /**
     * A temporary variable to store the depth.
     * @todo modify geoserver wcs to handle 3d.
     */
    private final int depth;

    /**
     * Constructs a reader for a netCDF file.
     *
     * @param format The default netcdf format.
     * @param input The netcdf file or url for this file.
     * @param hints Null in this implementation.
     * @throws DataSourceException
     */
    public NetcdfReader(final Format format, Object input, Hints hints, int depth) throws DataSourceException {
        this.depth = depth;
        this.hints = hints;
        this.format = format;
        try {
            this.crs = CRS.decode("EPSG:4326");
        } catch (NoSuchAuthorityCodeException ex) {
            this.crs = DefaultGeographicCRS.WGS84;
        } catch (FactoryException ex) {
            this.crs = DefaultGeographicCRS.WGS84;
        }               
        this.originalEnvelope = new GeneralEnvelope(crs);
        this.originalEnvelope.setRange(0, -180, +180);
        this.originalEnvelope.setRange(0, -90, +90);
        this.originalGridRange = new GeneralGridRange(originalEnvelope);
        if (input == null) {
            throw new DataSourceException("No source set to read this coverage.");
        }
        // sets the input
        source = input;
        if (source instanceof File) {
            this.coverageName = ((File)source).getName();
        } else {
            if (source instanceof URL) {
                File tmp = new File((String)source);
                this.coverageName = tmp.getName();
            } else {
                this.coverageName = "netcdf_coverage";
            }
        }
        // gets the coverage name without the extension and the dot
        final int dotIndex = coverageName.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex != coverageName.length()) {
            coverageName = coverageName.substring(0, dotIndex);
        }       
    }

    /**
     * Gets information about the netCDF format.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Get the names of metadata. Not implemented in this project.
     */
    public String[] getMetadataNames() throws IOException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Get the metadata value for a specified fields. Not implemented in this project.
     */
    public String getMetadataValue(String string) throws IOException, MetadataNameNotFoundException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Not implemented.
     */
    public String[] listSubNames() throws IOException {
        return null;
    }

    /**
     * Not implemented.
     */
    public String getCurrentSubname() throws IOException {
        return null;
    }

    /**
     * Not implemented.
     */
    public boolean hasMoreGridCoverages() throws IOException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Read the coverage and generate the Grid Coverage associated.
     *
     * @param params Contains the parameters values for this coverage.
     * @return The grid coverage generated from the reading of the netcdf file.
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public GridCoverage read(GeneralParameterValue[] params) throws IllegalArgumentException, IOException {
        final GeneralEnvelope requestedEnvelope = new GeneralEnvelope(originalEnvelope);
        readerSpi = new IfremerReaderSpi(depth);
        final ImageReader reader = readerSpi.createReaderInstance(null);
        reader.setInput(source);
        RenderedImage image = reader.read(0);
        //image = new ImageWorker(image).forceComponentColorModel().getRenderedImage();
        return createImageCoverage(PlanarImage.wrapRenderedImage(image));
    }

    /**
     * Not implemented.
     */
    public void skip() throws IOException {
        throw new UnsupportedOperationException("Only one NetCDF image supported.");
    }

    /**
     * Desallocate the input stream. If in IOException is caught, this implementation will retry.
     */
    public void dispose() {
        while (inStream != null) {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {}
            }
        }
    }
}
