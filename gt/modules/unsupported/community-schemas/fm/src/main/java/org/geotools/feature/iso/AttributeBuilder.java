package org.geotools.feature.iso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.feature.iso.type.AttributeDescriptorImpl;
import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Builder for attributes.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * 
 */
public class AttributeBuilder {
    private static final Logger LOGGER = Logger.getLogger(AttributeBuilder.class.getPackage()
            .getName());

    /**
     * Factory used to create attributes
     */
    FeatureFactory attributeFactory;

    /**
     * Namespace context.
     */
    String namespace;

    /**
     * Type of complex attribute being built.
     */
    AttributeType type;

    /**
     * Contained properties (associations + attributes)
     */
    List properties;

    /**
     * The crs of the attribute.
     */
    CoordinateReferenceSystem crs;

    /**
     * Default geometry of the feature.
     */
    Object defaultGeometry;

    public AttributeBuilder(FeatureFactory attributeFactory) {
        this.attributeFactory = attributeFactory;
    }

    //
    // Injection
    //
    // Used to inject dependencies we need during construction time.
    //
    /**
     * Returns the underlying attribute factory.
     */
    public FeatureFactory getFeatureFactory() {
        return attributeFactory;
    }

    /**
     * Sets the underlying attribute factory.
     */
    public void setFeatureFactory(FeatureFactory attributeFactory) {
        this.attributeFactory = attributeFactory;
    }

    //
    // State
    //

    /**
     * Initializes the builder to its initial state, the same state it is in
     * directly after being instantiated.
     */
    public void init() {
        type = null;
        properties = null;
        crs = null;
        defaultGeometry = null;
    }

    /**
     * Initializes the state of the builder based on a previously built
     * attribute.
     * <p>
     * This method is useful when copying another attribute.
     * </p>
     */
    public void init(Attribute attribute) {
        init();

        type = attribute.getType();

        if (attribute instanceof ComplexAttribute) {
            ComplexAttribute complex = (ComplexAttribute) attribute;
            Collection properties = (Collection) complex.get();
            for (Iterator itr = properties.iterator(); itr.hasNext();) {
                Property property = (Property) itr.next();
                if (property instanceof Attribute) {
                    Attribute att = (Attribute) property;
                    add(att.getID(), att.get(), att.name());
                } else if (property instanceof Association) {
                    Association assoc = (Association) property;
                    associate(assoc.getRelated(), assoc.name());
                }
            }
        }

        if (attribute instanceof Feature) {
            Feature feature = (Feature) attribute;
            crs = feature.getCRS();

            if (feature.getDefaultGeometry() != null) {
                defaultGeometry = feature.getDefaultGeometry().get();
            }
        }

    }

    /**
     * This namespace will be used when constructing attribute names.
     */
    public void setNamespaceURI(String namespace) {
        this.namespace = namespace;
    }

    /**
     * This namespace will be used when constructing attribute names.
     * 
     * @return namespace will be used when constructing attribute names.
     */
    public String getNamespaceURI() {
        return namespace;
    }

    /**
     * Sets the type of the attribute being built.
     * <p>
     * When building a complex attribute, this type is used a reference to
     * obtain the types of contained attributes.
     * </p>
     */
    public void setType(AttributeType type) {
        this.type = type;
    }

    /**
     * @return The type of the attribute being built.
     */
    public AttributeType getType() {
        return type;
    }

    // Feature specific methods
    /**
     * Sets the coordinate reference system of the built feature.
     */
    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * @return The coordinate reference system of the feature, or null if not
     *         set.
     */
    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    /**
     * Sets the default geometry of the feature.
     */
    public void setDefaultGeometry(Object defaultGeometry) {
        this.defaultGeometry = defaultGeometry;
    }

    /**
     * @return The default geometry of the feature.
     */
    public Object getDefaultGeometry() {
        return defaultGeometry;
    }

    //
    // Complex attribute specific methods
    //
    /**
     * Adds an attribute to the complex attribute being built. <br>
     * <p>
     * This method uses the result of {@link #getNamespaceURI()} to build a
     * qualified attribute name.
     * </p>
     * <p>
     * This method uses the type supplied in {@link #setType(AttributeType)} in
     * order to determine the attribute type.
     * </p>
     * 
     * @param name
     *            The name of the attribute.
     * @param value
     *            The value of the attribute.
     * 
     */
    public Attribute add(Object value, String name) {
        return add(null, value, name);
    }

    /**
     * Adds an association to the complex attribute being built. <br>
     * <p>
     * This method uses the result of {@link #getNamespaceURI()} to build a
     * qualified attribute name.
     * </p>
     * <p>
     * This method uses the type supplied in {@link #setType(AttributeType)} in
     * order to determine the association type.
     * </p>
     * 
     * @param value
     *            The value of the association, an attribute.
     * @param name
     *            The name of the association.
     */
    public void associate(Attribute value, String name) {
        associate(value, name, namespace);
    }

    /**
     * Adds an attribute to the complex attribute being built. <br>
     * <p>
     * This method uses the type supplied in {@link #setType(AttributeType)} in
     * order to determine the attribute type.
     * </p>
     * 
     * @param value
     *            The value of the attribute.
     * @param name
     *            The name of the attribute.
     * @param namespaceURI
     *            The namespace of the attribute.
     */
    public Attribute add(Object value, String name, String namespaceURI) {
        return add(null, value, name, namespaceURI);
    }

    /**
     * Adds an association to the complex attribute being built. <br>
     * <p>
     * This method uses the type supplied in {@link #setType(AttributeType)} in
     * order to determine the association type.
     * </p>
     * 
     * @param value
     *            The value of the association, an attribute.
     * @param name
     *            The name of the association.
     * @param namespaceURI
     *            The namespace of the association
     */
    public void associate(Attribute attribute, String name, String namespaceURI) {
        associate(attribute, Types.attributeName(namespaceURI, name));
    }

    /**
     * Adds an attribute to the complex attribute being built overriding the
     * type of the declared attribute descriptor by a subtype of it. <br>
     * <p>
     * This method uses the type supplied in {@link #setType(AttributeType)} in
     * order to determine the attribute type.
     * </p>
     * 
     * @param id
     *            the attribtue id
     * @param value
     *            The value of the attribute.
     * 
     * @param name
     *            The name of the attribute.
     * @param type
     *            the actual type of the attribute, which might be the same as
     *            the declared type for the given AttributeDescriptor or a
     *            derived type.
     * 
     */
    public Attribute add(final String id, final Object value, final Name name,
            final AttributeType type) {
        // existence check
        AttributeDescriptor descriptor = attributeDescriptor(name, type);
        AttributeType declaredType = descriptor.getType();
        if (!declaredType.equals(type)) {
            boolean argIsSubType = Types.isSuperType(type, declaredType);
            if (!argIsSubType) {
                /*
                 * commented out since we got community schemas where the
                 * required instance type is not a subtype of the declared one
                 * throw new IllegalArgumentException(type.getName() + " is not
                 * a subtype of " + declaredType.getName());
                 */
                LOGGER.warning("Adding attribute " + name + " of type " + type.getName()
                        + " which is not a subtype of " + declaredType.getName());
            }
            int minOccurs = descriptor.getMinOccurs();
            int maxOccurs = descriptor.getMaxOccurs();
            boolean nillable = descriptor.isNillable();
            descriptor = new AttributeDescriptorImpl(type, name, minOccurs, maxOccurs, nillable);
        }
        Attribute attribute = create(value, null, descriptor, id);
        properties().add(attribute);
        return attribute;
    }

    /**
     * Adds an attribute to the complex attribute being built. <br>
     * <p>
     * This method uses the type supplied in {@link #setType(AttributeType)} in
     * order to determine the attribute type.
     * </p>
     * 
     * @param name
     *            The name of the attribute.
     * @param value
     *            The value of the attribute.
     * 
     */
    public Attribute add(Object value, Name name) {
        return add(null, value, name);
    }

    /**
     * Adds an association to the complex attribute being built. <br>
     * <p>
     * This method uses the type supplied in {@link #setType(AttributeType)} in
     * order to determine the association type.
     * </p>
     * 
     * @param value
     *            The value of the association, an attribute.
     * @param name
     *            The name of the association.
     * @param namespaceURI
     *            The namespace of the association
     */
    public void associate(Attribute value, Name name) {
        AssociationDescriptor descriptor = associationDescriptor(name);
        Association association = attributeFactory.createAssociation(value, descriptor);

        properties().add(association);
    }

    /**
     * Adds an attribute to the complex attribute being built. <br>
     * <p>
     * The result of {@link #getNamespaceURI()} to build a qualified attribute
     * name.
     * </p>
     * <p>
     * This method uses the type supplied in {@link #setType(AttributeType)} in
     * order to determine the attribute type.
     * </p>
     * 
     * @param id
     *            The id of the attribute.
     * @param name
     *            The name of the attribute.
     * @param value
     *            The value of the attribute.
     */
    public Attribute add(String id, Object value, String name) {
        return add(id, value, name, namespace);
    }

    /**
     * Adds an attribute to the complex attribute being built. <br>
     * <p>
     * This method uses the type supplied in {@link #setType(AttributeType)} in
     * order to determine the attribute type.
     * </p>
     * 
     * @param id
     *            The id of the attribute.
     * @param value
     *            The value of the attribute.
     * @param name
     *            The name of the attribute.
     * @param namespaceURI
     *            The namespace of the attribute.
     */
    public Attribute add(String id, Object value, String name, String namespaceURI) {
        return add(id, value, Types.attributeName(namespaceURI, name));
    }

    /**
     * Adds an attribute to the complex attribute being built. <br>
     * <p>
     * This method uses the type supplied in {@link #setType(AttributeType)} in
     * order to determine the attribute type.
     * </p>
     * 
     * @param id
     *            The id of the attribute.
     * @param name
     *            The name of the attribute.
     * @param value
     *            The value of the attribute.
     * 
     */
    public Attribute add(String id, Object value, Name name) {
        AttributeDescriptor descriptor = attributeDescriptor(name);
        Attribute attribute = create(value, null, descriptor, id);
        properties().add(attribute);
        return attribute;
    }

    /**
     * Convenience accessor for properties list which does the null check.
     */
    protected List properties() {
        if (properties == null) {
            properties = new ArrayList();
        }

        return properties;
    }

    protected AssociationDescriptor associationDescriptor(Name name) {
        PropertyDescriptor descriptor = Types.descriptor((ComplexType) type, name);

        if (descriptor == null) {
            String msg = "Could not locate association: " + name + " in type: " + type.getName();
            throw new IllegalArgumentException(msg);
        }

        if (!(descriptor instanceof AssociationDescriptor)) {
            String msg = name + " references a non association";
            throw new IllegalArgumentException(msg);
        }

        return (AssociationDescriptor) descriptor;
    }

    protected AttributeDescriptor attributeDescriptor(Name name) {
        PropertyDescriptor descriptor = Types.descriptor((ComplexType) type, name);

        if (descriptor == null) {
            String msg = "Could not locate attribute: " + name + " in type: " + type.getName();
            throw new IllegalArgumentException(msg);
        }

        if (!(descriptor instanceof AttributeDescriptor)) {
            String msg = name + " references a non attribute";
            throw new IllegalArgumentException(msg);
        }

        return (AttributeDescriptor) descriptor;
    }

    protected AttributeDescriptor attributeDescriptor(Name name, AttributeType actualType) {
        PropertyDescriptor descriptor = Types.descriptor((ComplexType) type, name, actualType);

        if (descriptor == null) {
            String msg = "Could not locate attribute: " + name + " in type: " + type.getName();
            throw new IllegalArgumentException(msg);
        }

        if (!(descriptor instanceof AttributeDescriptor)) {
            String msg = name + " references a non attribute";
            throw new IllegalArgumentException(msg);
        }

        return (AttributeDescriptor) descriptor;
    }

    /**
     * Factors out attribute creation code, needs to be called with either one
     * of type or descriptor null.
     */
    protected Attribute create(Object value, AttributeType type, AttributeDescriptor descriptor,
            String id) {
        if (descriptor != null) {
            type = descriptor.getType();
        }
        Attribute attribute = null;
        if (type instanceof FeatureCollectionType) {
            attribute = descriptor != null ? attributeFactory.createFeatureCollection(
                    (Collection) value, descriptor, id) : attributeFactory.createFeatureCollection(
                    (Collection) value, (FeatureCollectionType) type, id);
        } else if (type instanceof FeatureType) {
            attribute = descriptor != null ? attributeFactory.createFeature((Collection) value,
                    descriptor, id) : attributeFactory.createFeature((Collection) value,
                    (FeatureType) type, id);
        } else if (type instanceof ComplexType) {
            attribute = descriptor != null ? attributeFactory.createComplexAttribute(
                    (Collection) value, descriptor, id) : attributeFactory.createComplexAttribute(
                    (Collection) value, (ComplexType) type, id);
        } else if (type instanceof GeometryType) {
            attribute = attributeFactory.createGeometryAttribute(value, descriptor, id, null);
        } else {
            attribute = attributeFactory.createAttribute(value, descriptor, id);
        }
        return attribute;
    }

    /**
     * Builds the attribute.
     * <p>
     * The class of the attribute built is determined from its type set with
     * {@link #setType(AttributeType)}.
     * </p>
     * 
     * @return The build attribute.
     */
    public Attribute build() {
        return build(null);
    }

    /**
     * Builds the attribute.
     * <p>
     * The class of the attribute built is determined from its type set with
     * {@link #setType(AttributeType)}.
     * </p>
     * 
     * @param id
     *            The id of the attribute, or null.
     * 
     * @return The build attribute.
     */
    public Attribute build(String id) {
        Attribute built = create(properties(), type, null, id);

        // if geometry, set the crs
        if (built instanceof GeometryAttribute) {
            ((GeometryAttribute) built).setCRS(getCRS());
        }

        // if feature, set crs and default geometry
        if (built instanceof Feature) {
            Feature feature = (Feature) built;
            feature.setCRS(getCRS());
            if (defaultGeometry != null) {
                for (Iterator itr = feature.attributes().iterator(); itr.hasNext();) {
                    Attribute att = (Attribute) itr.next();
                    if (att instanceof GeometryAttribute) {
                        if (defaultGeometry.equals(att.get())) {
                            feature.setDefaultGeometry((GeometryAttribute) att);
                        }
                    }

                }
            }
        }

        properties().clear();
        return built;
    }
}
