package org.opengis.feature.type;

import java.util.Set;

import org.opengis.util.GenericName;

/**
 * Type information, immutable.
 * <p>
 * This is not called AttributeType to prevent confusion by those familiar with
 * GeoTools
 */
public interface Type {
	/**
	 * Indicates the actual name of this Type.
	 * <p>
	 * GenericName is used, and becomes important when working with supertypes and complex content.
	 * </p>
	 * <p>
	 * Follows Java beans naming conventions indicating this is part of our data model.
	 * </p>
	 * @return Name containing both the namespace and name for this type
	 */
	public GenericName getName();
	
	/**
	 * True if this type is usable as a target of a reference.
	 * 
	 * @return true if this complex type must have non null getID()
	 */
	boolean isIdentified();
	
	/**
	 * Access to the name of this type.
	 * <p>
	 * </p>
	 * @return getName().toString();
	 */
	public String name();

	/**
	 * Access to super type information.
	 * <p>
	 * The super type may contain additional restrictions to be considered, or a
	 * definition of isNilable.
	 * </p>
	 * 
	 * @return Type of supertype
	 */
	public Type getSuper();

	/** Indicate that this Type may not be used directly */
	public boolean isAbstract();

	/** Java class bound to this content type */
	public Class getBinding();

	/**
	 * List of restrictions used to limit the allowable values for objects of
	 * this type.
	 * <p>
	 * These restrictions should be considered in light of those available through getSuper,
	 * in the case where Restrictions conflict these should be considered complete overrides
	 * of the restrictions available via the getSuper.
	 * </p>
	 * @return List<Restriction> used to validate allowable values.
	 */
	public Set<Restriction> getRestrictions();

	/**
	 * Boolean.True if value is allowed to be null.
	 * <p>
	 * Although this is a validation concern we are representing it here to
	 * allow for the simplified view of the world as described by FlatFeature.
	 * If this method gets in our way we can move it over to Schema.
	 * </p>
	 * 
	 * @return true if value may be null, false if value my be present, or null
	 *         indicating super should be used.
	 */
	public Boolean isNilable();

}