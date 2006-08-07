/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.metadata.iso.quality;

// OpenGIS dependencies
import org.opengis.metadata.quality.ThematicClassificationCorrectness;

 
/**
 * Comparison of the classes assigned to features or their attributes to a universe of discourse.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 *
 * @since 2.1
 */
public class ThematicClassificationCorrectnessImpl extends ThematicAccuracyImpl
        implements ThematicClassificationCorrectness
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5484398738783800915L;

    /**
     * Constructs an initially empty thematic classification correctness.
     */
    public ThematicClassificationCorrectnessImpl() {
    }
}
