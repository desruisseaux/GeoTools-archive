package org.geotools.renderer.shape;

import java.awt.geom.AffineTransform;

import junit.framework.TestCase;

import org.geotools.filter.BBoxExpression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.LiteralExpression;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class FilterTransformerTest extends TestCase {


    private FilterTransformer filterTransformer;
    private final FilterFactory filterFactory=FilterFactoryFinder.createFilterFactory();
    protected void setUp() throws Exception {
        super.setUp();
        AffineTransform t=AffineTransform.getTranslateInstance(10,10);
        DefaultMathTransformFactory fac=new DefaultMathTransformFactory();
        MathTransform mt = fac.createAffineTransform(new GeneralMatrix(t));
        filterTransformer=new FilterTransformer(mt);
    }
    
    public void testVisitBBoxExpression() throws Exception {
        Envelope envelope = new Envelope(0,10,0,10);
        GeometryFactory geometryFactory=new GeometryFactory();
        LiteralExpression lit=filterFactory.createLiteralExpression(geometryFactory.toGeometry(envelope));
        lit.accept(filterTransformer);
        assertTrue(geometryFactory.toGeometry(new Envelope(10,20,10,20)).equals((Geometry)lit.getLiteral()));
    }
    
    public void testVisitCompareFilter() throws Exception {
        Envelope envelope = new Envelope(0,10,0,10);
        GeometryFactory geometryFactory=new GeometryFactory();
        org.geotools.filter.GeometryFilter filter=filterFactory.createGeometryFilter(FilterType.GEOMETRY_BBOX);
        
        BBoxExpression bboxFilter = filterFactory.createBBoxExpression(envelope);
        filter.addLeftGeometry(bboxFilter);
        filter.addRightGeometry(filterFactory.createAttributeExpression("geom"));
        filter.accept(filterTransformer);
        assertTrue(geometryFactory.toGeometry(new Envelope(10,20,10,20)).equals((Geometry)bboxFilter.getLiteral()));
    }
    
    public void testVisitORFilter() throws Exception {
        GeometryFactory geometryFactory=new GeometryFactory();
        org.geotools.filter.GeometryFilter one=filterFactory.createGeometryFilter(FilterType.GEOMETRY_BBOX);
        BBoxExpression bboxFilter = filterFactory.createBBoxExpression(new Envelope(0,10,0,10));
        one.addLeftGeometry(bboxFilter);
        one.addRightGeometry(filterFactory.createAttributeExpression("geom"));
        
        org.geotools.filter.GeometryFilter two=filterFactory.createGeometryFilter(FilterType.GEOMETRY_BBOX);
        BBoxExpression bboxFilter2 = filterFactory.createBBoxExpression(new Envelope(20,10,20,10));
        two.addLeftGeometry(bboxFilter2);
        two.addRightGeometry(filterFactory.createAttributeExpression("geom2"));
        
        // TEST filter.None because it was a bug before
        one.or(two).or(Filter.NONE).accept(filterTransformer);
        assertTrue(geometryFactory.toGeometry(new Envelope(10,20,10,20)).equals((Geometry)bboxFilter.getLiteral()));
        assertTrue(geometryFactory.toGeometry(new Envelope(30,20,30,20)).equals((Geometry)bboxFilter2.getLiteral()));
    }

}
