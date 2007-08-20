package org.geotools.feature.simple;

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.geotools.feature.Feature;
import org.opengis.feature.Attribute;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
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
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
	
	/**
	 * factory
	 */
	protected SimpleFeatureFactory factory;
	/**
	 * list of attributes
	 */
	protected List attributes = new ArrayList();
	/**
	 * feaure type
	 */
	protected SimpleFeatureType featureType;
	/**
	 * default geometry
	 */
	protected GeometryAttribute defaultGeometry;
	/**
	 * coordinate reference system
	 */
	protected CoordinateReferenceSystem crs;
	
	/**
	 * Constructs the builder.
	 */
	public SimpleFeatureBuilder() {
		this( new SimpleFeatureFactoryImpl());
	}
	
	/**
	 * Constructs the builder specifying the factory to use for creating features.
	 */
    public SimpleFeatureBuilder(SimpleFeatureFactory factory) {
    	this.factory = factory;
	}

    /**
     * Sets the factory used to create features.
     */
	public void setSimpleFeatureFactory(SimpleFeatureFactory factory) {
		this.factory = (SimpleFeatureFactoryImpl) factory;
	}

    public void setType( SimpleFeatureType featureType ){
    	this.featureType = featureType; 
    }
    
    public void setType( SimpleFeatureCollectionType collectionType ){
    	//this.collectionType = collectionType;
    }
    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * Initialize the builder with the provided feature.
     * <p>
     * This is used to quickly create a "clone", can be used to change
     * between one SimpleFeatureImplementation and another.
     * </p>
     * @param feature
     */
    public void init( SimpleFeature feature ) {
		init();
		this.featureType = (SimpleFeatureType) feature.getType();
		for ( int i = 0; i < feature.getNumberOfAttributes(); i++ ) {
			add( feature.getValue( i ) );
		}
		defaultGeometry = feature.getDefaultGeometry();
		crs = feature.getCRS();
	}
    
    protected void init() {
    	attributes = null;
    }
    
    public void add(Object value) {
    	//get the descriptor from the type
    	AttributeDescriptor descriptor = featureType.getAttribute(attributes().size());
    	Attribute attribute = null;
    	
    	if ( isGeometry( descriptor ) ) {
    		//TODO: set crs on teh builder
    		attribute = factory.createGeometryAttribute(value, descriptor, null, featureType.getCRS() );
    		
    		//is this the default geometry?
    		if ( descriptor.equals( featureType.getDefaultGeometry() ) ) {
    			defaultGeometry = (GeometryAttribute) attribute;
    		}
    	}
    	else {
    		attribute = factory.createAttribute( value, descriptor, null ) ;
    	}
    	
    	//add it
		attributes().add(attribute);
	}
    
    public void add(Object[] values ) {
    	if ( values == null ) {
    		return;
    	}
    	for ( int i = 0; i < values.length; i++) {
    		add( values[ i ] );
    	}
    }
    
    public Object build( String id ){
    	if( featureType != null ){
    		if ( id == null ) {
    			id = createDefaultFeatureId();
    		}
    		return feature( id );
    	}

    	
    	return null;
    }
 
    protected boolean isGeometry( AttributeDescriptor value ) {
    	return value.getType() instanceof GeometryType;
    }
    
    protected String createDefaultFeatureId() {
    	  // According to GML and XML schema standards, FID is a XML ID
        // (http://www.w3.org/TR/xmlschema-2/#ID), whose acceptable values are those that match an
        // NCNAME production (http://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName):
        // NCName ::= (Letter | '_') (NCNameChar)* /* An XML Name, minus the ":" */
        // NCNameChar ::= Letter | Digit | '.' | '-' | '_' | CombiningChar | Extender
        // We have to fix the generated UID replacing all non word chars with an _ (it seems
        // they area all ":")
//        return "fid-" + NON_WORD_PATTERN.matcher(new UID().toString()).replaceAll("_");
        // optimization, since the UID toString uses only ":" and converts long and integers
        // to strings for the rest, so the only non word character is really ":"
        return "fid-" + new UID().toString().replace(':', '_');
    }
    
    public SimpleFeature feature(String id) {
    	SimpleFeature feature = factory.createSimpleFeature( attributes, featureType, id );
    	feature.setDefaultGeometry(defaultGeometry);
    	
    	//check for crs
    	if ( crs != null ) {
    	    feature.setCRS(crs);
    	}
    	else {
    	    //if no crs set, use the one from the defaultGeometry type
    	    if ( defaultGeometry != null ) {
    	        feature.setCRS(defaultGeometry.getCRS());
    	    }
    	}
    	
    	init();
    	return feature;
    }
    
//    public SimpleFeatureCollection collection(String id) {
//    	return factory.createSimpleFeatureCollection( collectionType, id );
//    }
    
//    protected Attribute create(
//            Object value, AttributeType type, AttributeDescriptor descriptor, String id
//        ) {        
//            if (descriptor != null) {
//                type = descriptor.getType();
//            }
//            
//            Attribute attribute = null;
//            if (type instanceof SimpleFeatureCollectionType) {
//                attribute = factory.createSimpleFeatureCollection((SimpleFeatureCollectionType)type,id);
//            }
//            else if (type instanceof FeatureCollectionType) {
//                attribute =  descriptor != null ? 
//                    factory.createFeatureCollection((Collection)value,descriptor,id) :
//                    factory.createFeatureCollection((Collection)value,(FeatureCollectionType)type,id);
//            }
//            else if (type instanceof SimpleFeatureType) {
//                attribute =  factory.createSimpleFeature( (SimpleFeatureType) type, id, null );
//            }
//            else if (type instanceof FeatureType) {
//                attribute = descriptor != null ? 
//                    factory.createFeature((Collection)value,descriptor,id) :
//                    factory.createFeature((Collection)value,(FeatureType)type,id);
//            }
//            else if (type instanceof ComplexType) {
//                attribute = descriptor != null ?
//                    factory.createComplexAttribute((Collection)value, descriptor, id) : 
//                    factory.createComplexAttribute((Collection)value, (ComplexType)type,id);
//            }
//            else if (type instanceof GeometryType) {
//                attribute = factory.createGeometryAttribute(value,descriptor,id,null);
//            }
//            else {
//                //use a normal attribute builder to create a "primitive" type
//                
//                //use the binding to create specific "simple" types
//                Class binding = descriptor.getType().getBinding();
//                if (Number.class.isAssignableFrom(binding)) {
//                    attribute = 
//                        factory.createNumericAttribute( (Number) value, descriptor );
//                }
//                else if (binding.isAssignableFrom(CharSequence.class)) {
//                    attribute = 
//                        factory.createTextAttribute((CharSequence)value,descriptor);
//                }
//                else if (binding.isAssignableFrom(Date.class)) {
//                    attribute = 
//                        factory.createTemporalAttribute((Date) value, descriptor);
//                }
//                else if (Boolean.class == binding) {
//                    attribute = 
//                        factory.createBooleanAttribute( (Boolean) value, descriptor );
//                }
//                else {
//                    attribute = factory.createAttribute(value,descriptor,id);    
//                }
//                
//            }
//            
//            return attribute;
//        }

   
//	public void init(SimpleFeature feature) {
//		init();
//		this.featureType = (SimpleFeatureType) feature.getType();
//		for( Iterator i=feature.attributes().iterator(); i.hasNext();){
//			this.attributes.add( i.next() ); // TODO: copy
//		}		
//	}
	
	protected List attributes() {
		if ( attributes == null ) {
			attributes = newList();
		}
		
		return attributes;
	}
	
	protected List newList() {
		return new ArrayList();
	}
}
