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
package org.geotools.feature;


/**
 * A SimpleFeatureCollectionType indicating that the contents are stored using the
 * deprecated GeoTools FeatureType. This FeatureCollectionType indicates that *no*
 * attribtues are stored at the FeatureCollection level.
 * <p>
 * The only useful information is available via:
 * <ul>
 * <li>FeatureCollectioType.getMemberType()
 * @author Jody Garnett (Refractions Research)
 */
public interface FeatureCollectionType extends FeatureType {
    /**
     * Explicitly documents the FeatureCollection as containing this FeatureType.
     */
    public FeatureType getMemberType();
}
