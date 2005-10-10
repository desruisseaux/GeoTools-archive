/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.feature.impl.AttributeFactoryImpl;
import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.opengis.feature.AttributeFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A set of constructs and utility methods used to test the data module.
 * <p>
 * By isolating a commong set of Features,FeatureTypes and Filters
 * we are able to reduce the amount of overhead in setting up new
 * tests.
 * </p>
 * <p>
 * We have also special cased assert( Geometry, Geometry ) to work around
 * Geometry.equals( Object ) not working as expected.
 * </p>
 * <p>
 * This code has been made part of the public geotools.jar to provide
 * a starting point for test cases involcing Data constructs.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public abstract class DataTestCase extends TestCase {
    protected GeometryFactory gf;
    protected SimpleFeatureType roadType; // road: id,geom,name
    protected SimpleFeatureType subRoadType; // road: id,geom    
    protected SimpleFeature[] roadFeatures;
    protected Envelope roadBounds;
    protected Envelope rd12Bounds;    
    protected Filter rd1Filter;
    protected Filter rd2Filter;
    protected Filter rd12Filter;
    protected Feature newRoad;
    
    protected SimpleFeatureType riverType; // river: id, geom, river, flow
    protected SimpleFeatureType subRiverType; // river: river, flow     
    protected Feature[] riverFeatures;
    protected Envelope riverBounds;
    protected Filter rv1Filter;
    protected Feature newRiver;    

    protected SimpleFeatureType lakeType; // lake: id, geom, name
    protected SimpleFeature[] lakeFeatures;
    protected Envelope lakeBounds;
    
    
    /**
     * Constructor for DataUtilitiesTest.
     *
     * @param arg0
     */
    public DataTestCase(String arg0) {
        super(arg0);        
    }

    protected void setUp() throws Exception {
        dataSetUp();
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void dataSetUp() throws Exception {
        final String namespace = getName();
        final TypeFactory typeFactory = new TypeFactoryImpl();
        final DescriptorFactory descFactory = new DescriptorFactoryImpl();
        final AttributeFactory attFactory = new AttributeFactoryImpl();
        List<AttributeType> types = new ArrayList<AttributeType>();

        types.add(typeFactory.createType("id", Integer.class));
        types.add(typeFactory.createType("geom", LineString.class));
        types.add(typeFactory.createType("name", String.class));
        roadType = typeFactory.createFeatureType(new QName(namespace, "road"), types, null);
        
        types.clear();
        types.add(typeFactory.createType("id", Integer.class));
        types.add(typeFactory.createType("geom", LineString.class));
        subRoadType = typeFactory.createFeatureType(new QName(namespace, "road"), types, null);

        gf = new GeometryFactory();

        roadFeatures = new SimpleFeature[3];

        //           3,2
        //  2,2 +-----+-----+ 4,2
        //     /     rd1     \
        // 1,1+               +5,1
        roadFeatures[0] = attFactory.create(roadType, "road.rd1", new Object[] {
                new Integer(1),
                line(new int[] { 1, 1, 2, 2, 4, 2, 5, 1 }),
                "r1",
            }
        );

        //       + 3,4
        //       + 3,3
        //  rd2  + 3,2
        //       |
        //    3,0+
        roadFeatures[1] = attFactory.create(roadType, "road.rd2", new Object[] {
                new Integer(2), line(new int[] { 3, 0, 3, 2, 3, 3, 3, 4 }),
                "r2"
            }
        );

        //     rd3     + 5,3
        //            / 
        //  3,2 +----+ 4,2
        roadFeatures[2] = attFactory.create(roadType, "road.rd3", new Object[] {
                new Integer(3),
                line(new int[] { 3, 2, 4, 2, 5, 3 }), "r3"
            }
        );
        roadBounds = new Envelope();
        roadBounds.expandToInclude( roadFeatures[0].getBounds() );
        roadBounds.expandToInclude( roadFeatures[1].getBounds() );
        roadBounds.expandToInclude( roadFeatures[2].getBounds() );
        
        FilterFactory factory = FilterFactory.createFilterFactory();
        rd1Filter = factory.createFidFilter("road.rd1");
        rd2Filter = factory.createFidFilter("road.rd2");

        FidFilter create = factory.createFidFilter();
        create.addFid("road.rd1");
        create.addFid("road.rd2");
        
        rd12Filter = create;
        
        rd12Bounds = new Envelope();
        rd12Bounds.expandToInclude(roadFeatures[0].getBounds());
        rd12Bounds.expandToInclude(roadFeatures[1].getBounds());        
        //   + 2,3
        //  / rd4
        // + 1,2
        newRoad = attFactory.create(roadType, "road.rd4",new Object[] {
                    new Integer(4), line(new int[] { 1, 2, 2, 3 }), "r4"
                });

        types.clear();
        types.add(typeFactory.createType("id", Integer.class));
        types.add(typeFactory.createType("geom", MultiLineString.class));
        types.add(typeFactory.createType("river", String.class));
        types.add(typeFactory.createType("flow", Float.class));
        
        riverType = typeFactory.createFeatureType(new QName(namespace, "river"), types, null);
        
        /*riverType = DataUtilities.createType(namespace+".river",
                "id:0,geom:MultiLineString,river:String,flow:0.0");*/
        
        types.clear();
        types.add(typeFactory.createType("river", String.class));
        types.add(typeFactory.createType("flow", Float.class));
        subRiverType = typeFactory.createFeatureType(new QName(namespace, "river"), types, null);

        /*subRiverType = DataUtilities.createType(namespace+".river",
                "river:String,flow:0.0");*/
        
        gf = new GeometryFactory();
        riverFeatures = new Feature[2];

        //       9,7     13,7
        //        +------+
        //  5,5  /
        //  +---+ rv1
        //   7,5 \
        //    9,3 +----+ 11,3
        riverFeatures[0] = attFactory.create(riverType, "river.rv1", new Object[] {
                    new Integer(1),
                    lines(new int[][] {
                            { 5, 5, 7, 4 },
                            { 7, 5, 9, 7, 13, 7 },
                            { 7, 5, 9, 3, 11, 3 }
                        }), "rv1", new Double(4.5)
                });

        //         + 6,10    
        //        /
        //    rv2+ 4,8
        //       |
        //   4,6 +
        riverFeatures[1] = attFactory.create(riverType, "river.rv2", new Object[] {
                    new Integer(2),
                    lines(new int[][] {
                            { 4, 6, 4, 8, 6, 10 }
                        }), "rv2", new Double(3.0)
                });
        riverBounds = new Envelope();
        riverBounds.expandToInclude( riverFeatures[0].getBounds());
        riverBounds.expandToInclude( riverFeatures[1].getBounds());
                
        rv1Filter = FilterFactory.createFilterFactory().createFidFilter("river.rv1");

        //  9,5   11,5   
        //   +-----+
        //     rv3  \ 
        //           + 13,3
        //                     
        newRiver = attFactory.create(riverType, "river.rv3", new Object[] {
                new Integer(3),
                lines(new int[][] {
                        { 9, 5, 11, 5, 13, 3 }
                    }), "rv3", new Double(1.5)
            }
        );
        
        types.clear();
        types.add(typeFactory.createType("id", Integer.class));
        types.add(typeFactory.createType(new QName("geom"), Polygon.class, false, true, null ));
        types.add(typeFactory.createType("name", String.class));

        lakeType = typeFactory.createFeatureType(new QName(namespace, "lake"), types, null);
        /*lakeType = DataUtilities.createType(namespace+".lake",
                    "id:0,geom:Polygon:nillable,name:String");*/
        
        lakeFeatures = new SimpleFeature[1];
        //             + 14,8
        //            / \
        //      12,6 +   + 16,6
        //            \  | 
        //        14,4 +-+ 16,4
        //
        lakeFeatures[0] = attFactory.create(lakeType, "lake.lk1", new Object[]{
                new Integer(0),
                polygon( new int[]{ 12,6, 14,8, 16,6, 16,4, 14,4, 12,6} ),
                "muddy"
            }
        );
        lakeBounds = new Envelope();
        lakeBounds.expandToInclude(lakeFeatures[0].getBounds());                 
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        gf = null;
        roadType = null;
        subRoadType = null;
        roadFeatures = null;
        roadBounds = null;
        rd1Filter = null;
        rd2Filter = null;
        newRoad = null;
        riverType = null;
        subRiverType = null;     
        riverFeatures = null;
        riverBounds = null;
        rv1Filter = null;
        newRiver = null;                    
    }
    
    public LineString line(int[] xy) {
        Coordinate[] coords = new Coordinate[xy.length / 2];

        for (int i = 0; i < xy.length; i += 2) {
            coords[i / 2] = new Coordinate(xy[i], xy[i + 1]);
        }

        return gf.createLineString(coords);
    }

    public MultiLineString lines(int[][] xy) {
        LineString[] lines = new LineString[xy.length];

        for (int i = 0; i < xy.length; i++) {
            lines[i] = line(xy[i]);
        }

        return gf.createMultiLineString(lines);
    }
    
    public Polygon polygon( int[] xy ){
        LinearRing shell = ring( xy );        
        return gf.createPolygon( shell, null );        
    }

    public Polygon polygon( int[] xy, int []holes[] ){
        if( holes == null || holes.length == 0){
           return polygon( xy );
        }
        LinearRing shell = ring( xy );        
        
        LinearRing[] rings = new LinearRing[holes.length];

        for (int i = 0; i < xy.length; i++) {
            rings[i] = ring(holes[i]);
        }        
        return gf.createPolygon( shell, rings );        
    }
        
    public LinearRing ring( int[] xy ){
        Coordinate[] coords = new Coordinate[xy.length / 2];

        for (int i = 0; i < xy.length; i += 2) {
            coords[i / 2] = new Coordinate(xy[i], xy[i + 1]);
        }

        return gf.createLinearRing(coords);        
    }
    //  need to special case Geometry
    protected void assertEquals(Geometry expected, Geometry actual) {
        if (expected == actual) {
            return;
        }
        assertNotNull(expected);
        assertNotNull(actual);
        assertTrue(expected.equals(actual));
    }
    protected void assertEquals(String message, Geometry expected, Geometry actual) {
        if (expected == actual) {
            return;
        }
        assertNotNull(message, expected);
        assertNotNull(message, actual);
        assertTrue(message, expected.equals(actual));
    }
    /**
     * Counts the number of Features returned by reader.
     * <p>
     * This method will close reader
     * </p>
     */
    /*
    protected int count( FeatureReader reader ) throws IOException {
        if( reader == null) {
            return -1;
        }             
        int count = 0;
        try {
            while( reader.hasNext() ){
                reader.next();
                count++;
            }
        } catch (NoSuchElementException e) {
            // bad dog!
            throw new DataSourceException("hasNext() lied to me at:"+count, e );
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("next() could not understand feature at:"+count, e );
        }        
        finally {
            reader.close();
        }
        return count;
    }
    protected int count(FeatureWriter writer)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        int count = 0;

        try {
            while (writer.hasNext()) {
                writer.next();
                count++;
            }
        } finally {
            writer.close();
        }

        return count;
    } 
    */              
}
