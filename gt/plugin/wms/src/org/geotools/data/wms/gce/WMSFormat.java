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

import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.GeneralParameterDescriptor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * GridCoverageExchangeFormat describing Capabilities of a Web Map Server.
 * 
 * <p>
 * Unlike several file system Formats this represents the abilities of a
 * dynamic Server. In addition file formats are often called by client code
 * with the specification of a single URL for processing. WMSFormat will need
 * a series of Layers.
 * </p>
 *
 * @author Richard Gould, Refractions Researach
 */
public class WMSFormat extends AbstractGridFormat {
    /** Parsed Capabilities Document */
    private WMSCapabilities capabilities;

    /**
     * TODO: What does this mean? How can I have a WMSFormat without knowing
     * the capabilities of my Web Map Server?
     * 
     * <p>
     * It looks like a WMSFormat would need to be provided with capabilities
     * during the call to accepts( Object ) and getReader( Object ). This
     * still does not make sense to me as differnent Capabilties documents
     * (for different specifications) should dictate the required parameters
     * described by getReadParameters().
     * </p>
     */
    public WMSFormat() {
    }

    /**
     * WMSFormat creation.
     *
     * @param capabilities Parsed Capabilties document from a Web Map Server
     */
    public WMSFormat(WMSCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.Format#getReader(java.lang.Object)
     */
    public GridCoverageReader getReader(Object source) {
        WMSReader reader = new WMSReader(source);
        reader.setFormat(this);

        return reader;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.Format#getWriter(java.lang.Object)
     */

    /**
     * Web Map Servers are not capable of writing, as of version 1.1.1 Returns
     * null.
     *
     * @param destination DOCUMENT ME!
     *
     * @return null
     */
    public GridCoverageWriter getWriter(Object destination) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.Format#accepts(java.lang.Object)
     */

    /**
     * Determines if the input can be processed or not.
     * 
     * <p>
     * Currently it accepts WebMapServers, WMT_MS_Capabilities, and URLs and
     * Strings that point to the WMS's getCapabilities address.
     * </p>
     * 
     * <p>
     * Feedback: this seams a bit crazy?
     * </p>
     *
     * @param input DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean accepts(Object input) {
        if (input instanceof String) {
            try {
                URL url = new URL((String) input);

                return true;
            } catch (MalformedURLException e) {
                return false;
            }
        }

        if (input instanceof URL) {
            return true;
        }

        if (input instanceof WMSCapabilities) {
            return true;
        }

        return false;
    }

    /**
     * Retrive parameter metadata describing parameters required by this Web
     * Map Server.
     * 
     * <p>
     * This information should be specific enough to allow the creation of a
     * user interface.
     * </p>
     *
     * @return DOCUMENT ME!
     */
    public org.opengis.parameter.ParameterDescriptorGroup getReadParameters() {
        GeneralParameterDescriptor[] params = new GeneralParameterDescriptor[16];

        WMSParameterMaker maker = new WMSParameterMaker(capabilities);
        params[0] = maker.createVersionReadParam();
        params[1] = maker.createRequestReadParam();
        params[2] = maker.createFormatReadParam();
        params[3] = maker.createHeightReadParam();
        params[4] = maker.createWidthReadParam();
        params[5] = maker.createSRSReadParam();
        params[6] = maker.createLayersReadParam();
        params[7] = maker.createBBoxMinXReadParam();
        params[8] = maker.createBBoxMinYReadParam();
        params[9] = maker.createBBoxMaxXReadParam();
        params[10] = maker.createBBoxMaxYReadParam();
        params[11] = maker.createTransparentReadParam();
        params[12] = maker.createBGColorReadParam();
        params[13] = maker.createExceptionsReadParam();
        params[14] = maker.createElevationReadParam();
        params[15] = maker.createTimeReadParam();

        Map properties = new HashMap();
        properties.put("name", capabilities.getService().getName());
        properties.put("remarks", capabilities.getService().get_abstract());
        readParameters = new ParameterDescriptorGroup(properties, params);

        return readParameters;
    }
}
