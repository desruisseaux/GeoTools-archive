/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2008, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wfs.WFSCapabilitiesType;

import org.geotools.data.DataSourceException;
import org.geotools.data.wfs.HttpMethod;
import org.geotools.data.wfs.Version;
import org.geotools.data.wfs.WFSOperationType;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.wfs.io.WFSConnectionFactory;
import org.geotools.xml.Parser;
import org.xml.sax.SAXException;

public class WFS110ProtocolHandler extends WFSConnectionFactory {

    private static final WFSConfiguration configuration = new WFSConfiguration();

    private WFSCapabilitiesType capabilities;

    public WFS110ProtocolHandler(InputStream capabilitiesReader, boolean tryGzip,
            Authenticator auth, String encoding) throws IOException {
        super(Version.v1_1_0, tryGzip, auth, encoding);
        capabilities = parseCapabilities(capabilitiesReader);
    }

    private WFSCapabilitiesType parseCapabilities(InputStream capabilitiesReader)
            throws IOException {
        // TODO: move to 1.1.0 specific class
        final Parser parser = new Parser(configuration);
        final Object parsed;
        try {
            parsed = parser.parse(capabilitiesReader);
        } catch (SAXException e) {
            throw new DataSourceException("Exception parsing WFS 1.1.0 capabilities", e);
        } catch (ParserConfigurationException e) {
            throw new DataSourceException("WFS 1.1.0 parsing configuration error", e);
        }
        if (parsed == null) {
            throw new DataSourceException("WFS 1.1.0 capabilities was not parsed");
        }
        if (!(parsed instanceof WFSCapabilitiesType)) {
            throw new DataSourceException("Expected WFS Capabilities, got " + parsed);
        }
        return (WFSCapabilitiesType) parsed;
    }

    public WFSCapabilitiesType getCapabilities() {
        return capabilities;
    }

    @Override
    public URL getOperationURL(WFSOperationType operation, HttpMethod method)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public boolean supports(WFSOperationType operation, HttpMethod method) {
        throw new UnsupportedOperationException("not yet implemented");
    }

}
