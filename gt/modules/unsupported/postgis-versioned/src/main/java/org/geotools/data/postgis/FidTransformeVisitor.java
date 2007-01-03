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

import org.geotools.data.postgis.fidmapper.VersionedFIDMapper;
import org.geotools.feature.FeatureType;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.visitor.DuplicatorFilterVisitor;

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

    private VersionedFIDMapper mapper;

    private Object featureType;

    public FidTransformeVisitor(FilterFactory factory, FeatureType featureType,
            VersionedFIDMapper mapper) {
        super(factory);
        this.mapper = mapper;
        this.featureType = featureType;
    }

    public void visit(FidFilter filter) {
        try {
            Set ids = filter.getIDs();
            if (ids.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid fid filter provides, has no fids inside");
            }
            Filter external = null;
            for (Iterator it = ids.iterator(); it.hasNext();) {
                String id = (String) it.next();
                Object[] attributes = mapper.getUnversionedPKAttributes(id);
                Filter idf = null;
                for (int i = 0; i < attributes.length; i++) {
                    if ("revision".equals(mapper.getColumnName(i)))
                        continue;
                    CompareFilter equal = ff.createCompareFilter(Filter.COMPARE_EQUALS);
                    equal.addLeftValue(ff.createAttributeExpression(mapper.getColumnName(i)));
                    equal.addRightValue(ff.createLiteralExpression(attributes[i]));
                    if (idf == null)
                        idf = equal;
                    else
                        idf = idf.and(equal);
                }
                if (external == null)
                    external = idf;
                else
                    external = external.or(idf);
            }
            pages.push(external);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while de-constructin the fid "
                    + "into primary key fields in order to build a new filter for "
                    + "versioned data store", e);
        }
    }

}
