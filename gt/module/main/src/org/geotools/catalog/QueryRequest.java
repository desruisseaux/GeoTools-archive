package org.geotools.catalog;

import java.net.URI;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.opengis.catalog.MetadataEntity;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Represents a query against metadata.
 * <p>
 * This class represents a strong extention of the existing geotools Filter and Expression
 * construct. I have chosen the name Query to agree with the origional meaning of the data.Query
 * which represented a metadata query matching the metadata typeName and a Feature Filter on the
 * resulting FeatureCollection. 
 * </p>
 * <p>
 * Another way of thinking about this is that it offers a walk of the Expr construct
 * from the persepective of Metadata. I noticed that although Expression and Filter
 * are defined as interfaces I have only ever seen one implementation. The *magic*
 * of data specific behaviour seems to occur only when walking the tree, rather than
 * implementing a custom FilterFactory (& thus custom tree mnodes).
 * </p>
 * <p>
 * So this class provides a tree construct for a QueryWalker to navigate through,
 * I am folloing the direct example of Filter and Expression here (even though
 * I think there may be a better way).
 * </p>
 * <p>
 * Q: Should this class turn itself into a data.Query? Or should I turn data.Query into a subclass.
 * </p>
 * @UML CG_CATALOG
 * @author Jody Garnett, Refractions Research
 * @version 2.1
 */
public class QueryRequest {
//	Expr expr;
    public static QueryRequest ALL = new QueryRequest( null ) {
        public boolean accepts( MetadataEntity meta ) {
            return true;
        }
    };
//  public QueryRequest( Expr expr ){
//	this.expr = expr;
//}	
    public QueryRequest( Filter expr ){
        this.filter = expr;
    }
    Filter filter;
    public boolean match( Object bean ) {
        if( bean instanceof MetadataEntity ) {
            return matchMetadata( (MetadataEntity) bean );
        }        
        else {
            return matchBean( bean );
        }        
    }
    protected boolean matchBean( Object bean ) {
        return false; // need Jese to implement XPATH bean lookup
    }
    protected boolean matchMetadata( MetadataEntity meta ) {
//        Expr query = expr.resolve( meta );
//        Filter filter;
//        try {
//            filter = query.filter( fakeFeatureType );
            return filter.contains( fakeFeature );            
//        } catch (IOException e) {
//            return false;
//        }        
	}
	static FeatureType fakeFeatureType = new FeatureType(){
		public URI getNamespace() {
			return null;
		}
		public String getTypeName() {
			return null;
		}
		public AttributeType[] getAttributeTypes() {
			return new AttributeType[0];
		}
		public boolean hasAttributeType(String xPath) {
			return false;
		}
		public AttributeType getAttributeType(String xPath) {
			return null;
		}
		public int find(AttributeType type) {
			return -1;
		}
		public GeometryAttributeType getDefaultGeometry() {
			return null;
		}
		public int getAttributeCount() {
			return 0;
		}
		public AttributeType getAttributeType(int position) {
			return null;
		}
		public boolean isDescendedFrom(URI nsURI, String typeName) {
			return false;
		}
		public boolean isDescendedFrom(FeatureType type) {
			return false;
		}
		public boolean isAbstract() {
			return false;
		}
		public FeatureType[] getAncestors() {
			return null;
		}
		public Feature duplicate(Feature feature) throws IllegalAttributeException {
			return null;
		}
		public Feature create(Object[] attributes) throws IllegalAttributeException {
			return null;
		}
		public Feature create(Object[] attributes, String featureID) throws IllegalAttributeException {
			return null;
		}
		public int find(String attName) {
			// TODO Auto-generated method stub
			return 0;
		}
	};
	static Feature fakeFeature = new Feature(){
		public FeatureCollection getParent() {
			return null;
		}
		public void setParent(FeatureCollection collection) {
		}
		public FeatureType getFeatureType() {
			return fakeFeatureType;
		}
		public String getID() {
			return "query";
		}

		public Object[] getAttributes(Object[] attributes) {
			return new Object[0];
		}
		public Object getAttribute(String xPath) {
			return null;
		}
		public Object getAttribute(int index) {
			return null;
		}
		public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
		}
		public int getNumberOfAttributes() {
			return 0;
		}
		public void setAttributes(Object[] attributes) throws IllegalAttributeException {
		}
		public void setAttribute(String xPath, Object attribute) throws IllegalAttributeException {
		}
		public Geometry getDefaultGeometry() {
			return null;
		}
		public void setDefaultGeometry(Geometry geometry) throws IllegalAttributeException {
		}
		public Envelope getBounds() {
			return null;
		}		
	};
}
