/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.caching.spatialindex.grid;

import org.geotools.caching.spatialindex.Data;
import org.geotools.caching.spatialindex.Shape;


/** Associates data with its shape and id, as to be stored in the index.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
class GridData implements Data {
    int id;
    Shape shape;
    Object data;

    GridData(int id, Shape shape, Object data) {
        this.id = id;
        this.shape = shape;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public int getIdentifier() {
        return id;
    }

    public Shape getShape() {
        return shape;
    }
}
