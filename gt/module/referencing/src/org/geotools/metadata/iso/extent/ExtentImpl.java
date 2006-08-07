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
package org.geotools.metadata.iso.extent;

// J2SE dependencies
import java.util.Iterator;
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.BoundingPolygon;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.TemporalExtent;
import org.opengis.metadata.extent.VerticalExtent;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Information about spatial, vertical, and temporal extent.
 * This interface has three optional attributes
 * ({@linkplain #getGeographicElements geographic elements},
 *  {@linkplain #getTemporalElements temporal elements}, and
 *  {@linkplain #getVerticalElements vertical elements}) and an element called
 *  {@linkplain #getDescription description}.
 *  At least one of the four shall be used.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class ExtentImpl extends MetadataEntity implements Extent {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7812213837337326257L;

    /**
     * A geographic extent ranging from 180°W to 180°E and 90°S to 90°N.
     *
     * @since 2.2
     */
    public static final Extent WORLD;
    static {
        final ExtentImpl world = new ExtentImpl();
        world.getGeographicElements().add(GeographicBoundingBoxImpl.WORLD);
        world.freeze();
        WORLD = world;
    }

    /**
     * Returns the spatial and temporal extent for the referring object.
     */
    private InternationalString description;

    /**
     * Provides geographic component of the extent of the referring object
     */
    private Collection/*<GeographicExtent>*/ geographicElements;

    /**
     * Provides temporal component of the extent of the referring object
     */
    private Collection/*<TemporalExtent>*/ temporalElements;

    /**
     * Provides vertical component of the extent of the referring object
     */
    private Collection/*<VerticalExtent>*/ verticalElements;

    /**
     * Constructs an initially empty extent.
     */
    public ExtentImpl() {
    }

    /**
     * Returns the spatial and temporal extent for the referring object.
     */
    public InternationalString getDescription() {
        return description;
    }

    /**
     * Set the spatial and temporal extent for the referring object.
     */
    public synchronized void setDescription(final InternationalString newValue) {
        checkWritePermission();
        description = newValue;
    }

    /**
     * Provides geographic component of the extent of the referring object
     */
    public synchronized Collection/*<GeographicExtent>*/ getGeographicElements() {
        return geographicElements = nonNullCollection(geographicElements, GeographicExtent.class);
    }

    /**
     * Set geographic component of the extent of the referring object
     */
    public synchronized void setGeographicElements(final Collection/*<GeographicExtent>*/ newValues) {
        geographicElements = copyCollection(newValues, geographicElements, GeographicExtent.class);
    }

    /**
     * Provides temporal component of the extent of the referring object
     */
    public synchronized Collection/*<TemporalExtent>*/ getTemporalElements() {
        return temporalElements = nonNullCollection(temporalElements, TemporalExtent.class);
    }

    /**
     * Set temporal component of the extent of the referring object
     */
    public synchronized void setTemporalElements(final Collection/*<TemporalExtent>*/ newValues) {
        temporalElements = copyCollection(newValues, temporalElements, TemporalExtent.class);
    }

    /**
     * Provides vertical component of the extent of the referring object
     */
    public synchronized Collection/*<VerticalExtent>*/ getVerticalElements() {
        return verticalElements = nonNullCollection(verticalElements, VerticalExtent.class);
    }

    /**
     * Set vertical component of the extent of the referring object
     */
    public synchronized void setVerticalElements(final Collection/*<VerticalExtent>*/ newValues) {
        verticalElements = copyCollection(newValues, verticalElements, VerticalExtent.class);
    }

    /**
     * Convenience method returning a single geographic bounding box from the specified extent.
     * If no bounding box was found, then this method returns {@code null}. If more than one box
     * is found, then boxes are {@linkplain GeographicBoundingBoxImpl#add added} together.
     *
     * @since 2.2
     */
    public static GeographicBoundingBox getGeographicBoundingBox(final Extent extent) {
        GeographicBoundingBox candidate = null;
        if (extent != null) {
            GeographicBoundingBoxImpl modifiable = null;
            for (final Iterator it=extent.getGeographicElements().iterator(); it.hasNext();) {
                final GeographicExtent element = (GeographicExtent) it.next();
                final GeographicBoundingBox bounds;
                if (element instanceof GeographicBoundingBox) {
                    bounds = (GeographicBoundingBox) element;
                } else if (element instanceof BoundingPolygon) {
                    // TODO: iterates through all polygons and invoke Polygon.getEnvelope();
                    continue;
                } else {
                    continue;
                }
                /*
                 * A single geographic bounding box has been extracted. Now add it to previous
                 * ones (if any). All exclusion boxes before the first inclusion box are ignored.
                 */
                if (candidate == null) {
                    if (bounds.getInclusion()) {
                        candidate = bounds;
                    }
                } else {
                    if (modifiable == null) {
                        modifiable = new GeographicBoundingBoxImpl(candidate);
                        candidate = modifiable;
                    }
                    modifiable.add(bounds);
                }
            }
            if (modifiable != null) {
                modifiable.freeze();
            }
        }
        return candidate;
    }

    /**
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        description        = (InternationalString) unmodifiable(description);
        geographicElements = (Collection)          unmodifiable(geographicElements);
        temporalElements   = (Collection)          unmodifiable(temporalElements);
        verticalElements   = (Collection)          unmodifiable(verticalElements);
    }

    /**
     * Compare this extent with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ExtentImpl that = (ExtentImpl) object;
            return Utilities.equals(this.description,        that.description       ) &&
                   Utilities.equals(this.geographicElements, that.geographicElements) &&
                   Utilities.equals(this.temporalElements,   that.temporalElements  ) &&
                   Utilities.equals(this.verticalElements,   that.verticalElements  )  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this extent.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (description        != null) code ^= description       .hashCode();
        if (geographicElements != null) code ^= geographicElements.hashCode();
        if (temporalElements   != null) code ^= temporalElements  .hashCode();
        if (verticalElements   != null) code ^= verticalElements  .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this extent.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        if (description != null) {
            buffer.append(description);
        }
        final GeographicBoundingBox box = getGeographicBoundingBox(this);
        if (box != null) {
            if (buffer.length() != 0) {
                buffer.append(System.getProperty("line.separator"));
            }
            buffer.append(GeographicBoundingBoxImpl.toString(box, "DD°MM'SS.s\"", null));
        }
        return buffer.toString();
    }
}
