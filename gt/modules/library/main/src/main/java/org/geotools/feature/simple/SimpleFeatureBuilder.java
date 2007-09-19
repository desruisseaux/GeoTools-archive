package org.geotools.feature.simple;

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.feature.FeatureFactoryImpl;
import org.geotools.util.Converters;
import org.opengis.feature.Attribute;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
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
public class SimpleFeatureBuilder {
	/**
	 * logger
	 */
    static Logger LOGGER = Logger.getLogger("org.geotools.feature");
    
	/**
	 * factory
	 */
	protected FeatureFactory factory;
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
	//protected CoordinateReferenceSystem crs;
	
	/**
	 * Constructs the builder.
	 */
	public SimpleFeatureBuilder() {
		this( new FeatureFactoryImpl());
	}
	
	/**
	 * Constructs the builder specifying the factory to use for creating features.
	 */
    public SimpleFeatureBuilder(FeatureFactory factory) {
    	this.factory = factory;
	}

    /**
     * Sets the factory used to create features.
     */
	public void setFeatureFactory(FeatureFactory factory) {
		this.factory = factory;
	}

    public void setType( SimpleFeatureType featureType ){
    	this.featureType = featureType; 
    }
    
//    public void setCRS(CoordinateReferenceSystem crs) {
//        this.crs = crs;
//    }

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
		for ( int i = 0; i < feature.getAttributeCount(); i++ ) {
			add( feature.getAttribute( i ) );
		}
		//defaultGeometry = feature.getDefaultGeometryProperty();
		//crs = feature.getType().getCRS();
	}
    
    protected void init() {
    	attributes = null;
    }
    
    public void add(Object value) {
    	//get the descriptor from the type
    	AttributeDescriptor descriptor = featureType.getAttribute(attributes().size());
    	Attribute attribute = null;
    	
    	//make sure the type of the value and the binding of the type match up
    	if ( value != null ) {
    	    Class target = descriptor.getType().getBinding(); 
    	    if ( !target.isAssignableFrom(value.getClass()) ) {
    	        //try to convert
    	        LOGGER.fine("value: " + value + " does not match type: " + target.getName() + ". Converting.");
    	        Object converted = Converters.convert(value, target);
    	        if ( converted != null ) {
    	            value = converted;
    	        }
    	    }
    	}
    	if ( descriptor instanceof GeometryDescriptor ) {
    		//TODO: set crs on teh builder
    		attribute = factory.createGeometryAttribute(value, (GeometryDescriptor) descriptor, null, featureType.getCRS() );
    		
    		//is this the default geometry?
    		if ( descriptor.equals( featureType.getDefaultGeometry() ) ) {
    			defaultGeometry = (GeometryAttribute) attribute;
    		}
    	}
    	else {
    		attribute = factory.createAttribute( value, descriptor, null ) ;
    	}
    	
    	//add it
    	attributes().add(attributes().size(),attribute);
	}
    
    public void add(Object[] values ) {
    	if ( values == null ) {
    		return;
    	}
    	for ( int i = 0; i < values.length; i++) {
    		add( values[ i ] );
    	}
    }
    
    public void set(String name, Object value) {
        int index = featureType.indexOf(name);
        if (index < attributes().size()) {
            //already an attribute for this index
            Attribute attribute = (Attribute) attributes().get(index);
            attribute.setValue(value);
        }
        else {
            //expand the list of attributes up to the index
            while(attributes.size() < index ) {
                add((Object)null);
            }
            
            add(value);
        }
    }
    
    protected boolean isGeometry( AttributeDescriptor value ) {
    	return value instanceof GeometryDescriptor;
    }
    
    protected String createDefaultFeatureId() {
    	  // According to GML and XML schema standards, FID is a XML ID
        // (http://www.w3.org/TR/xmlschema-2/#ID), whose acceptable values are those that match an
        // NCNAME production (http://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName):
        // NCName ::= (Letter | '_') (NCNameChar)* /* An XML Name, minus the ":" */
        // NCNameChar ::= Letter | Digit | '.' | '-' | '_' | CombiningChar | Extender
        // We have to fix the generated UID replacing all non word chars with an _ (it seems
        // they area all ":")
        //return "fid-" + NON_WORD_PATTERN.matcher(new UID().toString()).replaceAll("_");
        // optimization, since the UID toString uses only ":" and converts long and integers
        // to strings for the rest, so the only non word character is really ":"
        return "fid-" + new UID().toString().replace(':', '_');
    }
    
    public SimpleFeature build(String id) {
        //ensure id
        if ( id == null ) {
            id = createDefaultFeatureId();
        }
        
        //ensure they specified enough values
        int n = featureType.getAttributeCount();
        while( attributes().size() < n ) {
            add((Object)null);
        }
        
        //build the feature
    	SimpleFeature feature = factory.createSimpleFeature( attributes, featureType, id );
    	if ( defaultGeometry != null ) {
    	    feature.setDefaultGeometryProperty(defaultGeometry);
    	}
    	
    	init();
    	return feature;
    }
    
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
