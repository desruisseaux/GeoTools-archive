/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.wms;

import org.geotools.data.ows.WMSCapabilities;

import org.jdom.Document;

import java.io.IOException;


/**
 * Provides support for parsing of a WMS GetCapabilties Document.
 * <p>
 * Instances will be responsible for parsing the GetCapabilities document according to
 * a specific implementation specification.
 * <p>
 * This class takes part in a GOF Builder pattern, the different instances of WMSParser
 * compete based on the value of canProcess to determine which one can best handle a provided
 * document. The winner makes a series of callback to the WMSBuilder object. WMSBuilder takes
 * care of the complexity of constructing a Capability object - in particular, the details
 * corresponding to the representation of Layers.
 * </p>
 * <p>
 * We have taken this approach to isolate the parsing code from the representation of
 * Capabilities. It should give us a chance to experiment with different layer representations.
 * </p>
 */
public interface WMSParser {
    /** Indicates Parser cannot process provided document */
    public static final int NO = 0;

    /** Indicates Parser provides generic or limited support for provided document */
    public static final int GENERIC = 1;

    /** Indicates Parser provides custom or specific support for provided document */
    public static final int CUSTOM = 2;

    /**
     * Test if this WMSParser can handle the provided document.
    * <p>
    * Sample use:
    * <pre><code>
    *  SAXBuilder builder = new SAXBuilder();
     *        Document document;
     *        try {
     *                document = builder.build(stream);
     *                return parser.canProcess( document );
     *        } catch (JDOMException e) {
     *                throw new ParseCapabilitiesException( badXML );
     *        }
    * </code></pre>
    * </p>
    * @param document Document to test
    * @returns GENERIC for a WMS 1.1.1 GetCapabilities docuemnt
    */
    public int canProcess(Document document) throws IOException;

    /**
     * Use WMSBuilder to construct a Capabilities object for the provided docuemnt.
     * <p>
     * Use of Builder pattern allows us to vary the Parser and isolate the complexities of
     * Capabilities construction (especially layer objects) from Parsing code. Note the use of
     * Builder (rather than a Factory) allows us to make the construction of layer objects order
     * dependent.
     * </p>
     * @param document Document to parse
     */
    public WMSCapabilities constructCapabilities(Document docuemnt,
        WMSBuilder builder) throws ParseCapabilitiesException;
}
