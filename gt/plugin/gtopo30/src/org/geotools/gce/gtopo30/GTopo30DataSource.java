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
package org.geotools.gce.gtopo30;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
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

import org.geotools.coverage.Category;
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.sun.media.imageio.stream.FileChannelImageInputStream;
import com.sun.media.imageio.stream.RawImageInputStream;

/**
 * A data source designed to read the GTOPO30 file format, a publicly available
 * world wide DEM. For more information, and to get the free data, visit <A
 * HREF="http://edcdaac.usgs.gov/gtopo30/gtopo30.html">GTOP030 web site </A> I
 * am going to use this data source from the reader class I wrote for the
 * GTOPO30 grid coverage format.
 *
 * @author aaime
 * @author simone giannecchini
 * @author mkraemer
 * @source $URL$
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
    private static final int TILE_SIZE = 1024 * 1024*2;

    /** Dem data URL */
    private URL demURL;

    /** Dem data header URL */
    private URL demHeaderURL;

    /** Dem statistics file URL */
    private URL statsURL;

    /** The name of the file, used as the schema name */
    private String name = null;

    /** Cropping evenlope if the user doesn't want to get out the whole file */
    private GeneralEnvelope cropEnvelope;

    /** Preset colors used to generate an Image from the raw data */
    private Color[] demColors = new Color[] {
            new Color(5, 90, 5), new Color(150, 200, 150),
            new Color(190, 150, 20), new Color(100, 100, 50),
            new Color(200, 210, 220), Color.WHITE, Color.WHITE, Color.WHITE,
            Color.WHITE
        };

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
    	
    	String filename;
        try {
            filename = URLDecoder.decode(url.getFile(), "US-ASCII");
        } catch (UnsupportedEncodingException use) {
            throw new MalformedURLException("Unable to decode " + url
                + " cause " + use.getMessage());
        }

        boolean recognized = false;

        if (filename.endsWith(dmext) || filename.endsWith(dhext) ||
        	filename.endsWith(srext) || filename.endsWith(shext) ||
            filename.endsWith(stext)) {
            recognized = true;
        } else {
            dmext = dmext.toUpperCase();
            dhext = dhext.toUpperCase();
            srext = srext.toUpperCase();
            shext = shext.toUpperCase();
            stext = stext.toUpperCase();

            if (filename.endsWith(dmext) || filename.endsWith(dhext) ||
                filename.endsWith(srext) || filename.endsWith(shext) ||
                filename.endsWith(stext)) {
                recognized = true;
            }
        }

        if (!recognized) {
            throw new DataSourceException(
                "Unrecognized file (file extension doesn't match)");
        }

        this.name = filename.substring(0, filename.length() - 4);

        this.demURL = new URL(url, this.name + dmext);
        this.demHeaderURL = new URL(url, this.name + dhext);
        this.statsURL = new URL(url, this.name + stext);
    }

    /**
     * Gets the bounding box of this datasource using the default speed of this
     * datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     *         expensive for the method to calculate.
     */
    public GeneralEnvelope getBounds() {
        GeneralEnvelope env = null;

        try {
            GT30Header header = new GT30Header(this.demHeaderURL);
            final double xmin = header.getULXMap() - (header.getXDim() / 2);
            final double ymax = header.getULYMap() + (header.getYDim() / 2);
            final double ymin = ymax - (header.getNRows() * header.getYDim());
            final double xmax = xmin + (header.getNCols() * header.getXDim());

            env = new GeneralEnvelope(new double[] { xmin, ymin },
                    new double[] { xmax, ymax });
        } catch (Exception e) {
            // This should not happen!
            throw new RuntimeException("Unexpected error during creation of the envelope!" + e.getMessage());
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
        if (crop != null) {
            this.cropEnvelope = crop;
        }
    }

    /**
     * Returns the current crop rectangle
     *
     * @return the current crop rectangle (null if not set)
     */
    public GeneralEnvelope getCropEnvelope() {
        return this.cropEnvelope;
    }

	/**
	 * Retrieves a grid coverage based on the DEM assoicated to this gtopo coverage.
	 * The color palette is fixed and there is no possibility for the final user to change it.
	 * 
	 * @return the GridCoverage object
	 * @throws DataSourceException if an error occurs
	 */
    public GridCoverage getGridCoverage() throws DataSourceException {
        // Read the header
        final GT30Header header ;

        try {
            header = new GT30Header(this.demHeaderURL);
        } catch (Exception e) {
            throw new DataSourceException("Unexpected exception when trying to retrieve the header "
                + e.getMessage());
        }

        // get information from the header
        final int nrows = header.getNRows();
		final int ncols = header.getNCols();
		final double xdim = header.getXDim();
		final double ydim = header.getYDim();
		final double minx = header.getULXMap() - (xdim / 2);
		final double miny = (header.getULYMap() + (ydim / 2)) - (ydim * nrows);

        // Read the statistics file
        final GT30Stats stats;

        try {
            stats = new GT30Stats(this.statsURL);
        } catch (Exception e) {
            throw new DataSourceException("Unexpected exception when trying to read statistics file "
                + e.getMessage());
        }

		final int max = stats.getMax();
		final int min = stats.getMin();

        // prepare NIO based ImageInputStream
        final FileChannelImageInputStream iis;

        try {
            // trying to create a channel to the file to read
			final String filePath = URLDecoder.decode(this.demURL.getFile(), "US-ASCII");
			final FileInputStream fis = new FileInputStream(filePath);
			final FileChannel channel = fis.getChannel();
            iis = new FileChannelImageInputStream(channel);
			
			
            if (header.getByteOrder().compareToIgnoreCase("M") == 0) {
                iis.setByteOrder(ByteOrder.BIG_ENDIAN);
            } else {
                iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            }
        } catch (Exception e) {
            throw new DataSourceException("Unexpected exception", e);
        }
        //sample dimension
        final GridSampleDimension band = getSampleDimension(max, min);
        
        // Prepare temporaray colorModel and sample model, needed to build the
        // RawImageInputStream
		final ColorModel cm = band.getColorModel();
		final SampleModel sm = cm.createCompatibleSampleModel(ncols, nrows);
		final ImageTypeSpecifier its = new ImageTypeSpecifier(cm, sm); 
		
        // Finally, build the image input stream
		final RawImageInputStream raw = new RawImageInputStream(iis, its,
                new long[] { 0 },
                new Dimension[] { new Dimension(ncols, nrows) });

        // if crop needed
		GeneralEnvelope env = getBounds();
        ImageReadParam irp = null;

        // Make some decision about tiling.
		final int tileRows = (int) Math.ceil(TILE_SIZE / (ncols * 2));

        // building the final image layout
		final ImageLayout il = new ImageLayout(0, 0, ncols, nrows, 0, 0, ncols,
                tileRows, sm, cm);

        // First operator: read the image
		final ParameterBlockJAI pbj = new ParameterBlockJAI("ImageRead");
        pbj.setParameter("Input", raw);
        pbj.setParameter("ReadParam", irp);

        // Do not cache these tiles: the file is memory mapped anyway by
        // using NIO and these tiles are very big and fill up rapidly the cache:
        // better use it to avoid operations down the rendering chaing
        final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);
//      hints.add(new RenderingHints(JAI.KEY_TILE_CACHE, null));

        RenderedOp image = JAI.create("ImageRead", pbj, hints);
		pbj.removeParameters();
		pbj.removeSources();;
		
        if (this.cropEnvelope != null) {
            env = intersectEnvelope(env, this.cropEnvelope);

            float cxmin = Math.round((env.getUpperCorner().getOrdinate(0) - minx) / xdim);
            float cymin = Math.round((env.getLowerCorner().getOrdinate(1) - miny) / ydim);
            float cwidth = Math.round(Math.abs(env.getUpperCorner().getOrdinate(0) -
            		env.getLowerCorner().getOrdinate(0)) / xdim);
            float cheight = Math.round(Math.abs(env.getUpperCorner().getOrdinate(1) -
            		env.getLowerCorner().getOrdinate(1)) / ydim);

            cymin = nrows - cymin - cheight;

            final ParameterBlock pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(cxmin);
            pb.add(cymin);
            pb.add(cwidth);
            pb.add(cheight);
            hints.clear();
            //hints.add(new RenderingHints(JAI.KEY_TILE_CACHE, null));
            image = JAI.create("Crop", pb, hints);
			pb.removeSources();
			pb.removeParameters();

            
            pb.addSource(image);
            pb.add(-cxmin);
            pb.add(-cymin);
            image = JAI.create("Translate", pb, hints);
        }

        // Build the coordinate system
        final CoordinateReferenceSystem crs = AbstractGridFormat.getDefaultCRS();


        //switch from -999 to NaN to keep transparency informations
        //for the gridcoverage


        //setting metadata
        final Map metadata = new HashMap();
        metadata.put("maximum", new Double(stats.getMax()));
        metadata.put("minimum", new Double(stats.getMin()));
        metadata.put("mean", new Double(stats.getAverage()));
        metadata.put("std_dev", new Double(stats.getStdDev()));
        metadata.put("nodata", new Double(-9999.0));

        //cleaning name
        String coverageName = (new File(this.name)).getName();
        final int extension = coverageName.lastIndexOf(".");
        if (extension != -1) {
            String ext = coverageName.substring(extension + 1);

            if ((dmext.compareToIgnoreCase(ext) == 0) ||
            	(dhext.compareToIgnoreCase(ext) == 0) ||
                (srext.compareToIgnoreCase(ext) == 0) ||
                (shext.compareToIgnoreCase(ext) == 0) ||
                (stext.compareToIgnoreCase(ext) == 0)) {
                coverageName = coverageName.substring(0, extension);
            }
        }
		env.setCoordinateReferenceSystem(crs);
		final GridCoverage2D gc = (GridCoverage2D) FactoryFinder.getGridCoverageFactory(null).create(
                coverageName, image, env, new GridSampleDimension[] { band }, null, metadata);
		
		/**
		 * Freeing everything to be sure we do not leave any dead reference.
		 */	
		image = null;		
        return gc;
    }

    /**
     * 
     * @param max
     * @param min
     * @return
     */
	private GridSampleDimension getSampleDimension(final int max, final int min) {
		// Create the SampleDimension, with colors and byte transformation
        // needed for visualization
        UnitFormat unitFormat = UnitFormat.getStandardInstance();
        Unit uom = null;

        try {
			//unit of measure is meter
            uom = unitFormat.parseUnit("m");
        } catch (Exception ex1) {
            uom = null;
        }
		unitFormat = null;

        Category values = new Category("values", this.getColors(),
                new NumberRange(1, 255), new NumberRange((short)min,(short) max));
        Category nan = new Category("nodata",
                new Color[] { new Color(0, 0, 0, 0) }, new NumberRange(0, 0),
                new NumberRange((short)-9999, (short)-9999));
        GridSampleDimension band =
        	new GridSampleDimension(new Category[] { values, nan }, uom);
		band = band.geophysics(true);
		return band;
	}

    /**
     * Intersects an envelop
     *
     * @param a the envelope to be intersected
     * @param b the envelope to intersect a
     *
     * @return the intersected envelope
     */
    private GeneralEnvelope intersectEnvelope(GeneralEnvelope a, GeneralEnvelope b) {
        GeneralEnvelope env = new GeneralEnvelope(a);
        env.intersect(b);
        return env;
    }

    /**
     * Returns the set of colors used to create the image contained in the
     * GridCoverage returned by getFeatures
     *
     * @return the set of colors used to depict the DEM
     */
    public Color[] getColors() {
        return this.demColors;
    }

    /**
     * Allows the user to set different colors to depict the DEM returned by
     * getFeatures
     *
     * @param colors the new color set
     */
    public void setColors(final Color[] colors) {
        if (colors != null) {
            this.demColors = colors;
        }
    }
}
