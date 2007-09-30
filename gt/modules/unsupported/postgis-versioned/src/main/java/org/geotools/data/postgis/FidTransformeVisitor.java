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
package org.geotools.data.postgis;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.postgis.fidmapper.VersionedFIDMapper;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.visitor.DuplicatorFilterVisitor;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Takes a filter that eventually contains a fid filter and builds a new filter that does not have
 * it, and relies on attributes instead. This assumes pk attributes are part of the feature type.
 * <br>
 * This cloning is necessary because public FID do not contain the revision attribute, that will be
 * handled by including new filters.
 * 
 * @author aaime
 * @since 2.4
 * 
 */
class FidTransformeVisitor extends DuplicatorFilterVisitor {
    /** The logger for the postgis module. */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.data.postgis");

    private VersionedFIDMapper mapper;

    private Object featureType;

    public FidTransformeVisitor(FilterFactory factory, SimpleFeatureType featureType,
            VersionedFIDMapper mapper) {
        super(factory);
        this.mapper = mapper;
        this.featureType = featureType;
    }

    public void visit(FidFilter filter) {
        Set ids = filter.getIDs();
        if (ids.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid fid filter provides, has no fids inside");
        }
        Filter external = null;
        for (Iterator it = ids.iterator(); it.hasNext();) {
            String id = (String) it.next();
            Object[] attributes;
            try {
                attributes = mapper.getUnversionedPKAttributes(id);
            } catch(IOException e) {
                // assume the fid provided is not in the format the mapper can handle, so
                // it's not really a real datastore fid but has been provided by the user.
                // No harm dome, we just need to skip it
                if(LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Skipping fid " + id + " since it's not in the " +
                                    "proper format for this datastore" + e.getMessage());
                continue;
            }
            Filter idf = null;
            for (int i = 0, j = 0; i < attributes.length; j++) {
                String colName = mapper.getColumnName(j);
                if ("revision".equals(colName))
                    continue;
                CompareFilter equal = ff.createCompareFilter(Filter.COMPARE_EQUALS);
                equal.addLeftValue(ff.createAttributeExpression(colName));
                equal.addRightValue(ff.createLiteralExpression(attributes[i]));
                if (idf == null)
                    idf = equal;
                else
                    idf = idf.and(equal);
                i++;
            }
            if (external == null)
                external = idf;
            else
                external = external.or(idf);
        }
        // if all the fids are in an improper format, the fid filter is equivalent to
        // a Filter that excludes everything... Since I cannot use Filter.EXCLUDE (it breaks
        // the filter splitter with a class cast exception) I'm falling back on the old  
        // "1 = 0" filter (ugly, but works...)
        if(external == null) {
            CompareFilter equal = ff.createCompareFilter(Filter.COMPARE_EQUALS);
            equal.addLeftValue(ff.createLiteralExpression(0));
            equal.addRightValue(ff.createLiteralExpression(1));
            pages.push(equal);
        } else
            pages.push(external);
    }

}
