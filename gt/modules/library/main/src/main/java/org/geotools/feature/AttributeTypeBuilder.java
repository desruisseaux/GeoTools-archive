package org.geotools.feature;

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Builder for attribute types and descriptors.
 * <p>
 * Building an attribute type:
 * <pre>
 * <code>
 *  //create the builder
 * 	AttributeTypeBuilder builder = new AttributeTypeBuilder();
 *  
 *  //set type information
 *  builder.setName( "intType" ):
 *  builder.setBinding( Integer.class );
 *  builder.setNillable( false );
 *  
 *  //build the type
 *  AttributeType type = builder.buildType();
 * </code>
 * </pre>
 * </p>
 * <p>
 * Building an attribute descriptor:
 * <pre>
 * <code>
 *  //create the builder
 * 	AttributeTypeBuilder builder = new AttributeTypeBuilder();
 *  
 *  //set type information
 *  builder.setName( "intType" ):
 *  builder.setBinding( Integer.class );
 *  builder.setNillable( false );
 *  
 *  //set descriptor information
 *  builder.setMinOccurs(0);
 *  builder.setMaxOccurs(1);
 *  builder.setNillable(true);
 *  
 *  //build the descriptor
 *  AttributeDescriptor descriptor = builder.buildDescriptor("intProperty");
 * </code>
 * </pre>
 * <p>
 * This class maintains state and is not thread safe.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class AttributeTypeBuilder {

	/**
	 * factory
	 */
	protected FeatureTypeFactory factory;
	
	//AttributeType
	//
	/**
	 * name
	 */
	protected String name;
	/**
	 * namespace uri
	 */
	protected String namespaceURI;
	/**
	 * abstract flag
	 */
	protected boolean isAbstract = false;
	/**
	 * restrictions
	 */
	protected List/*<Filter>*/ restrictions;
	/**
	 * string description
	 */
	protected String description;
	/**
	 * identifiable flag
	 */
	protected boolean isIdentifiable = false;
	/**
	 * bound java class
	 */
	protected Class binding;
	/**
	 * super type
	 */
	protected AttributeType superType;
	/**
	 * default value
	 */
	protected Object defaultValue;
	
	//GeometryType
	//
	protected CoordinateReferenceSystem crs;
	
	//AttributeDescriptor
	//
	/**
	 * min occurs
	 */
	protected int minOccurs = 1;
	/**
	 * max occurs
	 */
	protected int maxOccurs = 1;
	/**
	 * nullable
	 */
	protected boolean isNillable = true;
	
	
	/**
	 * Constructs the builder.
	 *
	 */
	public AttributeTypeBuilder() {
		this( new FeatureTypeFactoryImpl() );
	}
	
	/**
	 * Constructs the builder specifying the factory used to build attribute 
	 * types.
	 * 
	 */
	public AttributeTypeBuilder( FeatureTypeFactory factory ) {
		this.factory = factory;
	}
	
	/**
	 * Resets all builder state used to build the attribute type.
	 * <p>
	 * This method is called automatically after {@link #buildType()} and 
	 * {@link #buildGeometryType()}.
	 * </p>
	 */
	protected void resetTypeState() {
		name = null;
		namespaceURI = null;
		isAbstract = false;
		restrictions = null;
		description = null;
		isIdentifiable = false;
		binding = null;
		superType = null;
		crs = null;
	}
	
	protected void resetDescriptorState() {
		minOccurs = 1;
		maxOccurs = 1;
		isNillable = true;
	}
	
	public void setFactory(FeatureTypeFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * Initializes builder state from another attribute type.
	 */
	public void init( AttributeType type ) {
		name = type.getName().getLocalPart();
		namespaceURI = type.getName().getNamespaceURI();
		isAbstract = type.isAbstract();
		
		if ( type.getRestrictions() != null ) {
			restrictions().addAll( type.getRestrictions() );	
		}
		
		description = type.getDescription() != null ? type.getDescription().toString() : null;
		isIdentifiable = type.isIdentified();
		binding = type.getBinding();
		superType = type.getSuper();
		
		if ( type instanceof GeometryType ) {
			crs = ((GeometryType)type).getCRS();
		}
	}
	
	/**
	 * Initializes builder state from another attribute descriptor.
	 */

	public void init( AttributeDescriptor descriptor ) {
		init( descriptor.getType() );
		minOccurs = descriptor.getMinOccurs();
		maxOccurs = descriptor.getMaxOccurs();
		isNillable = descriptor.isNillable();
	}
	
	// Type methods
	//
	
	public void setBinding(Class binding) {
		this.binding = binding;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}
	
	public void setCRS(CoordinateReferenceSystem crs) {
		this.crs = crs;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public void setIdentifiable(boolean isIdentifiable) {
		this.isIdentifiable = isIdentifiable;
	}

	public void addRestriction(Filter restriction) {
		restrictions().add(restriction);
	}
	
	// Descriptor methods
	//
	
	public void setNillable(boolean isNillable) {
		this.isNillable = isNillable;
	}

	public void setMaxOccurs(int maxOccurs) {
		this.maxOccurs = maxOccurs;
	}

	public void setMinOccurs(int minOccurs) {
		this.minOccurs = minOccurs;
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	// construction methods
	//
	
	/**
	 * Builds the attribute type.
	 * <p>
	 * This method resets all state after the attribute is built.
	 * </p>
	 */
	public AttributeType buildType() {
		AttributeType type = factory.createAttributeType(
			new org.geotools.feature.Name(namespaceURI,name), binding, isIdentifiable, isAbstract, 
			restrictions, superType, description != null ? new SimpleInternationalString(description) : null);
		resetTypeState();
		
		return type;
	}
	
	/**
	 * Builds the geometry attribute type.
	 * <p>
	 * This method resets all state after the attribute is built.
	 * </p>
	 */
	public GeometryType buildGeometryType() {
		GeometryType type = factory.createGeometryType(
			new org.geotools.feature.Name(namespaceURI,name), binding, crs, isIdentifiable, isAbstract, 
			restrictions, superType, description != null ? new SimpleInternationalString(description) : null);
		
		resetTypeState();
		
		return type;
	}

	/**
	 * Builds an attribute descriptor first building an attribute type from 
	 * internal state.
	 * <p>
	 * If {@link #crs} has been set via {@link #setCRS(CoordinateReferenceSystem)}
	 * the internal attribute type will be built via {@link #buildGeometryType()}, 
	 * otherwise it will be built via {@link #buildType()}.
	 * </p>
	 * <p>
	 * This method calls through to {@link #buildDescriptor(String, AttributeType)}.
	 * </p>
	 * @param name The name of the descriptor.
	 * 
	 * @see #buildDescriptor(String, AttributeType) 
	 */
	public AttributeDescriptor buildDescriptor( String name ) {
		if ( crs != null ) {
			return buildDescriptor(name, buildGeometryType());
		}
		else {
			return buildDescriptor(name, buildType());
		}
	}
	
	/**
	 * Builds an attribute descriptor specifying its attribute type.
	 * <p>
	 * Internal state is reset after the descriptor is built.
	 * </p>
	 * @param name The name of the descriptor.
	 * @param type The type referenced by the descriptor.
	 *
	 */
	public AttributeDescriptor buildDescriptor(String name, AttributeType type) {
		return buildDescriptor(new org.geotools.feature.Name(name), type );
	}
	   
    /**
     * Builds a geometry descriptor specifying its attribute type.
     * <p>
     * Internal state is reset after the descriptor is built.
     * </p>
     * @param name The name of the descriptor.
     * @param type The geometry type referenced by the descriptor.
     *
     */
	public GeometryDescriptor buildDescriptor(String name, GeometryType type) {
	    return buildDescriptor( new org.geotools.feature.Name(name), type );
	}
	
	public AttributeDescriptor buildDescriptor(Name name, AttributeType type ) {
		AttributeDescriptor descriptor = factory.createAttributeDescriptor(
			type, name, minOccurs, maxOccurs, isNillable, defaultValue);
	
		resetDescriptorState();
		return descriptor;
	}
	
	public GeometryDescriptor buildDescriptor(Name name, GeometryType type ) {
	    GeometryDescriptor descriptor = factory.createGeometryDescriptor(
            type, name, minOccurs, maxOccurs, isNillable, defaultValue);
    
        resetDescriptorState();
        return descriptor;
    }
	
	// internal / subclass api
	//
	
	protected List restrictions() {
		if (restrictions == null ) {
			restrictions = new ArrayList();		
		}
		
		return restrictions;
	}
	
}
