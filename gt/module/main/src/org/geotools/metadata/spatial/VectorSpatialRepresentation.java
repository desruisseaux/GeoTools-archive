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
package org.geotools.metadata.spatial;

// J2SE direct dependencies
import java.util.Set;
import java.util.Collections;

// OpenGIS direct dependencies
import org.opengis.metadata.spatial.TopologyLevel;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;


/**
 * Information about the vector spatial objects in the dataset.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class VectorSpatialRepresentation extends SpatialRepresentation
        implements org.opengis.metadata.spatial.VectorSpatialRepresentation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5643234643524810592L;
    
    /**
     * Code which identifies the degree of complexity of the spatial relationships.
    */
    private TopologyLevel topologyLevel;

    /**
     * Information about the geometric objects used in the dataset.
     */
    private Set geometricObjects;

    /**
     * Construct an initially empty vector spatial representation.
     */
    public VectorSpatialRepresentation() {
    }
    
    /**
     * Code which identifies the degree of complexity of the spatial relationships.
    */
    public TopologyLevel getTopologyLevel() {
        return topologyLevel;
    }

    /**
     * Set the code which identifies the degree of complexity of the spatial relationships.
     */
    public synchronized void setTopologyLevel(final TopologyLevel newValue) {
        checkWritePermission();
        topologyLevel = newValue;
    }

    /**
     * Information about the geometric objects used in the dataset.
     */
    public Set getGeometricObjects() {
        final Set geometricObjects = this.geometricObjects; // Avoid synchronization
        return (geometricObjects!=null) ? geometricObjects : Collections.EMPTY_SET;
    }

    /**
     * Set information about the geometric objects used in the dataset.
     */
    public synchronized void setGeometricObjects(final Set newValues) {
        checkWritePermission();
        if (geometricObjects == null) {
            geometricObjects = new CheckedHashSet(GeometricObjects.class);
        } else {
            geometricObjects.clear();
        }
        geometricObjects.addAll(newValues);
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        topologyLevel    = (TopologyLevel) unmodifiable(topologyLevel);
        geometricObjects = (Set)           unmodifiable(geometricObjects);
    }

    /**
     * Compare this vector spatial representation with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final VectorSpatialRepresentation that = (VectorSpatialRepresentation) object; 
            return Utilities.equals(this.topologyLevel,    that.topologyLevel) &&
                   Utilities.equals(this.geometricObjects, that.geometricObjects);
        }
        return false;
    }

    /**
     * Returns a hash code value for this representation. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (topologyLevel != null)        code ^= topologyLevel.hashCode();
        if (geometricObjects != null)     code ^= geometricObjects.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this representation.
     */
    public String toString() {
        return String.valueOf(geometricObjects);
    }                
}
