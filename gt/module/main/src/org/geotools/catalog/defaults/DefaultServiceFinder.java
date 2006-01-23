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
package org.geotools.catalog.defaults;

import org.geotools.catalog.Catalog;
import org.geotools.catalog.Service;
import org.geotools.catalog.ServiceFactory;
import org.geotools.catalog.ServiceFinder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * A default service factory which uses the SPI classpath plugin lookup
 * mechanism to locate service extension plugins.
 *
 * @since 0.6
 * @source $URL$
 */
public class DefaultServiceFinder implements ServiceFinder {
    private Catalog catalog;

    public DefaultServiceFinder(Catalog catalog) {
        this.catalog = catalog;
    }

    public List aquire(URI id, Map params) {
        List services = new ArrayList();
        List extensions = getServiceFactories();

        for (Iterator itr = extensions.iterator(); itr.hasNext();) {
            ServiceFactory se = (ServiceFactory) itr.next();

            try {
                //protect ourselves from plugins
                Service service = se.createService(catalog, id, params);

                if (service != null) {
                    services.add(service);
                }
            } catch (Throwable t) {
                //do nothing
            }
        }

        return services;
    }

    public List aquire(Map params) {
        return aquire(null, params);
    }

    /**
     * Aquire IService handles generated by all ServiceExtentions that think
     * they can handle the provided target url.
     * 
     * <p>
     * Note: Just because a target is created does NOT mean it will actually
     * work. You can check the handles in the usual manner (ask for their
     * info) after you get back this list.
     * </p>
     *
     * @param target
     *
     * @return
     *
     * @see net.refractions.udig.catalog.IServiceFactory#aquire(java.net.URL)
     */
    public List aquire(URI target) {
        List maps = new LinkedList();
        List extensions = getServiceFactories();

        for (Iterator itr = extensions.iterator(); itr.hasNext();) {
            ServiceFactory se = (ServiceFactory) itr.next();

            try {
                Map m = se.createParams(target);

                if (m != null) {
                    maps.add(m);
                }
            } catch (Throwable t) {
                //do nothing
            }
        }

        List services = new LinkedList();
        Iterator i = maps.iterator();

        while (i.hasNext()) {
            List o = aquire((Map) i.next());

            if ((o != null) && !o.isEmpty()) {
                services.addAll(o);
            }
        }

        return services;
    }

    /**
     * This method uses the current classpath to look for instances of  {@link
     * ServiceFactory}. Sublcasses should overide this method if they  wish to
     * use a diffent plug-in mechanism.
     *
     * @return A list of ServiceFactory plugins, or an empty list if none
     *         could be found.
     */
    public List getServiceFactories() {
        //TODO: default implementation should use classpath lookup mechanism
        return Collections.EMPTY_LIST;
    }
}
