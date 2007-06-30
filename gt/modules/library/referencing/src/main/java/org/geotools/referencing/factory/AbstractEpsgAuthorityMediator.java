package org.geotools.referencing.factory;

import javax.sql.DataSource;

import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.util.SimpleInternationalString;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;

/**
 * Abstract implementation for EPSG (has a DataSource reference inside).
 * 
 * @author Cory Horner (Refractions Research)
 */
public abstract class AbstractEpsgAuthorityMediator extends AbstractAuthorityMediator {

    protected AbstractEpsgAuthorityMediator(int priority, Hints hints) {
        super(priority, hints);
    }

    DataSource datasource;
    
    public Citation getAuthority() {
        return Citations.EPSG;
    }

    /**
     * Gets a description of the object corresponding to a code.
     *
     * @param  code Value allocated by authority.
     * @return A description of the object, or {@code null} if the object
     *         corresponding to the specified {@code code} has no description.
     * @throws NoSuchAuthorityCodeException if the specified {@code code} was not found.
     * @throws FactoryException if the query failed for some other reason.
     */
    public InternationalString getDescriptionText(final String code) throws FactoryException {
        IdentifiedObject identifiedObject = createObject(code);
                final Identifier identifier = identifiedObject.getName();
        if (identifier instanceof GenericName) {
            return ((GenericName) identifier).toInternationalString();
        }
        return new SimpleInternationalString( identifier.getCode() );
    }
}
