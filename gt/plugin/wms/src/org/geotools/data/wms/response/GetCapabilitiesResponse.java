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
package org.geotools.data.wms.response;

import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.ParseCapabilitiesException;
import org.geotools.data.wms.WMSBuilder;
import org.geotools.data.wms.WMSParser;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import java.io.IOException;
import java.io.InputStream;


/**
 * Represents the response of a getCapabilities request.
 * 
 * <p>
 * Feedback - this object actually starts the parsing? As part of the
 * constructor (!). How does this differ at all from just doing the work?
 * Unless you wanted to go nuts and support response encoding). Basically
 * provide the contentType and inputStream to the superclass and have it take
 * care of providing you a Document. Wrapping the inputStream with zip
 * uncoding before passing it to SAXBuilder.build( inputStream).
 * </p>
 * 
 * <p>
 * Another suggestion - store the error. So this object either provides you
 * with an exception or a working Capabilities object when all is said and
 * done.
 * </p>
 *
 * @author Richard Gould, Refractions Research
 */
public class GetCapabilitiesResponse extends AbstractResponse {
    private WMSCapabilities capabilities;

    public GetCapabilitiesResponse(WMSParser parser, String contentType,
        InputStream inputStream)
        throws JDOMException, ParseCapabilitiesException, IOException {
        this(parser, buildDocument(new SAXBuilder(), inputStream));
    }

    public GetCapabilitiesResponse(WMSParser parser, Document document)
        throws ParseCapabilitiesException {
        super(null, null);
        capabilities = parser.constructCapabilities(document, new WMSBuilder());
    }

    private static Document buildDocument(SAXBuilder builder,
        InputStream inputStream) throws IOException, JDOMException {
        return builder.build(inputStream);
    }

    /**
     * Retrived parsed Capabilities
     *
     * @return DOCUMENT ME!
     */
    public WMSCapabilities getCapabilities() {
        return capabilities;
    }
}
