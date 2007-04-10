/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.h2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.filter.Filter;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureType;


public class H2ContentState extends ContentState {
    protected H2ContentState(ContentEntry entry) {
        super(entry);
    }

    public H2DataStore getDataStore() {
        return (H2DataStore) entry.getDataStore();
    }

    /**
     * Builds the primary key for the entry.
     */
    public PrimaryKey primaryKey() throws Exception {
        return H2Utils.primaryKey(this);
    }

    protected FeatureType buildFeatureType(SimpleTypeFactory factory)
        throws IOException {
        H2TypeBuilder builder = new H2TypeBuilder(factory);
        builder.setNamespaceURI(getDataStore().getNamespaceURI());

        try {
            return H2Utils.buildFeatureType(this, builder);
        } catch (Exception e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
}
