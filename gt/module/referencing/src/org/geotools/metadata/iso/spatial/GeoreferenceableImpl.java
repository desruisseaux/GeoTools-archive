/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.metadata.iso.spatial;

// J2SE direct dependencies
import java.util.List;
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.spatial.CellGeometry;
import org.opengis.metadata.spatial.Georeferenceable;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Grid with cells irregularly spaced in any given geographic/map projection coordinate
 * system, whose individual cells can be geolocated using geolocation information
 * supplied with the data but cannot be geolocated from the grid properties alone.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class GeoreferenceableImpl extends GridSpatialRepresentationImpl implements Georeferenceable {
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
    private Collection parameterCitation;

    /**
     * Constructs an initially empty georeferenceable.
     */
    public GeoreferenceableImpl() {
    }

    /**
     * Creates a georeferencable initialized to the given parameters.
     */
    public GeoreferenceableImpl(final int          numberOfDimensions,
                            final List         axisDimensionsProperties,
                            final CellGeometry cellGeometry,
                            final boolean      transformationParameterAvailable,
                            final boolean      controlPointAvailable, 
                            final boolean      orientationParameterAvailable)
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
    public synchronized Collection getParameterCitation() {
        return parameterCitation = nonNullCollection(parameterCitation, Citation.class);
    }

    /**
     * Set reference providing description of the parameters.
     */
    public synchronized void setParameterCitation(final Collection newValues) {
        parameterCitation = copyCollection(newValues, parameterCitation, Citation.class);
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        orientationParameterDescription = (InternationalString) unmodifiable(orientationParameterDescription);
        parameters                      = (Object)              unmodifiable(parameters);
        parameterCitation               = (Collection)          unmodifiable(parameterCitation);
    }

    /**
     * Compare this georeferenceable object with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GeoreferenceableImpl that = (GeoreferenceableImpl) object; 
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
