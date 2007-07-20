package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.type.TypeFactoryImpl;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Schema;
import org.opengis.feature.type.TypeFactory;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * A builder for simple feature and feature collection types.
 * <p>
 * Simple Usage:
 * <pre>
 *  <code>
 *  //create the builder
 *  SimpleTypeBuilder builder = new SimpleTypeBuilder();
 *  
 *  //set global state
 *  builder.setName( "testType" );
 *  builder.setNamespaceURI( "http://www.geotools.org/" );
 *  builder.setCRS( "EPSG:4326" );
 *  
 *  //add attributes
 *  builder.add( "intProperty", Integer.class );
 *  builder.add( "stringProperty", String.class );
 *  builder.add( "pointProperty", Point.class );
 *  
 *  //add attribute setting per attribute state
 *  builder.minOccurs(0).maxOccurs(2).nillable(false).add("doubleProperty",Double.class);
 *  
 *  //build the type
 *  SimpleFeatureType featureType = builder.feature();
 *  </code>
 * </pre>
 * </p>
 * This builder builds type by maintaining state. Two types of state are maintained:
 * <i>Global Type State</i> and <i>Per Attribute State</i>. Methods which set
 * global state are named <code>set&lt;property>()</code>. Methods which set per attribute 
 * state are named <code>&lt;property>()</code>. Furthermore calls to per attribute 
 * </p>
 * <p>
 * Global state is reset after a call to {@link #buildFeatureType()}. Per 
 * attribute state is reset after a call to {@link #add}.
 * <p>
 * This class maintains a set of {@link Class},{@link AttributeType} mappings
 * which are used to build attributes by specifying classes. By default these 
 * mappings are defined by {@link org.geotools.feature.simple.SimpleSchema}. The
 * methods {@link #addBinding(AttributeType)},{@link #addBindings(Schema)}, and 
 * {@link #setBindings(Schema)} can be used to change the defaults.
 * </p>
 * <p>
 * A default geometry for the feature type can be specified explictly via 
 * {@link #setDefaultGeometry(String)}. However if one is not set the first
 * geometric attribute ({@link GeometryType}) added will be resulting default.
 * So if only specifying a single geometry for the type there is no need to 
 * call the method. However if specifying multiple geometries then it is good
 * practice to specify the name of the default geometry type. For instance:
 * <code>
 * 	<pre>
 *  builder.add( "pointProperty", Point.class );
 *  builder.add( "lineProperty", LineString.class );
 *  builder.add( "polygonProperty", "polygonProperty" );
 *  
 *  builder.setDefaultGeometry( "lineProperty" );
 * 	</pre>
 * </code>
 * </p>
 * 
 * @author Justin Deolivera
 * @author Jody Garnett
 */
public class SimpleTypeBuilder {
	/**
	 * factories
	 */
	protected SimpleTypeFactory factory;

	/**
	 * Map of java class bound to properties types.
	 */
	protected Map/* <Class,AttributeType> */bindings;
	
	// Global state for the feature type
	//
	/**
	 * Naming: local name
	 */
	protected String local;

	/**
	 * Naming: uri indicating scope
	 */
	protected String uri;

	/**
	 * Description of type.
	 */
	protected InternationalString description;

	/**
	 * List of attributes.
	 */
	protected List attributes;

	/**
	 * Additional restrictions on the type.
	 */
	protected Set restrictions;

	/** 
	 * Name of the default geometry to use 
	 */
	protected String defaultGeometry;

	/** 
	 * coordinate reference system of the type 
	 */
	protected CoordinateReferenceSystem crs;
	
	/**
	 * MemberType for collection.
	 * <p>
	 * A simple feature collection can only represent one association.
	 */
	protected SimpleFeatureType memberType;
	
	/**
	 * attribute builder
	 */
	protected AttributeTypeBuilder attributeBuilder;

	/**
	 * Constructs the builder.
	 */
	public SimpleTypeBuilder() {
		this( new SimpleTypeFactoryImpl() );
	}
	
	/**
	 * Constructs the builder specifying the factory for creating feature and 
	 * feature collection types.
	 */
	public SimpleTypeBuilder(SimpleTypeFactory factory) {
		this.factory = factory;
		
		attributeBuilder = new AttributeTypeBuilder();
		setBindings( new SimpleSchema() );
	}
	
	// Dependency Injection
	//
	/**
	 * Sets the factory used to create feature and feature collection types.
	 */
	public void setSimpleTypeFactory(SimpleTypeFactory factory) {
		this.factory = factory;
	}
	/**
	 * The factory used to create feature and feature collection types.
	 */
	public SimpleTypeFactory getSimpleTypeFactory() {
		return factory;
	}
	
	// Builder methods
	//
	/**
	 * Initializes the builder with state from a pre-existing feature type.
	 */
	public void init(SimpleFeatureType type) {
		init();
		if (type == null)
			return;

		uri = type.getName().getNamespaceURI();
		local = type.getName().getLocalPart();
		description = type.getDescription();
		restrictions = null;
		restrictions().addAll(type.getRestrictions());

		attributes = newList((List) type.attributes());
		
		if (type instanceof SimpleFeatureCollectionType) {
			SimpleFeatureCollectionType collection = (SimpleFeatureCollectionType) type;
			attributes = Collections.EMPTY_LIST; // will prevent any addition
													// of attributes
			this.memberType = collection.getMemberType();
		}
	}

	/**
	 * Clears the running list of attributes. 
	 */
	protected void init() {
		attributes = null;
	}
	
	/**
	 * Completely resets all builder state.
	 *
	 */
	protected void reset() {
		uri = null;
		local = null;
		description = null;
		restrictions = null;
		attributes = null;
		memberType = null;
		crs = null;
	}
	
	/**
	 * Set the namespace uri of the built type.
	 */
	public void setNamespaceURI(String namespaceURI) {
		this.uri = namespaceURI;
	}
	/**
	 * The namespace uri of the built type.
	 */
	public String getNamespaceURI() {
		return uri;
	}
	/**
	 * Sets the namespace uri of the built type.
	 */
	public SimpleTypeBuilder namespaceURI(String namespaceURI) {
		setNamespaceURI(namespaceURI);
		return this;
	}
	/**
	 * Sets the name of the built type.
	 */
	public void setName(String name) {
		this.local = name;
	}
	/**
	 * The name of the built type.
	 */
	public String getName() {
		return local;
	}
	
	/**
	 * Sets the description of the built type.
	 */
	public void setDescription(InternationalString description) {
		this.description = description;
	}
	/**
	 * The description of the built type.
	 */
	public InternationalString getDescription() {
		return description;
	}
	
	/**
	 * Sets the name of the default geometry attribute of the built type.
	 */
	public void setDefaultGeometry(String defaultGeometryName) {
		this.defaultGeometry = defaultGeometryName;
	}
	/**
	 * The name of the default geometry attribute of the built type.
	 */
	public String getDefaultGeometry() {
		if (defaultGeometry == null) {
			for (Iterator i = attributes().iterator(); i.hasNext();) {
				AttributeDescriptor att = (AttributeDescriptor) i.next();
				if ( att.getType() instanceof GeometryType ) {
					return att.getName().getLocalPart();
				}
			}
			return null;
		}
		return defaultGeometry;
	}
	
	/**
	 * Sets the coordinate reference system of the built type.
	 */
	public void setCRS(CoordinateReferenceSystem crs) {
		this.crs = crs;
	}
	/**
	 * The coordinate reference system of the built type.
	 */
	public CoordinateReferenceSystem getCRS() {
		return crs;
	}
	
	/**
	 * Sets the coordinate reference system of the built type by specifying its
	 * srs.
	 * 
	 */
	public void setSRS(String SRS) {
		try {
			setCRS(CRS.decode(SRS));
		} catch (Exception  e) {
			String msg = "SRS '" + SRS + "' unknown:" + e.getLocalizedMessage(); 
			throw (IllegalArgumentException) new IllegalArgumentException( msg ).initCause( e );
		}
	}
	
	/**
	 * Specifies an attribute type binding.
	 * <p>
	 * This method is used to associate an attribute type with a java class. 
	 * The class is retreived from <code>type.getBinding()</code>. When the
	 * {@link #add(String, Class)} method is used to add an attribute to the 
	 * type being built, this binding is used to locate the attribute type.
	 * </p>
	 * 
	 * @param type The attribute type.
	 */
	public void addBinding(AttributeType type) {
		bindings().put(type.getBinding(), type);
	}
	
	/**
	 * Specifies a number of attribute type bindings.
	 * 
	 * @param schema The schema containing the attribute types.
	 * 
	 * @see {@link #addBinding(AttributeType)}.
	 */
	public void addBindings( Schema schema ) {
		for (Iterator itr = schema.values().iterator(); itr.hasNext();) {
			AttributeType type = (AttributeType) itr.next();
			addBinding(type);
		}
	}
	
	/**
	 * Specifies a number of attribute type bindings clearing out all existing
	 * bindings.
	 * 
	 * @param schema The schema contianing attribute types.
	 * 
	 * @see {@link #addBinding(AttributeType)}.
	 */
	public void setBindings( Schema schema ) {
		bindings().clear();
		addBindings( schema );
	}
	
	/**
	 * Looks up an attribute type which has been bound to a class.
	 * 
	 * @param binding The class.
	 * 
	 * @return AttributeType The bound attribute type.
	 */
	public AttributeType getBinding(Class binding) {
		return (AttributeType) bindings().get(binding);
	}
	
	// per attribute methods
	//
	/**
	 * Sets the minOccurs of the next attribute added to the feature type.
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleTypeBuilder minOccurs( int minOccurs ) {
		attributeBuilder.setMinOccurs(minOccurs);
		return this;
	}
	/**
	 * Sets the maxOccurs of the next attribute added to the feature type.
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleTypeBuilder maxOccurs( int maxOccurs ) {
		attributeBuilder.setMaxOccurs(maxOccurs);
		return this;
	}
	/**
	 * Sets the nullability of the next attribute added to the feature type.
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleTypeBuilder nillable( boolean isNillable ) {
		attributeBuilder.setNillable(isNillable);
		return this;
	}
	/**
	 * Adds a restriction to the next attribute added to the feature type.
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleTypeBuilder restriction( Filter filter ) {
		attributeBuilder.addRestriction( filter );
		return this;
	}
	/**
	 * Sets the description of the next attribute added to the feature type.
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleTypeBuilder description( String description ) {
		attributeBuilder.setDescription( description );
		return this;
	}
	/**
	 * Sets the default value of the next attribute added to the feature type.
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleTypeBuilder defaultValue( Object defaultValue ) {
		attributeBuilder.setDefaultValue( defaultValue );
		return this;
	}
	/**
	 * Sets the crs of the next attribute added to the feature type.
	 * <p>
	 * This only applies if the attribute added is geometric.
	 * </p>
	 * <p>
	 * This value is reset after a call to {@link #add(String, Class)}
	 * </p>
	 */
	public SimpleTypeBuilder crs( CoordinateReferenceSystem crs ) {
		attributeBuilder.setCRS(crs);
		return this;
	}
	
	/**
	 * Adds a new attribute w/ provided name and class.
	 * 
	 * <p>
	 * The provided class is used to locate an attribute type binding previously 
	 * specified by {@link #addBinding(AttributeType)},{@link #addBindings(Schema)}, 
	 * or {@link #setBindings(Schema)}. 
	 * </p>
	 * <p>
	 * If not such binding exists then an attribute type is created on the fly.
	 * </p>
	 * @param name The name of the attribute.
	 * @param bind The class the attribute is bound to.
	 * 
	 */
	public void add(String name, Class binding) {

		//check if this is the name of the default geomtry, in that case we 
		// better make it a geometry type
		if ( defaultGeometry != null && defaultGeometry.equals( name ) ) {
			add( name, binding, null );
			return;
		}
		
		attributeBuilder.setBinding(binding);
		attributeBuilder.setName(name);

		AttributeType type = attributeBuilder.buildType();
		AttributeDescriptor descriptor = attributeBuilder.buildDescriptor(name,type);
		attributes().add(descriptor);
	}
	
	public void add(String name, Class binding, CoordinateReferenceSystem crs ) {
		attributeBuilder.setBinding(binding);
		attributeBuilder.setName(name);
		attributeBuilder.setCRS(crs);
		
		GeometryType type = attributeBuilder.buildGeometryType();
		AttributeDescriptor descriptor = attributeBuilder.buildDescriptor(name,type);
		attributes().add(descriptor);
	}
	
	/**
	 * Builds a feature type from compiled state.
	 * <p>
	 * After the type is built the running list of attributes is cleared.
	 * </p>
	 * @return The built feature type.
	 */
	public SimpleFeatureType buildFeatureType() {
		AttributeDescriptor defaultGeometry = null;
		
		//was a default geometry set?
		if ( this.defaultGeometry != null ) {
			List atts = attributes();
			for ( int i = 0; i < atts.size(); i++) {
				AttributeDescriptor att = (AttributeDescriptor) atts.get(i);
				if ( this.defaultGeometry.equals( att.getName().getLocalPart() ) ) {
					//ensure the type is a geometry type
					if ( !(att.getType() instanceof GeometryType) ) {
						attributeBuilder.init( att );
						GeometryType type = attributeBuilder.buildGeometryType();
						att = attributeBuilder.buildDescriptor(att.getName(),type);
						atts.set( i, att );
					}
					defaultGeometry = att;
					break;
				}
			}
		}
		
		if ( defaultGeometry == null ) {
			//none was set by name, look for first geometric type
			for ( Iterator a = attributes().iterator(); a.hasNext(); ) {
				AttributeDescriptor att = (AttributeDescriptor) a.next();
				if ( att.getType() instanceof GeometryType ) {
					defaultGeometry = att;
					break;
				}
			}
		}
		
		SimpleFeatureType built = factory.createSimpleFeatureType(
			typeName(), attributes(), defaultGeometry, crs, restrictions(), description);
		
		init();
		return built;
	}
	
	// Internal api available for subclasses to override
	//
	/**
	 * Determines if the attribute descriptor represents a geometric attribute.
	 * 
	 * @param descriptor The attribute descriptor.
	 */
	protected boolean isGeometry(Class binding) {
		return false;
	}
	
	/**
	 * Creates a descriptor from the name/binding of an attribute.
	 * 
	 * @param name The name of the attribute.
	 * @param binding The binding of the attribute.
	 * 
	 */
//	protected final AttributeDescriptor createAttributeDescriptor(String name,Class binding,boolean isNillable,Set restrictions) {
//		TypeName typeName = new org.geotools.feature.type.TypeName(name);
//		
//		AttributeType type = getBinding(binding); 
//		if ( type != null ) {
//			//we need to actually prototype the attribute type beacuse for simple
//			// content we need to ensure the the descriptor name == type name
//			type = createPrototype(typeName, type );
//		}
//		else {
//			//create one on the fly
//			type = typeFactory.createAttributeType(
//				typeName,binding,false,false,restrictions,null,null);
//		}
//		
//		return typeFactory.createAttributeDescriptor(type,typeName,1,1,isNillable);
//	}
	
	/**
	 * Prototypes an attribute type, overriding only the name of hte resulting
	 * attribute type.
	 * 
	 * @param typeName The new name of the attribute type.
	 * @param proto The prototype.
	 * 
	 */
//	protected final AttributeType createPrototype( TypeName typeName, AttributeType proto ){
//		if ( proto instanceof GeometryType ) {
//			GeometryType gProto = (GeometryType) proto;
//			
//			//if a crs set, override the crs as well as the name
//			CoordinateReferenceSystem crs = null;
//			if ( this.crs != null ) {
//				crs = this.crs;
//			}
//			else {
//				crs = gProto.getCRS();
//			}
//			
//			return typeFactory.createGeometryType( typeName, proto.getBinding(), 
//				crs, proto.isIdentified(), proto.isAbstract(), proto.getRestrictions(), 
//				proto.getSuper(), proto.getDescription()); 
//		}
//		
//		return typeFactory.createAttributeType( typeName, proto.getBinding(), 
//			proto.isIdentified(), proto.isAbstract(), proto.getRestrictions(), 
//			proto.getSuper(), proto.getDescription()); 
//	}
	
	/**
	 * Creates a new set instance, this default implementation returns {@link HashSet}.
	 */
	protected Set newSet(){
		return new HashSet();
	}
	/**
	 * Creates a new list instance, this default impelementation returns {@link ArrayList}.
	 */
	protected List newList() {
		return new ArrayList();
	}
	
	/**
	 * Creates a new map instance, this default implementation returns {@link HashMap}
	 */
	protected Map newMap() {
		return new HashMap();
	}
	
	/**
	 * Creates a new list which is the same type as the provided list.
	 * <p>
	 * If the new copy can not be created reflectively.. {@link #newList()} is 
	 * returned.
	 * </p>
	 */
	protected List newList(List origional) {
		if (origional == null) {
			return newList();
		}
		if (origional == Collections.EMPTY_LIST) {
			return newList();
		}
		try {
			return (List) origional.getClass().newInstance();
		} catch (InstantiationException e) {
			return newList();
		} catch (IllegalAccessException e) {
			return newList();
		}
	}
	
	// Helper methods, 
	//
	/**
	 * Naming: Accessor which returns type name as follows:
	 * <ol>
	 * <li>If <code>typeName</code> has been set, its value is returned.
	 * <li>If <code>name</code> has been set, it + <code>namespaceURI</code>
	 * are returned.
	 * </ol>
	 * 
	 */
	protected TypeName typeName() {
		if (local == null)
			return null;
		
		return new org.geotools.feature.type.TypeName(uri, local);
	}

	/**
	 * Accessor for attributes.
	 */
	protected List attributes() {
		if (attributes == null) {
			attributes = newList();
		}
		return attributes;
	}
	/**
	 * Accessor for restrictions.
	 */
	protected Set restrictions(){
		if (restrictions == null) {
			restrictions = newSet();
		}
		return restrictions;		
	}
	/**
	 * Accessor for bindings.
	 */
	protected Map bindings() {
		if (bindings == null) {
			bindings = newMap();
		}
		return bindings;
	}
	
	
}
