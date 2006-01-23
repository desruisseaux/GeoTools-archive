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
package org.geotools.catalog;

import org.geotools.util.ProgressListener;


/**
 * Abstract implementation of Service.
 * @source $URL$
 */
public abstract class AbstractService implements Service {
    /** parent catalog containing the service */
    private Catalog parent;

    /**
     * Creates a new service handle contained within a catalog.
     *
     * @param parent The catalog containg the service.
     */
    public AbstractService(Catalog parent) {
        this.parent = parent;
    }

    /**
     * Returns the parent Catalog.
     *
     * @param monitor DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Resolve parent(ProgressListener monitor) {
        return parent;
    }

    /**
     * This should represent the identifier
     *
     * @param other
     *
     * @return
     *
     * @see Object#equals(java.lang.Object)
     */
    public final boolean equals(Object other) {
        if ((other != null) && other instanceof Service) {
            Service service = (Service) other;

            if ((getIdentifier() != null) && (service.getIdentifier() != null)) {
                return getIdentifier().equals(service.getIdentifier());
            }
        }

        return false;
    }

    /**
     * This method does nothing. Sublcasses should override if events are
     * supported.
     *
     * @param listener DOCUMENT ME!
     */
    public void addListener(ResolveChangeListener listener) {
        // do nothing
    }

    /**
     * This method does nothing. Sublcasses should override if events are
     * supported.
     *
     * @param listener DOCUMENT ME!
     */
    public void removeListener(ResolveChangeListener listener) {
        // do nothing
    }

    /**
     * This method does nothing. Sublcasses should override if events are
     * supported.
     *
     * @param event DOCUMENT ME!
     */
    public void fire(ResolveChangeEvent event) {
        // do nothing
    }

    /**
     * This should represent the identified
     *
     * @return
     *
     * @see Object#hashCode()
     */
    public final int hashCode() {
        if (getIdentifier() != null) {
            return getIdentifier().hashCode();
        }

        return super.hashCode();
    }

    /**
     * Indicate class and id.
     *
     * @return string representing this IResolve
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        String classname = getClass().getName();
        String name = classname.substring(classname.lastIndexOf('.') + 1);
        buf.append(name);
        buf.append("("); //$NON-NLS-1$
        buf.append(getIdentifier());
        buf.append(")"); //$NON-NLS-1$

        return buf.toString();
    }
}
