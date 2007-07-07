package org.geotools.referencing.factory;

import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;

/**
 * An extension to AuthorityFactory interface allowing searching.
 * 
 * @author Jody Garnett (Refractions Research)
 * @since 2.4
 */
public interface AuthorityFactory2 extends AuthorityFactory {
    /**
     * Returns a finder which can be used for looking up unidentified objects. The finder
     * fetches a fully {@linkplain IdentifiedObject identified object} from an incomplete one,
     * for example from an object without identifier or "{@code AUTHORITY[...]}" element in
     * <cite>Well Known Text</cite> terminology.
     *
     * @param  type The type of objects to look for. Should be a GeoAPI interface like
     *         {@code GeographicCRS.class}, but this method accepts also implementation
     *         class. If the type is unknown, use {@code IdentifiedObject.class}. A more
     *         accurate type may help to speed up the search, since it reduces the amount
     *         of tables to scan in some implementations like the factories backed by
     *         EPSG database.
     * @return A finder to use for looking up unidentified objects.
     * @throws FactoryException if the finder can not be created.
     *
     * @since 2.4
     */    
    public IdentifiedObjectFinder getIdentifiedObjectFinder(
            final Class/*<? extends IdentifiedObject>*/ type) throws FactoryException;
}
