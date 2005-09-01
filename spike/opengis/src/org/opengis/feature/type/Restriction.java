package org.opengis.feature.type;

/**
 * Captures individual restrictions.
 * <p>
 * Use of Filter was rejected as it is not available to work against
 * regular complex or simple content (not just Features).
 * <p>
 * It may/should be argued that Filter should work against non Feature
 * content (such as Metadata).
 * </p>
 * <p>
 * This class needs help, without an expression language we cannot hope
 * to capture these restrictions in a manner suitable for reuse. This idea
 * only worked in GeoTools because the mapping of a length restriction on
 * string has a well defined mapping to Filter.
 * </p>
 */
public interface Restriction {
	
  /** @return true if value is valid */
  public boolean validate( Object value );
  
}
