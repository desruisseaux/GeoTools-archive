/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.catalog.wfs;

import org.geotools.catalog.AbstractService;
import org.geotools.catalog.Catalog;
import org.geotools.catalog.ResolveChangeEvent;
import org.geotools.catalog.ResolveDelta;
import org.geotools.catalog.ServiceInfo;
import org.geotools.catalog.defaults.DefaultResolveChangeEvent;
import org.geotools.catalog.defaults.DefaultResolveDelta;
import org.geotools.catalog.defaults.DefaultServiceInfo;
import org.geotools.data.DataStore;
import org.geotools.data.ows.WFSCapabilities;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.util.ProgressListener;
import org.geotools.xml.wfs.WFSSchema;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;


/**
 * Handle for a WFS service.
 *
 * @since 0.6
 * @source $URL$
 */
public class WFSService extends AbstractService {
    private URI uri = null;
    private Map params = null;
    private List members = null;
    private ServiceInfo info = null;
    private Throwable msg = null;
    private MyWFSDataStore ds = null;

    public WFSService(Catalog parent, URI uri, Map params) {
        super(parent);

        this.uri = uri;
        this.params = params;
    }

    /*
     * Required adaptions:
     * <ul>
     * <li>IServiceInfo.class
     * <li>List.class <IGeoResource>
     * </ul>
     */
    public Object resolve(Class adaptee, ProgressListener monitor)
        throws IOException {
        if (adaptee == null) {
            return null;
        }

        if (adaptee.isAssignableFrom(ServiceInfo.class)) {
            return this.getInfo(monitor);
        }

        if (adaptee.isAssignableFrom(List.class)) {
            return this.members(monitor);
        }

        if (adaptee.isAssignableFrom(WFSDataStore.class)) {
            return getDS();
        }

        return null;
    }

    public boolean canResolve(Class adaptee) {
        if (adaptee == null) {
            return false;
        }

        return (adaptee.isAssignableFrom(ServiceInfo.class)
        || adaptee.isAssignableFrom(List.class)
        || adaptee.isAssignableFrom(WFSDataStore.class));
    }

    public List members(ProgressListener monitor) throws IOException {
        if (members == null) {
            synchronized (getDS()) {
                if (members == null) {
                    getDS(); // load ds
                    members = new LinkedList();

                    String[] typenames = ds.getTypeNames();

                    if (typenames != null) {
                        for (int i = 0; i < typenames.length; i++) {
                            members.add(new WFSGeoResource(this, typenames[i]));
                        }
                    }
                }
            }
        }

        return members;
    }

    public ServiceInfo getInfo(ProgressListener monitor)
        throws IOException {
        getDS(); // load ds

        if ((info == null) && (ds != null)) {
            synchronized (ds) {
                if (info == null) {
                    info = new IServiceWFSInfo(ds);
                }
            }

            Catalog parent = (Catalog) parent(monitor);

            if (parent != null) {
                ResolveDelta delta = new DefaultResolveDelta(this,
                        ResolveDelta.Kind.CHANGED);
                ResolveChangeEvent event = new DefaultResolveChangeEvent(this,
                        ResolveChangeEvent.Type.POST_CHANGE, delta);
                parent.fire(event);
            }
        }

        return info;
    }

    /*
     * @see net.refractions.udig.catalog.IService#getConnectionParams()
     */
    public Map getConnectionParams() {
        return params;
    }

    MyWFSDataStore getDS() throws IOException {
        if (ds == null) {
            synchronized (MyWFSDataStore.class) {
                if (ds == null) {
                    WFSDataStoreFactory dsf = new MyWFSDataStoreFactory();

                    if (dsf.canProcess(params)) {
                        try {
                            ds = (MyWFSDataStore) dsf.createDataStore(params);
                        } catch (IOException e) {
                            msg = e;
                            throw e;
                        }
                    }
                }
            }

            Catalog parent = (Catalog) parent(null);

            if (parent != null) {
                ResolveDelta delta = new DefaultResolveDelta(this,
                        ResolveDelta.Kind.CHANGED);
                ResolveChangeEvent event = new DefaultResolveChangeEvent(this,
                        ResolveChangeEvent.Type.POST_CHANGE, delta);

                parent.fire(event);
            }
        }

        return ds;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getStatus()
     */
    public Status getStatus() {
        return (msg != null) ? Status.BROKEN
                             : ((ds == null) ? Status.NOTCONNECTED
                                             : Status.CONNECTED);
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return msg;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getIdentifier()
     */
    public URI getIdentifier() {
        return uri;
    }

    private static class MyWFSDataStoreFactory extends WFSDataStoreFactory {
        public DataStore createNewDataStore(Map params)
            throws IOException {
            URL host = (URL) URL.lookUp(params);

            Boolean protocol = (Boolean) PROTOCOL.lookUp(params);

            String user;
            String pass;
            user = pass = null;

            int timeout = 3000;
            int buffer = 10;
            boolean tryGZIP=true;
            
            if (params.containsKey(TIMEOUT.key)) {
                Integer i = (Integer) TIMEOUT.lookUp(params);

                if (i != null) {
                    timeout = i.intValue();
                }
            }

            if (params.containsKey(BUFFER_SIZE.key)) {
                Integer i = (Integer) BUFFER_SIZE.lookUp(params);

                if (i != null) {
                    buffer = i.intValue();
                }
            }

            if (params.containsKey(TRY_GZIP.key)) {
                Boolean b = (Boolean) TRY_GZIP.lookUp(params);
                if(b!=null)
                    tryGZIP = b.booleanValue();
            }

            if (params.containsKey(USERNAME.key)) {
                user = (String) USERNAME.lookUp(params);
            }

            if (params.containsKey(PASSWORD.key)) {
                pass = (String) PASSWORD.lookUp(params);
            }

            if (((user == null) && (pass != null))
                    || ((pass == null) && (user != null))) {
                throw new IOException("Username / password cannot be null");
            }

            DataStore ds = null;

            try {
                ds = new MyWFSDataStore(host, protocol, user, pass, timeout,
                        buffer, tryGZIP);
                cache.put(params, ds);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }

            return ds;
        }
    }

    static class MyWFSDataStore extends WFSDataStore {
        MyWFSDataStore(URL host, Boolean protocol, String username,
            String password, int timeout, int buffer, boolean tryGZIP)
            throws SAXException, IOException {
            super(host, protocol, username, password, timeout, buffer, tryGZIP);
        }

        public WFSCapabilities getCapabilities() {
            return capabilities;
        }

        protected static URL createGetCapabilitiesRequest(URL host) {
            return WFSDataStore.createGetCapabilitiesRequest(host);
        }
    }

    private class IServiceWFSInfo extends DefaultServiceInfo {
        private WFSCapabilities caps = null;

        IServiceWFSInfo(MyWFSDataStore resource) {
            super();

            try {
                caps = resource.getCapabilities();
            } catch (Throwable t) {
                t.printStackTrace();
                caps = null;
            }
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getAbstract()
         */
        public String getAbstract() {
            return (caps == null) ? null
                                  : ((caps.getService() == null) ? null
                                                                 : caps.getService()
                                                                       .get_abstract());
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getIcon()
         */
        public Icon getIcon() {
            //TODO: get an icon
            return null;
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getKeywords()
         */
        public String[] getKeywords() {
            return (caps == null) ? null
                                  : ((caps.getService() == null) ? null
                                                                 : caps.getService()
                                                                       .getKeywordList());
        }

        /*
         * @see net.refractions.udig.catalog.IServiceInfo#getSchema()
         */
        public URI getSchema() {
            return WFSSchema.NAMESPACE;
        }

        public String getDescription() {
            return getIdentifier().toString();
        }

        public URI getSource() {
            return getIdentifier();
        }

        public String getTitle() {
            return ((caps == null) || (caps.getService() == null))
            ? ((getIdentifier() == null) ? "BROKEN" : getIdentifier().toString())
            : caps.getService().getTitle();
        }
    }
}
