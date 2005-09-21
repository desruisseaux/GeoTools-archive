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
package org.geotools.gce.geotiff;

import org.esa.beam.util.geotiff.GeoTIFF;
import org.esa.beam.util.geotiff.GeoTIFFMetadata;

import org.geotools.coverage.grid.GridCoverage2D;

import org.geotools.geometry.GeneralEnvelope;

import org.geotools.referencing.wkt.ParseWKT2GeoTiffMetadata;

import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;

import org.opengis.metadata.Identifier;

import org.opengis.parameter.GeneralParameterValue;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Collection;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;


/**
 * DOCUMENT ME!
 *
 * @author giannecchini TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class GeoTiffWriter implements GridCoverageWriter {
    private ImageOutputStream destination;

    /**
     * DOCUMENT ME!
     *
     * @param destination
     * @throws IOException
     */
    public GeoTiffWriter(Object destination) throws IOException {
        super();

        if (destination instanceof File || destination instanceof OutputStream) {
            this.destination = ImageIO.createImageOutputStream(destination);
        } else if (this.destination instanceof ImageOutputStream) {
            this.destination = (ImageOutputStream) destination;
        } else {
            this.destination = null;
        }
    }

    /**
     *
     */
    public GeoTiffWriter() {
        super();

        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.opengis.coverage.grid.GridCoverageWriter#getFormat()
     */
    public Format getFormat() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.coverage.grid.GridCoverageWriter#getDestination()
     */
    public Object getDestination() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.coverage.grid.GridCoverageWriter#getMetadataNames()
     */
    public String[] getMetadataNames() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String, java.lang.String)
     */
    public void setMetadataValue(String arg0, String arg1)
        throws IOException, MetadataNameNotFoundException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.opengis.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
     */
    public void setCurrentSubname(String arg0) throws IOException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.opengis.coverage.grid.GridCoverageWriter#write(org.opengis.coverage.grid.GridCoverage, org.opengis.parameter.GeneralParameterValue[])
     */
    public void write(GridCoverage gc, GeneralParameterValue[] arg1)
        throws IllegalArgumentException, IOException {
        //getting the coordinate reference system
        CoordinateReferenceSystem crs = gc.getCoordinateReferenceSystem();

        //we handle just projected andgeographic crsd 
        if (crs instanceof ProjectedCRS || crs instanceof GeographicCRS) {
            /** CREATING METADATA AND SETTING BASE FIELDS FOR THEM */

            //creating geotiff metadata
            GeoTIFFMetadata metadata = new GeoTIFFMetadata();

            //check if we have authority and code
            TreeSet identifiers = new TreeSet(crs.getIdentifiers());

            //model type			
            int modelType = (crs instanceof ProjectedCRS) ? 1 : 2;

            //GTModelTypeGeoKey
            metadata.addGeoShortParam(GeoTiffIIOMetadataAdapter.GTModelTypeGeoKey,
                modelType);

            //setting raster model
            metadata.addGeoShortParam(GeoTiffIIOMetadataAdapter.GTRasterTypeGeoKey,
                GeoTiffIIOMetadataAdapter.RasterPixelIsArea);

            switch (modelType) {
            /**
             * PROJECTED COORDINATE REFERENCE SYSTEM
             */
            case GeoTiffIIOMetadataAdapter.ModelTypeProjected:

                if ((identifiers != null) && (identifiers.size() != 0)) {
                    //ProjectedCSTypeGeoKey
                    metadata.addGeoShortParam(GeoTiffIIOMetadataAdapter.ProjectedCSTypeGeoKey,
                        Integer.parseInt(((Identifier) identifiers.first()).getCode()));
                } else //USER DEFINED PCS
                 {
                    ParseWKT2GeoTiffMetadata parser = new ParseWKT2GeoTiffMetadata(crs.toWKT(),
                            metadata);

                    try {
                        parser.parseCoordinateReferenceSystem();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                break;

            /**
             * GEOGRAPHIC COORDINATE REFERENCE SYSTEM
             */
            case GeoTiffIIOMetadataAdapter.ModelTypeGeographic:

                if ((identifiers != null) && (identifiers.size() != 0)) {
                    //ProjectedCSTypeGeoKey
                    metadata.addGeoShortParam(GeoTiffIIOMetadataAdapter.GeographicTypeGeoKey,
                        Integer.parseInt(((Identifier) identifiers.first()).getCode()));
                } else //USER DEFINED GCS
                 {
                    ParseWKT2GeoTiffMetadata parser = new ParseWKT2GeoTiffMetadata(crs.toWKT(),
                            metadata);

                    try {
                        parser.parseCoordinateReferenceSystem();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                break;

            default:
                throw new IllegalArgumentException("Unsuported model type");
            }

            /**
             * NOW WE NEED TO SET THE TIUE POINTS AND THE SCALE FOR THIS IMAGE
             */
            GeneralEnvelope envelope = (GeneralEnvelope) gc.getEnvelope();
            int W = gc.getGridGeometry().getGridRange().getLength(0);
            int H = gc.getGridGeometry().getGridRange().getLength(1);

            //tie points
            metadata.setModelTiePoint(0, 0, 0,
                envelope.getLowerCorner().getOrdinate(0),
                envelope.getUpperCorner().getOrdinate(1), 0);

            //scale
            metadata.setModelPixelScale(envelope.getLength(0) / W,
                envelope.getLength(1) / H, 0);

            //writing
            GeoTIFF.writeImage(prepareImage4Writing(((GridCoverage2D) gc).geophysics(
                        false).getRenderedImage()), this.destination, metadata);

            return;
        }

        throw new IllegalArgumentException(
            "The supplied grid coverage uses an unsupported crs! You are allowed to use " +
            " only projected and geographic coordinate reference systems");
    }

    /* (non-Javadoc)
    * @see org.opengis.coverage.grid.GridCoverageWriter#dispose()
    */
    public void dispose() throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * Prepare this image to be rendered correctly as a tiff. For the momet what we do is a simple check
     * over the color model in order to convert it to ComponentColorModel which seems to be
     * well acecepted from the TIFFEncoder in JAi. In the future we will really focus on doing
     * some compression and on to move this code in an utility class inside main for GeoTools.
     *
     *
     * @param renderedImage
     * @return
     */
    private RenderedImage prepareImage4Writing(RenderedImage renderedImage) {
        //we have nothing to do
        if (renderedImage.getColorModel() instanceof ComponentColorModel) {
            return renderedImage;
        }

        PlanarImage image = PlanarImage.wrapRenderedImage(renderedImage);

        //going from DirectColorModel to ComponentColorModel
        if (image.getColorModel() instanceof DirectColorModel) {
            renderedImage = GeoTiffUtils.direct2ComponentColorModel(image);
        }

        //going from IndexColorModel to ComponentColorModel
        //Are we dealing with IndexColorModel? If so we need to go back to ComponentColorModel
        if (image.getColorModel() instanceof IndexColorModel) {
            image = GeoTiffUtils.reformatColorModel2ComponentColorModel(image);
        }

        return image;
    }
}
