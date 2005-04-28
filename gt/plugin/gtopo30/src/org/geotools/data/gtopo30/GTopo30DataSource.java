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
package org.geotools.data.gtopo30;

import com.sun.media.imageio.stream.FileChannelImageInputStream;
import com.sun.media.imageio.stream.RawImageInputStream;
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.Envelope;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.units.Unit;
import javax.units.UnitFormat;


/**
 * A data source designed to read the GTOPO30 file format, a publicly available
 * world wide DEM. For more information, and to get the free data, visit <A
 * HREF="http://edcdaac.usgs.gov/gtopo30/gtopo30.html">GTOP030 web site </A> I
 * am going to use this data source from the reader class I wrote for the
 * GTOPO30 grid coverage format.
 *
 * @author aaime
 * @author simone giannecchini
 */
class GTopo30DataSource {
    private static String dmext = ".dem";
    private static String dhext = ".hdr";
    private static String srext = ".src";
    private static String shext = ".sch";
    private static String stext = ".stx";

    /**
     * Let's say that, for the moment, I want to read approximately 512k at a
     * time
     */
    private static final int TILE_SIZE = 1024 * 512;

    /** Dem data URL */
    private URL demURL;

    /** Dem data header URL */
    private URL demHeaderURL;

    /** Dem source file URL */
    private URL srcURL;

    /** Dem source header URL */
    private URL srcHeaderURL;

    /** Dem statistics file URL */
    private URL statsURL;

    /** The name of the file, used as the schema name */
    private String name = null;

    /** Cropping evenlope if the user doesn't want to get out the whole file */
    private Envelope cropEnvelope;

    /** Preset colors used to generate an Image from the raw data */
    private Color[] demColors = new Color[] {
            new Color(5, 90, 5), new Color(150, 200, 150),
            new Color(190, 150, 20), new Color(100, 100, 50),
            new Color(200, 210, 220), Color.WHITE, Color.WHITE, Color.WHITE,
            Color.WHITE
        };

    /** Contains the file name, without extension */
    private String filename;

    /**
     * Creates a new instance of GTopo30DataSource
     *
     * @param url URL pointing to one of the GTopo30 files (.dem, .hdr, .src,
     *        .sch, .stx)
     *
     * @throws MalformedURLException if the URL does not correspond to one of
     *         the GTOPO30 files
     * @throws DataSourceException if the given url points to an unrecognized
     *         file
     */
    public GTopo30DataSource(final URL url)
        throws MalformedURLException, DataSourceException {
        try {
            filename = URLDecoder.decode(url.getFile(), "US-ASCII");
        } catch (UnsupportedEncodingException use) {
            throw new MalformedURLException("Unable to decode " + url
                + " cause " + use.getMessage());
        }

        boolean recognized = false;

        if (filename.endsWith(dmext) || filename.endsWith(dhext)
                || filename.endsWith(srext) || filename.endsWith(shext)
                || filename.endsWith(stext)) {
            recognized = true;
        } else {
            dmext = dmext.toUpperCase();
            dhext = dhext.toUpperCase();
            srext = srext.toUpperCase();
            shext = shext.toUpperCase();
            stext = stext.toUpperCase();

            if (filename.endsWith(dmext) || filename.endsWith(dhext)
                    || filename.endsWith(srext) || filename.endsWith(shext)
                    || filename.endsWith(stext)) {
                recognized = true;
            }
        }

        if (!recognized) {
            throw new DataSourceException(
                "Unrecognized file (file extension doesn't match)");
        }

        name = filename.substring(0, filename.length() - 4);

        demURL = new URL(url, name + dmext);
        demHeaderURL = new URL(url, name + dhext);
        srcURL = new URL(url, name + srext);
        srcHeaderURL = new URL(url, name + shext);
        statsURL = new URL(url, name + stext);
    }

    /**
     * Gets the bounding box of this datasource using the default speed of this
     * datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public Envelope getBounds() {
        GeneralEnvelope env = null;

        try {
            GT30Header header = new GT30Header(demHeaderURL);
            double xmin = header.getULXMap() - (header.getXDim() / 2);
            double ymax = header.getULYMap() + (header.getYDim() / 2);
            double ymin = ymax - (header.getNRows() * header.getYDim());
            double xmax = xmin + (header.getNCols() * header.getXDim());

            env = new GeneralEnvelope(new double[] { xmin, ymin },
                    new double[] { xmax, ymax });
        } catch (Exception e) {
            // This should not happen!
            throw new RuntimeException("Unexpected error!" + e);
        }

        return env;
    }

    /**
     * Sets an envelope that will be used to crop the source data in order to
     * get fewer data from the file
     *
     * @param crop the rectangle that will be used to extract data from the
     *        file
     */
    public void setCropEnvelope(final GeneralEnvelope crop) {
        GeneralEnvelope bbox = (GeneralEnvelope) getBounds();

        if (crop != null) {
            cropEnvelope = crop;
        }
    }

    /**
     * Returns the current crop rectangle
     *
     * @return the current crop rectangle (null if not set)
     */
    public Envelope getCropEnvelope() {
        return cropEnvelope;
    }

    public GridCoverage getGridCoverage() throws DataSourceException {
        // Read the header
        GT30Header header = null;

        try {
            header = new GT30Header(demHeaderURL);
        } catch (Exception e) {
            throw new DataSourceException("Unexpected exception"
                + e.getMessage());
        }

        // get information from the header
        int nrows = header.getNRows();
        int ncols = header.getNCols();
        double xdim = header.getXDim();
        double ydim = header.getYDim();
        double minx = header.getULXMap() - (xdim / 2);
        double miny = (header.getULYMap() + (ydim / 2)) - (ydim * nrows);

        // Read the statistics file
        GT30Stats stats = null;

        try {
            stats = new GT30Stats(statsURL);
        } catch (Exception e) {
            throw new DataSourceException("Unexpected exception "
                + e.getMessage());
        }

        int max = stats.getMax();
        int min = stats.getMin();

        // prepare NIO based ImageInputStream
        FileChannelImageInputStream iis = null;

        try {
            // trying to create a channel to the file to read
            String filePath = URLDecoder.decode(demURL.getFile(), "US-ASCII");
            FileInputStream fis = new FileInputStream(filePath);
            FileChannel channel = fis.getChannel();
            iis = new FileChannelImageInputStream(channel);

            if (header.getByteOrder().compareToIgnoreCase("M") == 0) {
                iis.setByteOrder(ByteOrder.BIG_ENDIAN);
            } else {
                iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            }
        } catch (Exception e) {
            throw new DataSourceException("Unexpected exception", e);
        }

        // Prepare temporaray colorModel and sample model, needed to build the
        // RawImageInputStream
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(
                    ColorSpace.CS_GRAY), false, false, Transparency.OPAQUE,
                DataBuffer.TYPE_SHORT);
        SampleModel sm = cm.createCompatibleSampleModel(ncols, nrows);
        ImageTypeSpecifier its = new ImageTypeSpecifier(cm, sm); // ImageTypeSpecifier.createGrayscale(16,

        // DataBuffer.TYPE_SHORT,
        // true);
        // Finally, build the image input stream
        RawImageInputStream raw = new RawImageInputStream(iis, its,
                new long[] { 0 },
                new Dimension[] { new Dimension(ncols, nrows) });

        // if crop needed
        Envelope env = getBounds();
        ImageReadParam irp = null;

        // Make some decision about tiling.
        int tileRows = (int) Math.ceil(TILE_SIZE / (ncols * 2));

        // building the final image layout
        ImageLayout il = new ImageLayout(0, 0, ncols, nrows, 0, 0, ncols,
                tileRows, sm, cm);

        // First operator: read the image
        ParameterBlockJAI pbj = new ParameterBlockJAI("ImageRead");
        pbj.setParameter("Input", raw);
        pbj.setParameter("ReadParam", irp);

        // Do not cache these tiles: the file is memory mapped anyway by
        // using NIO and these tiles are very big and fill up rapidly the cache:
        // better use it to avoid operations down the rendering chaing
        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);
        //hints.add(new RenderingHints(JAI.KEY_TILE_CACHE, null));

        RenderedOp image = JAI.create("ImageRead", pbj, hints);

        if (cropEnvelope != null) {
            env = intersectEnvelope(env, cropEnvelope);

            float cxmin = (float) Math.round((env.getUpperCorner().getOrdinate(0)
                    - minx) / xdim);
            float cymin = (float) Math.round((env.getLowerCorner().getOrdinate(1)
                    - miny) / ydim);
            float cwidth = (float) Math.round(Math.abs(env.getUpperCorner()
                                                          .getOrdinate(0)
                        - env.getLowerCorner().getOrdinate(0)) / xdim);
            float cheight = (float) Math.round(Math.abs(env.getUpperCorner()
                                                           .getOrdinate(1)
                        - env.getUpperCorner().getOrdinate(1)) / ydim);

            cymin = nrows - cymin - cheight;

            ParameterBlock pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(cxmin);
            pb.add(cymin);
            pb.add(cwidth);
            pb.add(cheight);
            hints = new RenderingHints(JAI.KEY_TILE_CACHE, null);
            image = JAI.create("Crop", pb, hints);

            pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(-cxmin);
            pb.add(-cymin);
            image = JAI.create("Translate", pb, hints);
        }

        // Build the coordinate system
        CoordinateReferenceSystem crs = AbstractGridFormat.getDefaultCRS();

        // Create the SampleDimension, with colors and byte transformation
        // needed for visualization
        String UoM = null;
        UnitFormat unitFormat = UnitFormat.getStandardInstance();
        Unit uom = null;

        try {
            uom = unitFormat.parseUnit("m");
        } catch (Exception ex1) {
            uom = null;
        }

        Category values = new Category("values", this.getColors(),
                new NumberRange(1, 255), new NumberRange(min, max));
        Category nan = new Category("nodata",
                new Color[] { new Color(0, 0, 0, 1) }, new NumberRange(0, 0),
                new NumberRange(-9999, -9999));
        ; //new Category("nodata", new Color(0, 0, 0), 0);

        //  GridSampleDimension band = new GridSampleDimension(new Category[] {
        //     nan, values
        // }, uom);
        // band = band.geophysics(true);
        GridSampleDimension band = new GridSampleDimension(new Category[] {
                    values.geophysics(true), nan.geophysics(true)
                }, uom);

        //switch from -999 to NaN to keep transparency informations
        //for the gridcoverage
        BufferedImage img = new BufferedImage(band.getColorModel(),
                (WritableRaster) this.getAdjustedRaster(
                    (WritableRaster) image.createSnapshot().getAsBufferedImage()
                                          .getData()), false, null); // properties????

        //setting metadata
        Map metadata = new HashMap();
        metadata.put("maximum", new Double(stats.getMax()));
        metadata.put("minimum", new Double(stats.getMin()));
        metadata.put("mean", new Double(stats.getAverage()));
        metadata.put("std_dev", new Double(stats.getStdDev()));
        metadata.put("nodata", new Double(-9999.0));

        //cleaning name
        String coverageName = (new File(this.name)).getName();
        int extension = coverageName.lastIndexOf(".");

        if (extension != -1) {
            String ext = coverageName.substring(extension + 1);

            if ((dmext.compareToIgnoreCase(ext) == 0)
                    || (dhext.compareToIgnoreCase(ext) == 0)
                    || (srext.compareToIgnoreCase(ext) == 0)
                    || (shext.compareToIgnoreCase(ext) == 0)
                    || (stext.compareToIgnoreCase(ext) == 0)) {
                coverageName = coverageName.substring(0, extension);
            }
        }

        return new GridCoverage2D(coverageName, img, crs, env,
            new GridSampleDimension[] { band }, null, metadata);
    }

    /**
     * DOCUMENT ME!
     *
     * @param raster
     *
     * @return
     */
    private WritableRaster getAdjustedRaster(WritableRaster raster) {
        return raster; /*
           int W=raster.getWidth();
           int H=raster.getHeight();
           WritableRaster returnedRaster= javax.media.jai.RasterFactory.createBandedRaster(
                           DataBuffer.TYPE_FLOAT,
                           W,
                           H,
                           raster.getNumBands(),//1
                           null);
           int sample=0;
           for(int i=0;i<H;i++)
                   for(int j=0;j<W;j++)
                   {
                           sample=raster.getSample(j,i,0);
                           if(sample==-9999)
                                   returnedRaster.setSample(j,i,0,Float.NaN);
                           else
                                   returnedRaster.setSample(j,i,0,(float)sample);
                   }
           raster=null;//free initial raster
           return returnedRaster;*/
    }

    /**
     * Convenience method to wrap a GridCoverage in a feature for inclusion in
     * a FeatureCollection
     *
     * @param a the GridCoverage to be passed
     * @param b DOCUMENT ME!
     *
     * @return a feature wrapping the grid coverage
     */

    /*
     * private Feature wrapGcInFeature(GridCoverage gc) { // create surrounding
     * polygon GeometryFactory gf = new GeometryFactory(); Rectangle2D rect =
     * gc.getEnvelope().toRectangle2D(); Coordinate[] coord = new Coordinate[5];
     * coord[0] = new Coordinate(rect.getMinX(), rect.getMinY()); coord[1] = new
     * Coordinate(rect.getMaxX(), rect.getMinY()); coord[2] = new
     * Coordinate(rect.getMaxX(), rect.getMaxY()); coord[3] = new
     * Coordinate(rect.getMinX(), rect.getMaxY()); coord[4] = new
     * Coordinate(rect.getMinX(), rect.getMinY()); Feature feature = null; try {
     * LinearRing ring = gf.createLinearRing(coord); Polygon bounds =
     * gf.createPolygon(ring, null); // create the feature type AttributeType
     * geom = AttributeTypeFactory.newAttributeType("geom", Polygon.class);
     * AttributeType grid = AttributeTypeFactory.newAttributeType("grid",
     * GridCoverage.class); FeatureType schema = null; AttributeType[] attTypes =
     * {geom, grid}; schema = FeatureTypeFactory.newFeatureType(attTypes,
     * filename); // create the feature feature = schema.create(new Object[]
     * {bounds, gc}); } catch (SchemaException e) { throw new
     * RuntimeException("A schema exception occurred, that should not happen!",
     * e); } catch (IllegalAttributeException e) { throw new
     * RuntimeException("An illegal attribute exception occurred, that " +
     * "should not happen!", e); } return feature; }
     */

    /**
     * DOCUMENT ME!
     *
     * @param a DOCUMENT ME!
     * @param b DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Envelope intersectEnvelope(Envelope a, Envelope b) {
        GeneralEnvelope env = new GeneralEnvelope(a);

        ((GeneralEnvelope) env).intersect((GeneralEnvelope) b);

        return env;
    }

    /**
     * Returns the set of colors used to create the image contained in the
     * GridCoverage returned by getFeatures
     *
     * @return the set of colors used to depict the DEM
     */
    public Color[] getColors() {
        return demColors;
    }

    /**
     * Allows the user to set different colors to depict the DEM returned by
     * getFeatures
     *
     * @param colors the new color set
     */
    public void setColors(final Color[] colors) {
        if (colors != null) {
            demColors = colors;
        }
    }
}
