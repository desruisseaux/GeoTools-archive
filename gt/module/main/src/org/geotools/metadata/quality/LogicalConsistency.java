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


/**
 * Degree of adherence to logical rules of data structure, attribution and relationships (data
 * structure can be conceptual, logical or physical).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class LogicalConsistency extends Element
        implements org.opengis.metadata.quality.LogicalConsistency
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2470752876057569947L;
    
    /**
     * Construct an initially empty logical consistency.
     */
    public LogicalConsistency() {
    }
}
