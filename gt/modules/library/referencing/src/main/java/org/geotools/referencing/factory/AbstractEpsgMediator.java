/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2007, GeoTools Project Managment Committee (PMC)
 * 
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.factory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public abstract class AbstractEpsgMediator extends AbstractAuthorityMediator {

    private static final Logger LOGGER = Logger.getLogger("org.geotools.referencing.factory");
    
    public AbstractEpsgMediator(Hints hints, DataSource datasource) {
        super(hints);
        this.datasource = datasource;
    }
    
    DataSource datasource;

    protected Connection getConnection() throws SQLException {
        try {
            return datasource.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Connection failed", e);
            throw e;
        }
    }
    
    public Citation getAuthority() {
        return Citations.EPSG;
    }
    
    public void dispose(){
        datasource = null;
    }

    public boolean isConnected(){
        return datasource != null;        
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
        return new SimpleInternationalString(identifier.getCode());
    }
}
