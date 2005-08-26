package org.opengis.feature.type;

import org.opengis.feature.Attribute;
import org.opengis.util.GenericName;

/**
 * Type information, immutable.
 * <p>
 * This is not called AttributeType to prevent confusion by those familiar with GeoTools
 */
public interface Type {
  /** @return Name containing both the namespace and name for this type */
  GenericName getName();

  /** Java class bound to this content type */
  Class getJavaType();

  /** True if value is allowed to be null.
   * <p>
   * Although this is a validation concern we are representing it here
   * to allow for the simplified view of the world as described by
   * FlatFeature. If this method gets in our way we can move it over to Schema.
   * </p>
   */
  boolean isNilable();

/**
 * @return  Returns the attribute.
 * @uml.property  name="attribute"
 * @uml.associationEnd  inverse="type1:org.opengis.feature.Attribute"
 * @uml.association  name="instance"
 */
public Attribute getAttribute();

/**
 * Setter of the property <tt>attribute</tt>
 * @param attribute  The attribute to set.
 * @uml.property  name="attribute"
 */
public void setAttribute(Attribute attribute);
}