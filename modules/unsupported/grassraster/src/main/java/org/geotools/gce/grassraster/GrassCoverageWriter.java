/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2010, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.grassraster;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.io.AbstractGridCoverageWriter;
import org.geotools.gce.grassraster.core.GrassBinaryRasterWriteHandler;
import org.geotools.gce.grassraster.format.GrassCoverageFormat;
import org.geotools.gce.grassraster.spi.GrassBinaryImageReaderSpi;
import org.geotools.gce.grassraster.spi.GrassBinaryImageWriterSpi;
import org.geotools.geometry.Envelope2D;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.util.ProgressListener;

/**
 * Coverage Writer class for writing GRASS raster maps.
 * <p>
 * The class writes a GRASS raster map to a GRASS workspace (see package documentation for further
 * info). The writing is really done via Imageio extended classes.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 * @see GrassBinaryImageWriter
 * @see GrassBinaryRasterWriteHandler
 */
public class GrassCoverageWriter extends AbstractGridCoverageWriter implements GridCoverageWriter {
    private File output;
    private ProgressListener monitor = new DummyProgressListener();

    /**
     * Constructor for the {@link GrassCoverageWriter}.
     */
    public GrassCoverageWriter( Object output ) {
        if (output instanceof File) {
            this.output = (File) output;
        } else {
            throw new IllegalArgumentException("Illegal input argument!");
        }
    }

    public void setProgressListener( ProgressListener monitor ) {
        this.monitor = monitor;
    }

    /**
     * Writes the {@link GridCoverage2D supplied coverage} to disk.
     * <p>
     * Note that this also takes care to cloes the file handle after writing to disk.
     * </p>
     * 
     * @param gridCoverage2D the coverage to write.
     * @throws IOException
     */
    public void write( GridCoverage2D gridCoverage2D ) throws IOException {
        try {
            Envelope2D env = gridCoverage2D.getEnvelope2D();
            GridEnvelope2D worldToGrid = gridCoverage2D.getGridGeometry().worldToGrid(env);

            double xRes = env.getWidth() / worldToGrid.getWidth();
            double yRes = env.getHeight() / worldToGrid.getHeight();

            JGrassRegion region = new JGrassRegion(env.getMinX(), env.getMaxX(), env.getMinY(), env
                    .getMaxY(), xRes, yRes);

            GrassBinaryImageWriterSpi writerSpi = new GrassBinaryImageWriterSpi();
            GrassBinaryImageWriter writer = new GrassBinaryImageWriter(writerSpi, monitor);
            RenderedImage renderedImage = gridCoverage2D.view(ViewType.GEOPHYSICS)
                    .getRenderedImage();
            writer.setOutput(output, region);
            writer.write(renderedImage);
            writer.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void write( GridCoverage2D gridCoverage2D, JGrassRegion writeRegion ) throws IOException {
        GrassBinaryImageWriterSpi writerSpi = new GrassBinaryImageWriterSpi();
        GrassBinaryImageWriter writer = new GrassBinaryImageWriter(writerSpi, monitor);
        RenderedImage renderedImage = gridCoverage2D.view(ViewType.GEOPHYSICS).getRenderedImage();
        writer.setOutput(output, writeRegion);
        writer.write(renderedImage);
        writer.dispose();
    }

    public Format getFormat() {
        return new GrassCoverageFormat();
    }

    public void write( GridCoverage coverage, GeneralParameterValue[] parameters )
            throws IllegalArgumentException, IOException {
        if (coverage instanceof GridCoverage2D) {
            GridCoverage2D gridCoverage = (GridCoverage2D) coverage;
            write(gridCoverage);
        } else {
            throw new IllegalArgumentException("Coverage not a GridCoverage2D");
        }
    }

}
