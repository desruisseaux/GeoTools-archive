/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.quality;

// OpenGIS direct dependencies
import org.opengis.metadata.quality.Result;


/**
 * Closeness of reported coordinate values to values accepted as or being true.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 * @deprecated Renamed as {@code AbsoluteExternalPositionalAccuracyImpl} in {@code org.geotools.metadata.iso} subpackage.
 */
public class AbsoluteExternalPositionalAccuracy extends PositionalAccuracy
       implements org.opengis.metadata.quality.AbsoluteExternalPositionalAccuracy
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4116627805950579738L;
    
    /**
     * Constructs an initially empty absolute external positional accuracy.
     */
    public AbsoluteExternalPositionalAccuracy() {
    }    

    /**
     * Creates an positional accuracy initialized to the given result.
     */
    public AbsoluteExternalPositionalAccuracy(final Result result) {
        super(result);
    }
}
