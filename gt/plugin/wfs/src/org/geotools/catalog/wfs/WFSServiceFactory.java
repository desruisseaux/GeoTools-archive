/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.catalog.wfs;

import org.geotools.catalog.Catalog;
import org.geotools.catalog.Service;
import org.geotools.catalog.ServiceFactory;
import org.geotools.catalog.wfs.WFSService.MyWFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * Provides ...TODO summary sentence
 * 
 * <p>
 * TODO Description
 * </p>
 *
 * @since 0.6
 * @source $URL$
 */
public class WFSServiceFactory implements ServiceFactory {
    private static WFSDataStoreFactory wfsDSFactory;

    /**
     * TODO summary sentence for getWFSDSFactory ...
     *
     * @return x
     */
    public static WFSDataStoreFactory getWFSDSFactory() {
        if (wfsDSFactory == null) {
            wfsDSFactory = new WFSDataStoreFactory();
        }

        return wfsDSFactory;
    }

    /**
     * TODO summary sentence for createService ...
     *
     * @param parent DOCUMENT ME!
     * @param id
     * @param params
     *
     * @return x
     */
    public Service createService(Catalog parent, URI id, Map params) {
        if ((params == null)
                || !params.containsKey(WFSDataStoreFactory.URL.key)) {
            return null;
        }

        try {
            if (id == null) {
                URL base = (URL) params.get(WFSDataStoreFactory.URL.key);
                base = (base == null) ? null
                                      : MyWFSDataStore
                        .createGetCapabilitiesRequest(base);

                    return new WFSService(parent,
                        new URI(base.toExternalForm()), params);
                }

                return new WFSService(parent, id, params);
            } catch (URISyntaxException e) {
                return null;
            }
        }

        /**
         * TODO summary sentence for createParams ...
         *
         * @param uri
         *
         * @return x
         *
         * @see net.refractions.udig.catalog.ServiceExtension#createParams(java.net.URL)
         */
        public Map createParams(URI uri) {
            URL url;

            try {
                url = uri.toURL();
            } catch (MalformedURLException e) {
                return null;
            }

            if (!isWFS(url)) {
                return null;
            }

            // wfs check
            Map params = new HashMap();
            params.put(WFSDataStoreFactory.URL.key, url);

            // don't check ... it blocks
            // (XXX: but we are using that to figure out if the service will work?)
            return params;
        }

        public boolean canProcess(URI uri) {
            try {
                URL url = uri.toURL();

                return isWFS(url);
            } catch (MalformedURLException e) {
                return false;
            }
        }

        /**
         * A couple quick checks on the url
         *
         * @param url DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        private static final boolean isWFS(URL url) {
            String PATH = url.getPath();
            String QUERY = url.getQuery();
            String PROTOCOL = url.getProtocol();

            if (!"http".equals(PROTOCOL)) { //$NON-NLS-1$

                return false;
            }

            if ((QUERY != null)
                    && (QUERY.toUpperCase().indexOf("SERVICE=") != -1)) { //$NON-NLS-1$
                                                                          // we have a service! it better be wfs            

                return QUERY.toUpperCase().indexOf("SERVICE=WFS") != -1; //$NON-NLS-1$
            }

            if ((PATH != null)
                    && (PATH.toUpperCase().indexOf("GEOSERVER/WFS") != -1)) { //$NON-NLS-1$

                return true;
            }

            return true; // try it anyway
        }
    }
