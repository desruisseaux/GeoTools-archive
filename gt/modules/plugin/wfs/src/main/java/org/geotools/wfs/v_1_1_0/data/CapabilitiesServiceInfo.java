/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, GeoTools Project Managment Committee (PMC)
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
 */

package org.geotools.wfs.v_1_1_0.data;

import static org.geotools.wfs.protocol.HttpMethod.*;
import static org.geotools.wfs.protocol.WFSOperationType.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;

import net.opengis.ows.KeywordsType;
import net.opengis.ows.OnlineResourceType;
import net.opengis.ows.OperationsMetadataType;
import net.opengis.ows.ServiceProviderType;
import net.opengis.wfs.WFSCapabilitiesType;

import org.geotools.data.ServiceInfo;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.protocol.HttpMethod;
import org.geotools.wfs.protocol.WFSOperationType;

/**
 * Adapts a WFS capabilities document to {@link ServiceInfo}
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
final class CapabilitiesServiceInfo implements ServiceInfo {
    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");
    
    private static URI WFS_1_1_0_SCHEMA_URI;
    static {
        try {
            WFS_1_1_0_SCHEMA_URI = new URI("http://schemas.opengis.net/wfs/1.1.0/wfs.xsd");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private WFS110ProtocolHandler protocolHandler;

    public CapabilitiesServiceInfo(WFS110ProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    /**
     * Maps to the capabilities' service identification abstract
     * 
     * @see ServiceInfo#getDescription()
     */
    public String getDescription() {
        return protocolHandler.getServiceAbstract();
    }

    /**
     * @return {@code null}
     * @see ServiceInfo#getDescription()
     */
    public Icon getIcon() {
        return null; // talk to Eclesia the icons are in renderer?
    }

    /**
     * Maps to the capabilities' service identification keywords list
     * 
     * @see ServiceInfo#getDescription()
     */
    public Set<String> getKeywords() {
        return protocolHandler.getKeywords();
    }

    /**
     * @see ServiceInfo#getPublisher()
     */
    public URI getPublisher() {
        return protocolHandler.getServiceProviderUri();
    }

    /**
     * Maps to the WFS xsd schema in schemas.opengis.net
     * 
     * @see ServiceInfo#getSchema()
     */
    public URI getSchema() {
        return WFS_1_1_0_SCHEMA_URI;
    }

    /**
     * Maps to the URL of the capabilities document
     * 
     * @see ServiceInfo#getSource()
     */
    public URI getSource() {
        URL url;
        if (protocolHandler.supports(GET_CAPABILITIES, GET)) {
            url = protocolHandler.getOperationURL(GET_CAPABILITIES, GET);
        } else {
            url = protocolHandler.getOperationURL(GET_CAPABILITIES, POST);
        }
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "converting to URI: " + url.toExternalForm());
            return null;
        }
    }

    public String getTitle() {
        return protocolHandler.getServiceTitle();
    }
}