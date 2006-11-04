package org.geotools.data.view;

import java.io.IOException;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.CompareFilter;
import org.opengis.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class DefaultViewTest extends TestCase {

    String typeName = "type1";
    private MemoryDataStore ds;

    protected void setUp() throws Exception {
        super.setUp();
        FeatureType ft=DataUtilities.createType(typeName, "geom:Point,name:String,id:int");
        ds=new MemoryDataStore();
        ds.addFeature(createFeatures(ft,1));
        ds.addFeature(createFeatures(ft,2));
        ds.addFeature(createFeatures(ft,3));
        ds.addFeature(createFeatures(ft,4));
    }

    private Feature createFeatures(FeatureType ft, int i) throws IllegalAttributeException {
        GeometryFactory fac=new GeometryFactory();
        return ft.create(new Object[]{
                fac.createPoint(new Coordinate(i,i)),
                "name"+i, 
                new Integer(i)
            }
        );
    }

    public void testGetFeatures() throws Exception {

        FeatureSource view = getView();
        
        FeatureIterator features = view.getFeatures().features();
        int count=0;
        while( features.hasNext() ){
            count++;
            features.next();
        }
        
        assertEquals(2, count);
    }

    public void testGetFeaturesQuery() throws Exception {

        FeatureSource view = getView();
        
        FeatureIterator features = view.getFeatures(getQuery()).features();
        int count=0;
        while( features.hasNext() ){
            count++;
            features.next();
        }
        
        assertEquals(1, count);
    }
    public void testGetFeaturesFilter() throws Exception {

        FeatureSource view = getView();
        CompareFilter f = getFilter();
        FeatureIterator features = view.getFeatures(f).features();
        int count=0;
        while( features.hasNext() ){
            count++;
            features.next();
        }
        
        assertEquals(1, count);
    }
  
    public void testGetCount() throws Exception {
        FeatureSource view = getView();
        
        DefaultQuery defaultQuery = getQuery();
        int count = view.getCount(defaultQuery);
        assertEquals(1, count);
    }

    private DefaultQuery getQuery() throws IllegalFilterException {
        CompareFilter f = getFilter();
        DefaultQuery defaultQuery = new DefaultQuery(typeName, f, new String[0]);
        return defaultQuery;
    }

    private CompareFilter getFilter() throws IllegalFilterException {
        FilterFactory fac=FilterFactoryFinder.createFilterFactory();
        CompareFilter f = fac.createCompareFilter(FilterType.COMPARE_EQUALS);
        f.addLeftValue(fac.createAttributeExpression("name"));
        f.addRightValue(fac.createLiteralExpression("name2"));
        return f;
    }

    private FeatureSource getView() throws IllegalFilterException, IOException, SchemaException {
        FilterFactory fac=FilterFactoryFinder.createFilterFactory();
        CompareFilter f = fac.createCompareFilter(FilterType.COMPARE_LESS_THAN);
        f.addLeftValue(fac.createAttributeExpression("id"));
        f.addRightValue(fac.createLiteralExpression(3));
        
        FeatureSource view = ds.getView(new DefaultQuery(typeName, f));
        return view;
    }

}
