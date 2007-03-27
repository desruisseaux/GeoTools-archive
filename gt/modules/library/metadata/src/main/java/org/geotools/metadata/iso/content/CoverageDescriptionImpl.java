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
package org.geotools.metadata.iso.content;

// Geotools dependencies
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.opengis.metadata.content.CoverageDescription;

// OpenGIS dependencies
import org.geotools.resources.Utilities;
import org.opengis.metadata.content.CoverageContentType;
import org.opengis.metadata.content.RangeDimension;
import org.opengis.util.MemberName;
import org.opengis.util.Record;
import org.opengis.util.RecordSchema;
import org.opengis.util.RecordType;
import org.opengis.util.TypeName;

/**
 * Information about the content of a grid data cell.
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/metadata/src/main/java/org/geotools/metadata/iso/content/CoverageDescriptionImpl.java $
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 * @since 2.1
 */
public class CoverageDescriptionImpl extends ContentInformationImpl implements CoverageDescription {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2303929749109678792L;

    /**
     * Description of the attribute described by the measurement value.
     */
    private RecordType attributeDescription;

    /**
     * Type of information represented by the cell value.
     */
    private CoverageContentType contentType;

    /**
     * Information on the dimensions of the cell measurement value.
     */
    private RangeDimension dimension;

    /**
     * Constructs an empty coverage description.
     */
    public CoverageDescriptionImpl() {
    }

    /**
     * Returns the description of the attribute described by the measurement value.
     */
    public RecordType getAttributeDescription() {
        return attributeDescription;
    }

    /**
     * Set the description of the attribute described by the measurement value.
     */
    public synchronized void setAttributeDescription(final Class newValue) {
        checkWritePermission();
        attributeDescription = null;
    }
    /**
     * Returns the type of information represented by the cell value.
     */
    public CoverageContentType getContentType() {
        return contentType;
    }

    /**
     * Set the type of information represented by the cell value.
     */
    public synchronized void setContentType( final CoverageContentType newValue ) {
        checkWritePermission();
        contentType = newValue;
    }

    /**
     * Returns the information on the dimensions of the cell measurement value.
     */
    public RangeDimension getDimension() {
        return dimension;
    }

    /**
     * Set the information on the dimensions of the cell measurement value.
     */
    public synchronized void setDimension( final RangeDimension newValue ) {
        checkWritePermission();
        dimension = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        attributeDescription = (RecordType) unmodifiable(attributeDescription);
        dimension = (RangeDimension) unmodifiable(dimension);
    }

    /**
     * Compare this coverage description with the specified object for equality.
     */
    public synchronized boolean equals( final Object object ) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final CoverageDescriptionImpl that = (CoverageDescriptionImpl) object;
            return Utilities.equals(this.attributeDescription, that.attributeDescription)
                    && Utilities.equals(this.contentType, that.contentType)
                    && Utilities.equals(this.dimension, that.dimension);
        }
        return false;
    }

    /**
     * Returns a hash code value for this coverage description. For performance reason, this method
     * do not uses all attributes for computing the hash code. Instead, it uses the attributes that
     * are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int) serialVersionUID;
        if (attributeDescription != null)
            code ^= attributeDescription.hashCode();
        if (contentType != null)
            code ^= contentType.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this coverage description.
     */
    public String toString() {
        return String.valueOf(attributeDescription);
    }

    public Collection getDimensions(){
        return Collections.singleton( dimension);
    }
}
