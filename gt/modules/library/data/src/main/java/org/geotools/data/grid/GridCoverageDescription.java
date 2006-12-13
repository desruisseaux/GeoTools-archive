/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.grid;

import java.util.Map;

import org.opengis.coverage.grid.Format;
import org.opengis.feature.type.TypeName;

/**
 * Used to capture "header" information describing a grid coverage.
 * <p>
 * You will recognize this information as the kind of thing that was available
 * via a reader previously.
 * </p>
 * @since 2.4
 * @deprecated This is a Proposal, we need your feedback!
 * @author Jody Garnett, Refractions Research Inc.
 */
public interface GridCoverageDescription {
    TypeName getName();
    
    Format format();
    
    /** Map<MetaDataKey,MetaDataValue> describing associated GridCoverage */    
    Map/*<Key,Value*/ metadata();
    
    // add more stuff here, basically preparse your header
}
