/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */

package org.geotools.processparameter;

import java.util.Map;

import org.opengis.util.InternationalString;

/**
 * A Parameter defines information about a valid process parameter.
 *
 * @author Graham Davis
 */
public class ProcessParameter {
	
    public final String key;
    public final InternationalString description;
    public final Class type ;
    /** Can the value be missing? Or is null allowed... */
    public final boolean required;
    public final Object sample;
    /** Refinement of type; such as the FeatureType of a FeatureCollection, or component type of a List */
    public final Map<String, Object> metadata;

    /** Mandatory information */
    public ProcessParameter(String key, Class type, InternationalString description ){
        this( key, type, description, false, null, null );
    }
    /** Addition of optional parameters */
    public ProcessParameter(String key, Class type, InternationalString description,
                     boolean required, Object sample, Map metadata){
        this.key = key;
        this.type = type;
        this.description = description;
        this.required = required;
        this.sample = sample;
        this.metadata = metadata;
    }
}
