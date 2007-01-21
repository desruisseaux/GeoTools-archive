package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.geotools.feature.Feature;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;

/**
 * A builder used to construct an instanceof {@link org.opengis.feature.simple.SimpleFeature}.
 * <p>
 * Usage:
 * <code>
 * 	<pre>
 *  //type of features we would like to build ( assume schema = (geom:Point,name:String) )
 *  SimpleFeatureType featureType = ...  
 * 
 *  SimpleFeatureFactory simpleFactory = ...  //factory used to build feature instances
 * 
 *  //create the builder
 *  SimpleFeatureBuilder builder = new SimpleFeatureBuilder( simpleFactory );
 *  
 *  //set hte type of created features
 *  builder.setType( featureType );
 *  
 *  //add the attributes
 *  builder.add( new Point( 0 , 0 ) );
 *  builder.add( "theName" );
 *  
 *  //build the feature
 *  SimpleFeature feature = builder.build( "fid" );
 * 	</pre>
 * </code>
 * </p>
 * 
 * @author Justin Deoliveira
 * @author Jody Garnett
 */
public class SimpleFeatureBuilder  {
	
	List attributes = new ArrayList();
	private SimpleFeatureFactoryImpl factory;
	//private SimpleFeatureFactory factory;
	private org.geotools.feature.FeatureType featureType;
	//private SimpleFeatureType featureType;
	private SimpleFeatureCollectionType collectionType;
	
    public SimpleFeatureBuilder(SimpleFeatureFactory factory) {
    	this.factory = (SimpleFeatureFactoryImpl) factory;
	}
    
    /**
     * Setter injection for SimpleFeatureFactory.
     * XXX Review? If you do not mean for Setter injection please factory final
     * @param factory
     */
	public void setSimpleFeatureFactory(SimpleFeatureFactory factory) {
		this.factory = (SimpleFeatureFactoryImpl) factory;
	}

    public void init(){
    	attributes.clear();
    }
    public void setType( org.geotools.feature.FeatureType featureType ) {
    	this.featureType = featureType;
    }
//    public void setType( SimpleFeatureType featureType ){
//    	this.featureType = featureType; 
//    }
    public void setType( SimpleFeatureCollectionType collectionType ){
    	this.collectionType = collectionType;
    }
    /** Call to add the next attribute to the builder. */
    public void add(Object[] values ) {
    	for ( int i = 0; i < values.length; i++) {
    		add( values[ i ] );
    	}
    }
    
    public void add(Object value) {
		 attributes.add(value);
	}
    public Object build( String id ){
    	if( featureType != null ){
    		return feature( id );
    	}
    	if( collectionType != null){
    		return  collection( id );
    	}
    	return null;
    }
    public org.geotools.feature.Feature feature( String id ) {
    	return factory.createSimpleFeature( featureType, id, attributes.toArray() );
    }
//    public SimpleFeature feature(String id) {
//    	return factory.createSimpleFeature( featureType, id, attributes.toArray() );
//    }
    
    public SimpleFeatureCollection collection(String id) {
    	return factory.createSimpleFeatureCollection( collectionType, id );
    }
    
    protected Attribute create(
            Object value, AttributeType type, AttributeDescriptor descriptor, String id
        ) {        
            if (descriptor != null) {
                type = descriptor.getType();
            }
            
            Attribute attribute = null;
            if (type instanceof SimpleFeatureCollectionType) {
                attribute = factory.createSimpleFeatureCollection((SimpleFeatureCollectionType)type,id);
            }
            else if (type instanceof FeatureCollectionType) {
                attribute =  descriptor != null ? 
                    factory.createFeatureCollection((Collection)value,descriptor,id) :
                    factory.createFeatureCollection((Collection)value,(FeatureCollectionType)type,id);
            }
            else if (type instanceof SimpleFeatureType) {
                attribute =  factory.createSimpleFeature( (SimpleFeatureType) type, id, null );
            }
            else if (type instanceof FeatureType) {
                attribute = descriptor != null ? 
                    factory.createFeature((Collection)value,descriptor,id) :
                    factory.createFeature((Collection)value,(FeatureType)type,id);
            }
            else if (type instanceof ComplexType) {
                attribute = descriptor != null ?
                    factory.createComplexAttribute((Collection)value, descriptor, id) : 
                    factory.createComplexAttribute((Collection)value, (ComplexType)type,id);
            }
            else if (type instanceof GeometryType) {
                attribute = factory.createGeometryAttribute(value,descriptor,id,null);
            }
            else {
                //use a normal attribute builder to create a "primitive" type
                
                //use the binding to create specific "simple" types
                Class binding = descriptor.getType().getBinding();
                if (Number.class.isAssignableFrom(binding)) {
                    attribute = 
                        factory.createNumericAttribute( (Number) value, descriptor );
                }
                else if (binding.isAssignableFrom(CharSequence.class)) {
                    attribute = 
                        factory.createTextAttribute((CharSequence)value,descriptor);
                }
                else if (binding.isAssignableFrom(Date.class)) {
                    attribute = 
                        factory.createTemporalAttribute((Date) value, descriptor);
                }
                else if (Boolean.class == binding) {
                    attribute = 
                        factory.createBooleanAttribute( (Boolean) value, descriptor );
                }
                else {
                    attribute = factory.createAttribute(value,descriptor,id);    
                }
                
            }
            
            return attribute;
        }

    /**
     * Initialize the builder with the provided feature.
     * <p>
     * This is used to quickly create a "clone", can be used to change
     * between one SimpleFeatureImplementation and another.
     * </p>
     * @param feature
     */
    public void init( Feature feature ) {
		init();
		this.featureType = feature.getFeatureType();
		for ( int i = 0; i < feature.getNumberOfAttributes(); i++ ) {
			this.attributes.add( feature.getAttribute( i ) ); // TODO: copy
		}
	}
//	public void init(SimpleFeature feature) {
//		init();
//		this.featureType = (SimpleFeatureType) feature.getType();
//		for( Iterator i=feature.attributes().iterator(); i.hasNext();){
//			this.attributes.add( i.next() ); // TODO: copy
//		}		
//	}
	
	
}
