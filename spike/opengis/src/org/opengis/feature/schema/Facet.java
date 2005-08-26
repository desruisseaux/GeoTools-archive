package org.opengis.feature.schema;

/**
 * Use of Filter was rejected as it is not available to work against
 * regular complex or simple content.
 */
public interface Facet {
  /** @return true if value is valid */
  boolean validate( Object value );
}
