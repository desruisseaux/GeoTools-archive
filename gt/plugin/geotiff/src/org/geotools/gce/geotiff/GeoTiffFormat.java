/*
 * NOTICE OF RELEASE TO THE PUBLIC DOMAIN
 *
 * This work was created by employees of the USDA Forest Service's
 * Fire Science Lab for internal use.  It is therefore ineligible for
 * copyright under title 17, section 105 of the United States Code.  You
 * may treat it as you would treat any public domain work: it may be used,
 * changed, copied, or redistributed, with or without permission of the
 * authors, for free or for compensation.  You may not claim exclusive
 * ownership of this code because it is already owned by everyone.  Use this
 * software entirely at your own risk.  No warranty of any kind is given. 
 *
 * A copy of 17-USC-105 should have accompanied this distribution in the file
 * 17USC105.html.  If not, you may access the law via the US Government's
 * public websites:
 *   - http://www.copyright.gov/title17/92chap1.html#105
 *   - http://www.gpoaccess.gov/uscode/  (enter "17USC105" in the search box.)
 */
package org.geotools.gce.geotiff;

// Geotools dependencies
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;

// GeoAPI dependencies
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

// J2SE dependencies
import java.util.HashMap;


/**
 * Provides basic information about the GeoTIFF format IO.   This is currently
 * an extension of the Geotools AbstractGridFormat because the stream and file
 * GCEs will pick it up if it extends AbstractGridFormat.
 *
 * @author Bryce Nordgren, USDA Forest Service
 */
public class GeoTiffFormat extends AbstractGridFormat implements Format {
    /** Indicates whether we need to rescale the input */
    public static final DefaultParameterDescriptor RESCALE = new DefaultParameterDescriptor("Rescale",
            "Indicates whether we need to rescale the input", Boolean.TRUE, true);

    /**
     * Creates a new instance of GeoTiffFormat
     */
    public GeoTiffFormat() {
        writeParameters = null;
        mInfo = new HashMap();
        mInfo.put("name", "GeoTIFF");
        mInfo.put("description",
            "Tagged Image File Format with Geographic information");
        mInfo.put("vendor", "Geotools");
        mInfo.put("version", "1.1");
        mInfo.put("docURL",
            "http://www.remotesensing.org:16080/websites/geotiff/geotiff.html");

        //reading parameters
        readParameters = new ParameterGroup(new DefaultParameterDescriptorGroup(
                    mInfo, new GeneralParameterDescriptor[] { RESCALE }));
    }

    /**
     * Currently, we only accept files, and we open the file to  verify that it
     * has a GeoKeyDirectory tag.  If anything more subtle is  wrong with the
     * file, we deal with that when we try and read it.
     *
     * @param o the source object to test for compatibility with this  format.
     *        Must be a CatalogEntry.
     *
     * @return true if "o" is a CatalogEntry with a GeoTiff file as a
     *         resource.
     */
    public boolean accepts(Object o) {
        boolean goodfile = false;

        goodfile = o instanceof File;

        if (goodfile) {
            goodfile = GeoTiffReader.isGeoTiffFile((File) o);
        } else if (o instanceof URL) {
            URL url = (URL) o;

						try {
            	final String pathname = URLDecoder.decode(url.getFile(),"UTF-8");
            
            	goodfile = GeoTiffReader.isGeoTiffFile(new File(pathname));
            } catch (Exception e) {
            	goodfile = false;
            }
        }

        return goodfile;
    }

    /**
     * If <CODE>source</CODE> is a file, this will return a reader object. This
     * file does not use hints in the construction of the geotiff reader.
     *
     * @param source must be a GeoTiff File
     *
     * @return a GeoTiffReader object initialized to the specified File.
     */
    public GridCoverageReader getReader(Object source) {

        GridCoverageReader reader = null;

        if (accepts(source)) {
        		if (source instanceof URL) {
            	URL url = (URL) source;

							try {
            		final String pathname = URLDecoder.decode(url.getFile(),"UTF-8");
            
            		reader = new GeoTiffReader(this, new File(pathname), null);
            	} catch (Exception e) {
            		reader = null;
            	}
            } else {
            	reader = new GeoTiffReader(this, source, null);
            }
        }

        return reader;
    }

    /**
     * Always returns null.
     *
     * @param source ignored
     *
     * @return null, always.
     */
    public GridCoverageWriter getWriter(Object source) {
        return null;
    }
}
