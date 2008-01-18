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
package org.geotools.wfs.v_1_0_0.data;

import static org.geotools.data.wfs.WFSOperationType.*;
import static org.geotools.data.wfs.HttpMethod.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.Authenticator;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.ows.OperationType;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.data.wfs.HttpMethod;
import org.geotools.data.wfs.Version;
import org.geotools.data.wfs.WFSOperationType;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.io.WFSConnectionFactory;
import org.geotools.xml.DocumentFactory;
import org.xml.sax.SAXException;

public class WFS100ProtocolHandler extends WFSConnectionFactory {
    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    private WFSCapabilities capabilities;

    public WFS100ProtocolHandler(InputStream capabilitiesReader, boolean tryGzip,
            Authenticator auth, String encoding) throws IOException {
        super(Version.v1_0_0, tryGzip, auth, encoding);
        capabilities = parseCapabilities(capabilitiesReader);
    }

    public WFSCapabilities getCapabilities() {
        return capabilities;
    }

    @SuppressWarnings("unchecked")
    private WFSCapabilities parseCapabilities(InputStream capabilitiesReader) throws IOException {
        // TODO: move to some 1.0.0 specific class
        Map hints = new HashMap();
        hints.put(DocumentFactory.VALIDATION_HINT, Boolean.FALSE);

        Object parsed;
        try {
            parsed = DocumentFactory.getInstance(capabilitiesReader, hints, LOGGER.getLevel());
        } catch (SAXException e) {
            throw new DataSourceException("Error parsing WFS 1.0.0 capabilities", e);
        }

        if (parsed instanceof WFSCapabilities) {
            return (WFSCapabilities) parsed;
        } else {
            throw new DataSourceException(
                    "The specified URL Should have returned a 'WFSCapabilities' object. Returned a "
                            + ((parsed == null) ? "null value."
                                    : (parsed.getClass().getName() + " instance.")));
        }
    }

    @Override
    public URL getOperationURL(WFSOperationType operation, HttpMethod method)
            throws UnsupportedOperationException {
        OperationType operationType;
        switch (operation) {
        case DESCRIBE_FEATURETYPE:
            operationType = capabilities.getDescribeFeatureType();
            break;
        case GET_CAPABILITIES:
            operationType = capabilities.getGetCapabilities();
            break;
        case GET_FEATURE:
            operationType = capabilities.getGetFeature();
            break;
        case GET_FEATURE_WITH_LOCK:
            operationType = capabilities.getGetFeatureWithLock();
            break;
        case LOCK_FEATURE:
            operationType = capabilities.getLockFeature();
            break;
        case TRANSACTION:
            operationType = capabilities.getTransaction();
            break;
        default:
            throw new IllegalArgumentException("Unknown operation type " + operation);
        }
        if (operationType == null) {
            throw new UnsupportedOperationException(operation + " not supported by the server");
        }
        URL url;
        if (GET == method) {
            url = operationType.getGet();
        } else {
            url = operationType.getPost();
        }
        if (url == null) {
            throw new UnsupportedOperationException("Method " + method + " for " + operation
                    + " is not supported by the server");
        }
        return url;
    }

    @Override
    public boolean supports(WFSOperationType operation, HttpMethod method) {
        try {
            getOperationURL(operation, method);
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

}
