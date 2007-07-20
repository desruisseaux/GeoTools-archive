package org.geotools.caching.spatialindex.grid;

import org.geotools.caching.spatialindex.Data;
import org.geotools.caching.spatialindex.Shape;


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
