package org.geotools.data.gpx;

import java.util.ArrayList;
import java.util.Iterator;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gpx.bean.RteType;
import org.geotools.gpx.bean.TrkType;
import org.geotools.gpx.bean.TrksegType;
import org.geotools.gpx.bean.WptType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

/**
 * This class is no thread safe. It uses a SimpleFeatureBuilder,
 * which is not thread safe.
 * 
 * @author Peter Bolla
 *
 */
public class FeatureTranslator {

    private final SimpleFeatureBuilder builder;
    private final GeometryFactory geomFactory;
    private final Object[] attrs;
    private final SimpleFeatureType featureType;
    
    FeatureTranslator(SimpleFeatureType featureType) {
        builder = new SimpleFeatureBuilder(featureType);
        
        geomFactory = new GeometryFactory();

        attrs = new Object[featureType.getAttributeCount()];
        
        this.featureType = featureType;
    }

    public SimpleFeature convertFeature(WptType type) {
        Coordinate ptCoord = new Coordinate(type.getLon(), type.getLat(), type.getEle());
        Point pt = geomFactory.createPoint(ptCoord);
        pt.setUserData(type.getTime());
        
        attrs[0]=pt;
        attrs[1]=type.getName();
        attrs[2]=type.getDesc();
        attrs[3]=type.getCmt();
        
//        builder.add(attrs);
//        return builder.feature(type.getName());
        try {
            return SimpleFeatureBuilder.build((SimpleFeatureType)featureType, attrs, type.getName());
        } catch (IllegalAttributeException e) {
            throw new RuntimeException("illegal attributes", e);
        }
    }
    
    public SimpleFeature convertFeature(TrkType type) {
        ArrayList<LineString> lineStrings = new ArrayList<LineString>();
        Iterator<TrksegType> it = type.getTrkseg().iterator();

        while (it.hasNext()) {
            TrksegType segment = it.next();

            ArrayList<Coordinate> lineStringCoords = new ArrayList<Coordinate>();

            Iterator<WptType> it2 = segment.getTrkpt().iterator();

            while (it2.hasNext()) {
                WptType coord = it2.next();
                lineStringCoords.add(new Coordinate(coord.getLon(), coord.getLat(), coord.getEle()));
            }

            LineString line = geomFactory.createLineString(lineStringCoords.toArray(
                        new Coordinate[lineStringCoords.size()]));
            lineStrings.add(line);
        }

        MultiLineString geom = geomFactory.createMultiLineString(lineStrings.toArray(
                        new LineString[lineStrings.size()]));

        attrs[0]=geom;
        attrs[1]=type.getName();
        attrs[2]=type.getDesc();
        attrs[3]=type.getCmt();
        
//        builder.add(attrs);
//        return builder.feature(type.getName());
        try {
            return SimpleFeatureBuilder.build((SimpleFeatureType)featureType, attrs, type.getName());
        } catch (IllegalAttributeException e) {
            throw new RuntimeException("illegal attributes", e);
        }
    }
    
    public SimpleFeature convertFeature(RteType type) {

        ArrayList<Coordinate> lineStringCoords = new ArrayList<Coordinate>();

        Iterator<WptType> it2 = type.getRtept().iterator();
        
        while (it2.hasNext()) {
            WptType coord = it2.next();
            lineStringCoords.add(new Coordinate(coord.getLon(), coord.getLat(), coord.getEle()));
        }

        LineString geom = geomFactory.createLineString(lineStringCoords.toArray(
                    new Coordinate[lineStringCoords.size()]));

        attrs[0]=geom;
        attrs[1]=type.getName();
        attrs[2]=type.getDesc();
        attrs[3]=type.getCmt();
        
//        builder.add(attrs);
//        return builder.feature(type.getName());
        try {
            return SimpleFeatureBuilder.build((SimpleFeatureType)featureType, attrs, type.getName());
        } catch (IllegalAttributeException e) {
            throw new RuntimeException("illegal attributes", e);
        }
    }
    
    
    
}
