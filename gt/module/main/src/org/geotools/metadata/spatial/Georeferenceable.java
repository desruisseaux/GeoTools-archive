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
import org.opengis.metadata.citation.Citation;
import org.opengis.util.InternationalString;
import org.opengis.metadata.spatial.CellGeometry;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.metadata.spatial.GridSpatialRepresentation;
import org.geotools.util.CheckedHashSet;


/**
 * Grid with cells irregularly spaced in any given geographic/map projection coordinate
 * system, whose individual cells can be geolocated using geolocation information
 * supplied with the data but cannot be geolocated from the grid properties alone.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class Georeferenceable extends GridSpatialRepresentation
        implements org.opengis.metadata.spatial.Georeferenceable
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5203270142818028946L;

    /**
     * Indication of whether or not control point(s) exists.
     */
    private boolean controlPointAvailable;

    /**
     * Indication of whether or not orientation parameters are available.
     */
    private boolean orientationParameterAvailable;

    /**
     * Description of parameters used to describe sensor orientation.
     */
    private InternationalString orientationParameterDescription;

    /**
     * Terms which support grid data georeferencing.
     */
    private Object parameters;

    /**
     * Reference providing description of the parameters.
     */
    private Set parameterCitation;

    /**
     * Construct an initially empty georeferenceable.
     */
    public Georeferenceable() {
    }

    /**
     * Creates a georeferencable initialized to the given parameters.
     */
    public Georeferenceable(final int numberOfDimensions,
                            final Set axisDimensionsProperties,
                            final CellGeometry cellGeometry,
                            final boolean transformationParameterAvailable,
                            final boolean controlPointAvailable, 
                            final boolean orientationParameterAvailable)
    {
        super(numberOfDimensions, axisDimensionsProperties, cellGeometry, transformationParameterAvailable);
        setControlPointAvailable        (controlPointAvailable        );
        setOrientationParameterAvailable(orientationParameterAvailable);
    }

    /**
     * Indication of whether or not control point(s) exists.
     */
    public boolean isControlPointAvailable() {
        return controlPointAvailable;
    }

    /**
     * Set an indication of whether or not control point(s) exists.
     */
    public synchronized void setControlPointAvailable(final boolean newValue) {
       checkWritePermission();
       controlPointAvailable = newValue;
    }
    
    /**
     * Indication of whether or not orientation parameters are available.
     */
    public boolean isOrientationParameterAvailable() {
        return orientationParameterAvailable;
    }

    /**
     * Set an indication of whether or not orientation parameters are available.
     */
    public synchronized void setOrientationParameterAvailable(final boolean newValue) {
        checkWritePermission();
        orientationParameterAvailable = newValue;
    }

    /**
     * Description of parameters used to describe sensor orientation.
     */
    public InternationalString getOrientationParameterDescription() {
        return orientationParameterDescription;
    }

    /**
     * Set a description of parameters used to describe sensor orientation.
     */
     public synchronized void setOrientationParameterDescription(final InternationalString newValue) {
        checkWritePermission();
        orientationParameterDescription = newValue;
    }

    /**
     * Terms which support grid data georeferencing.
     */
    public Object getParameters() {
        return parameters;
    }

    /**
     * Set terms which support grid data georeferencing.
     */
    public synchronized void setParameters(final Object newValue) {
        checkWritePermission();
        parameters = newValue;
    }

    /**
     * Reference providing description of the parameters.
     */
    public Set getParameterCitation() {
        final Set parameterCitation = this.parameterCitation; // Avoid synchronization
        return (parameterCitation!=null) ? parameterCitation : Collections.EMPTY_SET;
    }

    /**
     * Set reference providing description of the parameters.
     */
    public synchronized void setParameterCitation(final Set newValues) {
        checkWritePermission();
        if (parameterCitation == null) {
            parameterCitation = new CheckedHashSet(Citation.class);
        } else {
            parameterCitation.clear();
        }
        parameterCitation.addAll(newValues);
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        orientationParameterDescription = (InternationalString) unmodifiable(orientationParameterDescription);
        parameters                      = (Object)              unmodifiable(parameters);
        parameterCitation               = (Set)                 unmodifiable(parameterCitation);
    }

    /**
     * Compare this georeferenceable object with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Georeferenceable that = (Georeferenceable) object; 
            return Utilities.equals(this.parameters,
                                    that.parameters) &&
                   Utilities.equals(this.parameterCitation,
                                    that.parameterCitation) &&
                   Utilities.equals(this.orientationParameterDescription,
                                    that.orientationParameterDescription) &&
                   (this.controlPointAvailable         == that.controlPointAvailable) &&
                   (this.orientationParameterAvailable == that.orientationParameterAvailable);
        }
        return false;
    }

    /**
     * Returns a hash code value for this object. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (parameters != null)        code ^= parameters.hashCode();
        if (parameterCitation != null) code ^= parameterCitation.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return String.valueOf(parameterCitation);
    }            
}
