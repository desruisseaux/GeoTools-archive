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
import org.geotools.image.io.netcdf.TemperatureReaderSpi;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

// Geotools dependencies
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.image.ImageUtilities;

// OpenGIS dependencies
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;

// Geomatys dependencies
import org.geotools.image.io.netcdf.AbstractReaderSpi;


/**
 * A reader of NetCDF files, to obtain a Grid Coverage from data in it.
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
    private final static AbstractReaderSpi readerSpi = new TemperatureReaderSpi();

    /**
     * The format that created this reader.
     */
    private final Format format;
    
    /**
     * Constructs a reader for a netCDF file.
     */
    public NetcdfReader(final Format format, Object input, Hints hints) throws DataSourceException {
        this.format = format;
        crs = DefaultGeographicCRS.WGS84;
        
        // sets the input.
        if (input == null) {
            throw new DataSourceException("No source set to read this coverage.");
        }
        
        source = input;
        if (source instanceof File) {
            this.coverageName = ((File)source).getName();
        } else {
            this.coverageName = "netcdf_coverage";
        }

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
     * @param params Contains the parameters values for this coverage.
     */
    public GridCoverage read(GeneralParameterValue[] params) throws IllegalArgumentException, IOException {
        GeneralEnvelope requestedEnvelope = null;
        Rectangle dim = null;
        // Test params
        if (params != null) {
            Parameter param;
            final int length = params.length;
            for (int i = 0; i < length; i++) {
                param = (Parameter) params[i];
                if (param.getDescriptor().getName().getCode().equals(
                        AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())) {
                    final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                    requestedEnvelope = new GeneralEnvelope((Envelope) gg.getEnvelope2D());
                    dim = gg.getGridRange2D().getBounds();
                }
            }
        }
        // Set params
        Integer imageChoice = new Integer(0);
        final ImageReadParam readP = new ImageReadParam();
        requestedEnvelope = new GeneralEnvelope(crs);
        requestedEnvelope.setRange(0, -180, +180);
        requestedEnvelope.setRange(1, -90, +90);
        this.originalEnvelope = new GeneralEnvelope(crs);
        this.originalEnvelope.setRange(0, -180, +180);
        this.originalEnvelope.setRange(1, -90, +90);
        try {
            imageChoice = setReadParams(readP, requestedEnvelope, dim);
        } catch (TransformException e) {
            new DataSourceException(e);
        }
        // Construct a reader
        final ImageReader reader = readerSpi.createReaderInstance(null);
        final ImageInputStream inStream;
        if (source instanceof File || source instanceof URL) {
            inStream = ImageIO.createImageInputStream(source);
        } else {
            throw new IllegalArgumentException();
        }
        //System.out.println(inStream.getClass());
        final Hints newHints = (Hints) hints.clone();
        //final ImageInputStream inStream = ImageIO.createImageInputStream(source);
        //reader.setInput(inStream, true);
//        final Hints newHints = (Hints) hints.clone();
//        inStream.mark();
//        if (!reader.isImageTiled(imageChoice.intValue())) {
//            final Dimension tileSize = ImageUtilities.toTileSize(new Dimension(
//                    reader.getWidth(imageChoice.intValue()), reader.getHeight(imageChoice.intValue())));
//            final ImageLayout layout = new ImageLayout();
//            layout.setTileGridXOffset(0);
//            layout.setTileGridYOffset(0);
//            layout.setTileHeight(tileSize.height);
//            layout.setTileWidth(tileSize.width);
//            newHints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));
//        }
//        inStream.reset();
        final ParameterBlock pbjRead = new ParameterBlock();
        pbjRead.add(inStream);
        pbjRead.add(imageChoice);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(null);
        pbjRead.add(null);
        pbjRead.add(readP);
        pbjRead.add(reader);
        
        // create the coverage
        RenderedImage image = JAI.create("ImageRead",pbjRead, (RenderingHints) newHints);
        GridCoverage grid = createImageCoverage((PlanarImage) image);
        return grid;
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
