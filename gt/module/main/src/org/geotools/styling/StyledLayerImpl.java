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
package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;


/**
 * DOCUMENT ME!
 *
 * @author jamesm
 * @source $URL$
 */
public class StyledLayerImpl extends AbstractGTComponent implements StyledLayer {
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if ((name == this.name) || ((name != null) && name.equals(this.name))) {
            return;
        }

        this.name = name;
        fireChanged();
    }
}
