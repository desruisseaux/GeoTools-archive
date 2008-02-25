/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.data;

import net.sf.jsqlparser.statement.select.PlainSelect;

import org.opengis.feature.simple.SimpleFeatureType;

import com.esri.sde.sdk.client.SeQueryInfo;

/**
 * Stores information about known ArcSDE feature types or in-process registered
 * "views".
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/FeatureTypeInfo.java $
 */
final class FeatureTypeInfo {

    private final SimpleFeatureType featureType;

    private final FIDReader fidStrategy;

    private final boolean isWritable;

    // we don't support transaction on multiversioned tables
    private final boolean isMultiVersion;

    /** is it a SDE registered view? */
    private final boolean isView;

    private final PlainSelect definitionQuery;

    private final SeQueryInfo sdeDefinitionQuery;

    public FeatureTypeInfo(final SimpleFeatureType featureType, final FIDReader fidStrategy,
            final boolean isWritable, final boolean isMultiVersion, final boolean isView) {
        this(featureType, fidStrategy, isWritable, isMultiVersion, isView, null, null);
    }

    public FeatureTypeInfo(final SimpleFeatureType featureType, final FIDReader fidStrategy,
            final PlainSelect definitionQuery, final SeQueryInfo sdeDefinitionQuery) {
        this(featureType, fidStrategy, false, false, false, definitionQuery, sdeDefinitionQuery);
    }

    private FeatureTypeInfo(final SimpleFeatureType featureType, final FIDReader fidStrategy,
            final boolean isWritable, final boolean isMultiVersion, final boolean isView,
            final PlainSelect definitionQuery, final SeQueryInfo sdeDefinitionQuery) {
        assert featureType != null;
        assert fidStrategy != null;

        if (definitionQuery != null || sdeDefinitionQuery != null) {
            if (definitionQuery == null || sdeDefinitionQuery == null) {
                throw new NullPointerException(
                        "both SeQueryInfo and PlainSelect are needed for an in-process view");
            }
            if (isWritable) {
                throw new IllegalArgumentException("In-process views can't be writable");
            }
        }
        if (isView) {
            throw new IllegalArgumentException("ArcSDE registered views can't be writable");
        }

        this.featureType = featureType;
        this.fidStrategy = fidStrategy;
        this.isWritable = isWritable;
        this.isMultiVersion = isMultiVersion;
        this.isView = isView;
        this.definitionQuery = definitionQuery;
        this.sdeDefinitionQuery = sdeDefinitionQuery;
    }

    public String getFeatureTypeName() {
        return featureType.getTypeName();
    }

    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    public FIDReader getFidStrategy() {
        return fidStrategy;
    }

    public boolean isWritable() {
        return isWritable;
    }

    public boolean isInProcessView() {
        return definitionQuery != null;
    }

    public PlainSelect getDefinitionQuery() {
        return definitionQuery;
    }

    public SeQueryInfo getSdeDefinitionQuery() {
        return sdeDefinitionQuery;
    }

    public boolean isMultiVersion() {
        return isMultiVersion;
    }

    public boolean isView() {
        return isView;
    }
}
