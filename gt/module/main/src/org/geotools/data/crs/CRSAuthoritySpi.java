package org.geotools.data.crs;

import org.geotools.factory.Factory;

/**
 * This is a marker used to allow CRSService to dynamically locate
 * implementations of CoordinateSystemAuthorityFactory.
 * <p>
 * When the time comes CRSService can switch over to
 * org.geotools.referencing.Factory - that time is not now.
 * </p>
 * @author Jody Garnett
 */
public interface CRSAuthoritySpi extends Factory {

}
