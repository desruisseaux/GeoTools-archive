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

import org.geotools.catalog.CatalogEntry;

// Geotools dependencies
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.parameter.ParameterDescriptor;
import org.geotools.parameter.ParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;

// GeoAPI dependencies
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import java.io.File;

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
    public static final ParameterDescriptor RESCALE = new ParameterDescriptor("Rescale",
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
        readParameters = new ParameterGroup(new ParameterDescriptorGroup(
                    mInfo, new GeneralParameterDescriptor[] { RESCALE }));
    }

    /**
     * Currently, we only accept files, and we do not open the file to  verify
     * the correct format.  If the file format is wrong, we deal with that
     * when we try to read it.
     *
     * @param o the source object to test for compatibility with this  format.
     *
     * @return true if "o" is a file.
     */
    public boolean accepts(Object o) {
        boolean goodfile = false;

        if (o instanceof CatalogEntry) {
            o = ((CatalogEntry) o).resource();
        }

        goodfile = o instanceof File;

        if (goodfile) {
            goodfile = GeoTiffReader.isGeoTiffFile((File) o);
        }

        return goodfile;
    }

    /**
     * If <CODE>source</CODE> is a file, this will return a reader object. This
     * file does not use hints in the construction of the geotiff reader.
     *
     * @param source DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public GridCoverageReader getReader(Object source) {
        if (source instanceof CatalogEntry) {
            source = ((CatalogEntry) source).resource();
        }

        GridCoverageReader reader = null;

        if (accepts(source)) {
            reader = new GeoTiffReader(this, source, null);
        }

        return reader;
    }

    /**
     * Always returns null.
     *
     * @param source DOCUMENT ME!
     *
     * @return null, always.
     */
    public GridCoverageWriter getWriter(Object source) {
        return null;
    }
}
