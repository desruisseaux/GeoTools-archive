/*
 * FeatureType.java
 *
 * Created on April 29, 2002, 3:46 PM
 */
package org.geotools.feature;

import com.vividsolutions.jts.geom.Geometry;

/** 
 * <p>A metadata template for a feature of arbitrary complexity.  Note that
 * this documentation should be read in conjunction with the
 * <code>Feature</code> API.
 *
 * This interface answers the question: How do we represent features within
 * GeoTools?  Of course, the most general answer would be: features can be any
 * Java object. However, this is also the least useful solution because it
 * means that users of features have essentially no way to find out about
 * the meaning of features other than using Java introspection/reflection.
 * This is too cumbersome and is insufficient for the goal of creating a simple
 * framework for manipulating and accessing generic geographic data.  The
 * opposite approach might be to define a very constrained set of possible
 * attributes (that, for example, mirrored Java primitives and OGC simple 
 * geometries) and only allow features of this type.</p>
 *
 * </p>This interface takes a different approach: it defines a minimal ontology
 * for representing a feature and serves as a consistent framework
 * for defining more constrained (and, therefore, often more meaningful) feature
 * types.  A <code>FeatureType</code> represents features as an object that 
 * contains zero or more attribute objects, one of which must be a geometry.  
 * Note that instances of implementations of this class are henceforth refered
 * to as schemas.<p>
 * 
 * <p>With one exception, the type of an attribute is considered to be its 
 * cannonical definition by the FeatureType.  For example, an attribute type 
 * might be a <code>javax.sound.midi.Sequence</code> object, which contains
 * a <code>float</code> public field called PPQ.  The fact that this attribute
 * exists is not known by the <code>FeatureType</code> itself.  If a caller asks
 * this <code>FeatureType</code> for all of its attributes, the <code>
 * FeatureType</code> will tell the caller that it has an attribute of type 
 * <code>javax.sound.midi.Sequence</code>, but not that this attribute has a
 * sub-attribute (field) called PPQ.  It is the responsibility of the callers
 * to understand the objects it is asking for and manipulate them appropriately.
 * This sole exception is if the type stored in the <code>FeatureType</code> is
 * a <code>org.geotools.datasource.Feature</code> type.  In this case, all
 * information about sub-attributes are stored and passed to calling classes
 * upon request.  The style of reference (XPath) is defined in and mediated
 * by <code>FeatureType</code> implementations.</p>
 *
 * <p>It is the responsibility of the implementing class to ensure that the
 * <code>FeatureType</code> is always in a valid state.  This means that each
 * attribute tuple must be fully initialized and valid.  The minimum valid
 * <code>FeatureType</code> is one with nulls for namespace, type, and attributes;
 * this is clearly a trivial case, since it is so constrained that it would
 * not allow for any feature construction.  There are a few conventions to
 * which implementers of this interface must be aware in order to successfully
 * manage a <code>FeatureType</code>:</p><ol>
 *   
 * <li><b>Immutability</b><br>
 * <i>FeatureTypes must be implemented as immutable objects!</i>  All setting 
 * methods return schemas and these methods must be clones of the schema object,
 * rather than the object itself.  This is the reason that the FeatureType 
 * interface extends the <code>Cloneable</code> interface.</li><br>
 *
 * <li><b>Default Geometries</b><br>
 * Note that the schema contains two special methods for handling geometries.
 * The primary geometry retrieval methods are in <code>Feature</code> because 
 * they may change over the life of the feature, while the schema may not.
 * The exception to this is the initial geometry, which seeds the geometry
 * attribute value for the feature, but may be subsequently ignored by the 
 * feature.  Null is a valid initial geometry reference.</li>
 *
 * <li><b>XPath</b><br>
 * XPath is the standard used to access all attributes (flat, nested, and
 * multiple), via a single, unified string.  Using XPath to access attributes
 * has the convenient side-benefit of making them appear to be non-nested and
 * non-multiple to callers with no awareness of XPath.  This greatly simplifies
 * accessing and manipulating data.  However, it does put extra burden on the
 * implementers of <code>FeatureType</code> to understand and correctly 
 * implement XPath pointers.  Note that the <code>Feature</code> object does not
 * understand XPath at all and relies on implementors of this interface to
 * interpret XPath referenes.  Fortunately, XPath is quite simple and has a 
 * clearly written <a href="http://www.w3.org/TR/xpath">specification</a>.</li>
 * </ol>
 * 
 * @author Rob Hranac, VFNY
 * @see org.geotools.datasource.Feature
 * @see org.geotools.datasource.FeatureTypeFlat
 */
public interface FeatureType extends AttributeType {

    /* ************************************************************************
     * Handles all global schema modifications and access.                    *
     * ************************************************************************/
    /**
     * Sets the global schema namespace.  Note that namespaces are not required
     * and should return null if it is not set.
     *
     * @param namespace URI namespace associate with this schema.
     * @return A modified copy of this schema.
     */
    public FeatureType setNamespace(String namespace);

    /**
     * Gets the global schema namespace.
     *
     * @return Namespace of schema.
     */
    public String getNamespace();

    /**
     * Sets the global schema type name.  Note that type names are not required
     * and should return null if it is not set.
     *
     * @param name Type name associated with this schema.
     * @return A modified copy of this schema.
     */
    public FeatureType setTypeName(String name);

    /**
     * Gets the type name for this schema.
     *
     * @return Namespace of schema.
     */
    public String getTypeName();


    /* ************************************************************************
     * Handles all schema attribute type modifications.                       *
     * ************************************************************************/
    /**
     * Sets the values for any attribute other than a nested Feature attribute.
     * This method overwrites any existing attribute definitions.
     *
     * @param attribute The attribute type to set.
     * @return A modified copy of this schema.
     * @throws SchemaException When the type is not cloneable, occurances 
     * are illegal
     */
    public FeatureType setAttributeType(AttributeType attribute)
        throws SchemaException;

    /**
     * Removes the attribute, if it exists.
     *
     * @param xPath XPath pointer to attribute type.
     * @return A modified copy of this schema.
     * @throws SchemaException When the attribute does not exist.
     */
    public FeatureType removeAttributeType(String xPath)
        throws SchemaException;

    /**
     * Sets the default feature geometry.
     *
     * @param xPath XPath pointer to attribute type.
     * @return A modified copy of this schema.
     * @throws SchemaException If the attribute does not exist or is not a 
     * geometry.
     */
    public FeatureType setDefaultGeometry(String xPath)
        throws SchemaException;


    /* ************************************************************************
     * Handles all attribute information retreival for non-feature clients.   *
     * ************************************************************************/
    /**
     * Gets all of the names for the first 'level' of attributes.  This means
     * that nested attributes must be read seperately, via the getNames()
     * method of thier schemas or the getAllNames() method.
     *
     * @return Non-nested attribute names.
     */
    public AttributeType[] getAttributeTypes();

    /**
     * Gets all of the names for all 'levels' of attributes.  This is a
     * convenience method for clients who want to think of the feature
     * as 'flat,' regardless of its actual occurences or nested attributes.
     *
     * @return Nested attribute names.
     */
    public AttributeType[] getAllAttributeTypes();

    /**
     * Gets the number of occurances of this attribute.
     *
     * @param xPath XPath pointer to attribute type.
     * @return Number of occurences.
     */
    public boolean hasAttributeType(String xPath);

    /**
     * Checks for attribute existence.
     *
     * @param xPath XPath pointer to attribute type.
     * @param Name the name of the attribute.
     * @return True if attribute exists.
     */
    public AttributeType getAttributeType(String xPath)
        throws SchemaException;

    /**
     * Gets the default feature geometry.
     *
     * @return Path to initial geometry as XPath.
     */
    public AttributeType getDefaultGeometry();


    /* ************************************************************************
     * Handles all attribute information retreival for feature clients.       *
     * ************************************************************************/
    /**
     * Returns the number of attributes at the first 'level' of the schema.
     *
     * @param Name the name of the attribute.
     * @return True if attribute exists.
     */
    public int attributeTotal();

    /**
     * Gets the number of occurances of this attribute.
     *
     * @param Name the name of the attribute.
     * @return Number of occurences.
     */
    public AttributeType getAttributeType(int position);

}

