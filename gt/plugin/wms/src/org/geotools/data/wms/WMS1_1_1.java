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

import org.geotools.data.wms.request.GetCapabilitiesRequest;

import org.jdom.Document;

import java.io.IOException;

import java.net.URL;


/**
 * Provides support for the Web Map Server 1.1.1 Specificaiton.
 * <p>
 * This class opperates as a Factory creating the following related objects.
 * <ul>
 * <li>WMS1_1_1.Parser - a WMSParser capable of parsing a Get Capabilities Document
 * <li>WMS1_1_1.Format - a WMSFormat describing required parameters
 * <li>WMS1_1_1.MapRequest - a MapRequest specific to WMS 1.0
 * </ul>
 * </p>
 * <p>
 * The idea is that this class opperates a Toolkit for all things assocated with
 * Web Map Server 1.1.1 Specification. The various objects produced by this toolkit
 * are used as stratagy objects for the top level Web Map Server objects:
 * <ul>
 * <li>Web Map Server - uses WMS1_1_1.Parser to derive a Get Capabilities Document
 * <li>Web Map Server - uses WMS1_1_1 as a WMSFormat factory to generate the correct
 *     WMS_1_1_1.Format.
 * </ul>
 * </p>
 * <p>
 * WMS1_1_1 provides both name and version information that may be checked against
 * a GetCapabilities document during version negotiation.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class WMS1_1_1 extends Specification {
    private WMSParser[] parsers;

    public WMS1_1_1() {
        parsers = new WMSParser[1];
        parsers[0] = new Parser();
    }

    /** Expected name attribute for root element */
    public String getName() {
        return "WMT_MS_Capabilities";
    }

    /**
     * Expected version attribute for root element.
     */
    public String getVersion() {
        return "1.1.1";
    }

    /** Factory method to create WMS 1.1.1 GetCapabilities Request */
    public GetCapabilitiesRequest createGetCapabilitiesRequest(URL server) {
        return new GetCapsRequest(server);
    }

    /**
     * Create parser given document generated from createRequest.
     * <p>
     * By allowing the specification to choose a Parser based on the document
     * we are given an oppertunity to choose a specific parser for several
     * vendors that generate unusal Capabilities documents.
     * </p>
     * <p>
     * That is even the version number negotiation worked out
     * with getVersion and createRequest above may not be sufficient to
     * narrow down our options to a specific parser. Actual document inspection
     * is required.
     * </p>
     * @param document
     * @return Parser capable of handling provided document
     */
    public WMSParser createParser(Document document) throws IOException {
        WMSParser generic = null;
        WMSParser custom = null;

        for (int i = 0; i < parsers.length; i++) {
            int canProcess = parsers[i].canProcess(document);

            if (canProcess == WMSParser.GENERIC) {
                generic = parsers[i];
            } else if (canProcess == WMSParser.CUSTOM) {
                custom = parsers[i];
            }
        }

        WMSParser parser = generic;

        if (custom != null) {
            parser = custom;
        }

        if (parser == null) {
            // Um can we have the name & version number please?
            throw new RuntimeException(
                "No parsers available to parse that GetCapabilities document");
        }

        return new Parser();
    }

    static public class GetCapsRequest extends GetCapabilitiesRequest {
        /**
         * Construct a Request compatable with a 1.0.0 Web Feature Server.
         *
         * @param urlGetCapabilities URL of GetCapabilities document.
         */
        public GetCapsRequest(URL urlGetCapabilities) {
            super(urlGetCapabilities);
        }

        protected void initVersion() {
            setProperty("VERSION", "1.1.1");
        }
    }

    static public class Parser extends AbstractWMSParser {
        public String getVersion() {
            return "1.1.1";
        }
    }
}
