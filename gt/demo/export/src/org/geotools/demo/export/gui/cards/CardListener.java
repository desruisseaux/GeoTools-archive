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
package org.geotools.demo.export.gui.cards;

/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public interface CardListener {
    /**
     * DOCUMENT ME!
     *
     * @param enable DOCUMENT ME!
     */
    void setNextEnabled(boolean enable);

    /**
     * DOCUMENT ME!
     *
     * @param enable DOCUMENT ME!
     */
    void setPreviousEnabled(boolean enable);

    /**
     * DOCUMENT ME!
     *
     * @param enable DOCUMENT ME!
     */
    void setFinishEnabled(boolean enable);
}
