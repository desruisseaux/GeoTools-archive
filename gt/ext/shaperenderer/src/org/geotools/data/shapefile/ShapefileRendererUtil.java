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
package org.geotools.data.shapefile;

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.data.FIDReader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.indexed.IndexedFidReader;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.renderer.shape.MultiLineHandler;
import org.geotools.renderer.shape.MultiPointHandler;
import org.geotools.renderer.shape.PointHandler;
import org.geotools.renderer.shape.PolygonHandler;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;

/**
 * Allows access the the ShapefileReaders.
 * 
 * @author jeichar
 * @since 2.1.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.2.x/ext/shaperenderer/src/org/geotools/data/shapefile/ShapefileRendererUtil.java $
 */
public class ShapefileRendererUtil {
    /**
     * gets a shapefile reader with the custom shaperenderer shape handler.
     * 
     * @param ds the datastore used to obtain the reader
     * @param bbox the area, in data coordinates, of the viewed data.
     * @param mt The transform used to transform from data->world coordinates->screen coordinates
     * @param hasOpacity the transform from screen coordinates to world coordinates. Used for
     *        decimation.
     * @return
     * @throws IOException
     * @throws TransformException
     */
    public static ShapefileReader getShpReader( ShapefileDataStore ds, Envelope bbox,
            MathTransform mt, boolean hasOpacity ) throws IOException, TransformException {
        ShapefileReader reader = ds.openShapeReader();
        ShapeType type = reader.getHeader().getShapeType();

        if ((type == ShapeType.ARC) || (type == ShapeType.ARCM) || (type == ShapeType.ARCZ)) {
            reader.setHandler(new MultiLineHandler(type, bbox, mt, hasOpacity));
        }

        if ((type == ShapeType.POLYGON) || (type == ShapeType.POLYGONM)
                || (type == ShapeType.POLYGONZ)) {
            reader.setHandler(new PolygonHandler(type, bbox, mt, hasOpacity));
        }

        if ((type == ShapeType.POINT) || (type == ShapeType.POINTM) || (type == ShapeType.POINTZ)) {
            reader.setHandler(new PointHandler(type, bbox, mt, hasOpacity));
        }

        if ((type == ShapeType.MULTIPOINT) || (type == ShapeType.MULTIPOINTM)
                || (type == ShapeType.MULTIPOINTZ)) {
            reader.setHandler(new MultiPointHandler(type, bbox, mt, hasOpacity));
        }

        return reader;
    }

    public static DbaseFileReader getDBFReader( ShapefileDataStore ds ) throws IOException {
        return new DbaseFileReader(ds.getReadChannel(ds.dbfURL));
    }

    public static ReadableByteChannel getShpReadChannel( ShapefileDataStore ds ) throws IOException {
        return ds.getReadChannel(ds.shpURL);
    }

    public static URL getshpURL( ShapefileDataStore ds ) {
        return ds.shpURL;
    }

    public static FIDReader getFidReader( ShapefileDataStore datastore )
            throws MalformedURLException {
        if (datastore instanceof IndexedShapefileDataStore) {
            IndexedShapefileDataStore ids = (IndexedShapefileDataStore)datastore;
            if( ids.fixURL==null )
                return createBasicFidReader(datastore);
            try{
                return new IndexedFidReader(datastore.getCurrentTypeName(), datastore.getReadChannel(ids.fixURL));
            }catch (Exception e) {
                return createBasicFidReader(datastore);
            }
        } else {
            return createBasicFidReader(datastore);
        }
    }

    private static FIDReader createBasicFidReader(ShapefileDataStore datastore) {
        final String typename = datastore.getCurrentTypeName() + ".";
        return new FIDReader(){
            int i = 0;
            boolean closed = false;
            public void close() throws IOException {
                closed = true;
            }

            public boolean hasNext() throws IOException {
                if (closed)
                    return false;
                return true;
            }

            public String next() throws IOException {
                if (closed)
                    throw new IllegalStateException("Reader is closed"); //$NON-NLS-1$
                i++;
                return typename + i;
            }

        };
    }
}
