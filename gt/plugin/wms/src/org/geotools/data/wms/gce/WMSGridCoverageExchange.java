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
package org.geotools.data.wms.gce;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;

import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageExchange;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.xml.sax.SAXException;



/**
 * DOCUMENT ME!
 *
 * @author Richard Gould, Refractions Research
 */
public class WMSGridCoverageExchange implements GridCoverageExchange {
    /** Available formats for this Web Map Server */
    private Format[] formats;

    /** Parsed WMS Capabilities document */
    private WMSCapabilities capabilities;

    /** Web Map Server proxy */
    private WebMapServer wms;

    public WMSGridCoverageExchange(Object source) throws SAXException, URISyntaxException, IOException
{
        if (source instanceof String || source instanceof URL) {
            URL url = null;

            if (source instanceof String) {
                url = new URL((String) source);
            } else {
                url = (URL) source;
            }

            wms = new WebMapServer(url);
            capabilities = wms.getCapabilities();

        } else if (source instanceof WMSCapabilities) {
            capabilities = (WMSCapabilities) source;
        }

        WMSFormatFactory factory = new WMSFormatFactory(capabilities);

        if (!factory.isAvailable()) {
            throw new RuntimeException("WMS support is not available");
        }

        formats = new Format[1];
        formats[0] = factory.createFormat();
    }

    public void dispose() throws IOException {
        // TODO Auto-generated method stub
    }

    public Format[] getFormats() {
        return formats;
    }

    public GridCoverageReader getReader(Object source)
        throws IOException {
        if (source instanceof String || source instanceof URL) {
            if (source instanceof String) {
                try {
                    new URL((String) source);
                } catch (MalformedURLException e) {
                    throw new InvalidParameterException(
                        "Unable to convert source to a URL (it is malformed)");
                }
            }

            return new WMSReader(wms);
            
        }

        throw new InvalidParameterException("Source is not of a support type");
    }

    /**
     * WMS Specification does not permit writing!
     *
     * @param destination DOCUMENT ME!
     * @param format DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    public GridCoverageWriter getWriter(Object destination, Format format)
        throws IOException {
        throw new RuntimeException("Writing is not supported for WMSs");
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean setDataSource(Object datasource) {
        if (datasource instanceof String || datasource instanceof URL) {
            try {
                new URL((String) datasource);

                return true;
            } catch (MalformedURLException e) {
                return false;
            }
        }

        return false;
    }

    public WMSCapabilities getCapabilities() {
        return capabilities;
    }
}
