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
package org.geotools.metadata.content;

// OpenGIS dependencies
import org.geotools.resources.Utilities;
import org.opengis.metadata.content.CoverageContentType;
import org.opengis.metadata.content.RangeDimension;


/**
 * Location of the responsible individual or organization.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class CoverageDescription extends ContentInformation
       implements org.opengis.metadata.content.CoverageDescription
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2303929749109678792L;
    
    /**
     * Description of the attribute described by the measurement value.
     */
    private Class attributeDescription;

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
    public CoverageDescription() {
    }
    
    /**
     * Returns the description of the attribute described by the measurement value.
     */
    public Class getAttributeDescription() {
        return attributeDescription;
    }
    
    /**
     * Set the description of the attribute described by the measurement value.
     */
    public synchronized void setAttributeDescription(final Class newValue) {
        checkWritePermission();
        attributeDescription = newValue;
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
    public synchronized void setContentType(final CoverageContentType newValue) {
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
    public synchronized void setDimension(final RangeDimension newValue) {
        checkWritePermission();
        dimension = newValue;
    }     
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        attributeDescription = (Class)          unmodifiable(attributeDescription);
        dimension            = (RangeDimension) unmodifiable(dimension);        
    }

    /**
     * Compare this coverage description with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final CoverageDescription that = (CoverageDescription) object;
            return Utilities.equals(this.attributeDescription, that.attributeDescription ) &&
                   Utilities.equals(this.contentType,          that.contentType          ) &&
                   Utilities.equals(this.dimension,            that.dimension            )  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this coverage description. For performance reason, this
     * method do not uses all attributes for computing the hash code. Instead, it uses the
     * attributes that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (attributeDescription != null)  code ^= attributeDescription.hashCode();
        if (contentType          != null)  code ^= contentType         .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this coverage description.
     */
    public String toString() {
        return String.valueOf(attributeDescription);
    }        
}
