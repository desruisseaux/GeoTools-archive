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
package org.geotools.metadata.iso.distribution;

// J2SE direct dependencies
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.distribution.Distributor;
import org.opengis.metadata.distribution.Format;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Description of the computer language construct that specifies the representation
 * of data objects in a record, file, message, storage device or transmission channel.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class FormatImpl extends MetadataEntity implements Format {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6498897239493553607L;
    
    /**
     * Name of the data transfer format(s).
     */
    private InternationalString name;

    /**
     * Version of the format (date, number, etc.).
     */
    private InternationalString version;
     
    /**
     * Amendment number of the format version.
     */
    private InternationalString amendmentNumber;

    /**
     * Name of a subset, profile, or product specification of the format.
     */
    private InternationalString specification;

    /**
     * Recommendations of algorithms or processes that can be applied to read or
     * expand resources to which compression techniques have been applied.
     */
    private InternationalString fileDecompressionTechnique;

    /**
     * Provides information about the distributor’s format.
     */
    private Collection formatDistributors;
    
    /**
     * Constructs an initially empty format.
     */
    public FormatImpl() {
    }

    /**
     * Creates a format initialized to the given name.
     */
    public FormatImpl(final InternationalString name, final InternationalString version) {
        setName   (name   );
        setVersion(version);
    }

    /**
     * Returns the name of the data transfer format(s).
     */
    public InternationalString getName() {
        return name; 
    }
     
    /**
     * Set the name of the data transfer format(s).
     */
    public synchronized void setName(final InternationalString newValue) {
         checkWritePermission();
         name = newValue;
     }

    /**
     * Returne the version of the format (date, number, etc.).
     */
    public InternationalString getVersion() {
        return version;
    }
     
    /**
     * Set the version of the format (date, number, etc.).
     */
    public synchronized void setVersion(final InternationalString newValue) {
        checkWritePermission();
        version = newValue;
    }

    /**
     * Returns the amendment number of the format version.
     */
    public InternationalString getAmendmentNumber() {
        return amendmentNumber;
    }

    /**
     * Set the amendment number of the format version.
     */
    public synchronized void setAmendmentNumber(final InternationalString newValue) {
        checkWritePermission();
        amendmentNumber = newValue;
    }

    /**
     * Returns the name of a subset, profile, or product specification of the format.
     */
    public InternationalString getSpecification() {
        return specification;
    }

    /**
     * Set the name of a subset, profile, or product specification of the format.
     */
    public synchronized void setSpecification(final InternationalString newValue) {
        checkWritePermission();
        specification = newValue;
    }

    /**
     * Returns recommendations of algorithms or processes that can be applied to read or
     * expand resources to which compression techniques have been applied.
     */
    public InternationalString getFileDecompressionTechnique() {
        return fileDecompressionTechnique;
    }

    /**
     * Set recommendations of algorithms or processes that can be applied to read or
     * expand resources to which compression techniques have been applied.
     */
    public synchronized void setFileDecompressionTechnique(final InternationalString newValue) {
        checkWritePermission();
        fileDecompressionTechnique = newValue;        
    }

    /**
     * Provides information about the distributor’s format.
     */
    public synchronized Collection getFormatDistributors() {
        return formatDistributors = nonNullCollection(formatDistributors, Distributor.class);
    }
    
    /**
     * Set information about the distributor’s format.
     */
    public synchronized void setFormatDistributors(final Collection newValues) {
        formatDistributors = copyCollection(newValues, formatDistributors, Distributor.class);
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        name                       = (InternationalString) unmodifiable(name);
        version                    = (InternationalString) unmodifiable(version);
        amendmentNumber            = (InternationalString) unmodifiable(amendmentNumber);
        specification              = (InternationalString) unmodifiable(specification);
        fileDecompressionTechnique = (InternationalString) unmodifiable(fileDecompressionTechnique);
        formatDistributors         = (Collection)          unmodifiable(formatDistributors);
    }

    /**
     * Compare this Format with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final FormatImpl that = (FormatImpl) object;
            return Utilities.equals(this.name,                       that.name                      ) &&
                   Utilities.equals(this.version,                    that.version                   ) &&
                   Utilities.equals(this.amendmentNumber,            that.amendmentNumber           ) &&
                   Utilities.equals(this.specification,              that.specification             ) &&
                   Utilities.equals(this.fileDecompressionTechnique, that.fileDecompressionTechnique) &&
                   Utilities.equals(this.formatDistributors,         that.formatDistributors        );
        }
        return false;
    }

    /**
     * Returns a hash code value for this series.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (name                       != null) code ^= name                      .hashCode();
        if (version                    != null) code ^= version                   .hashCode();
        if (amendmentNumber            != null) code ^= amendmentNumber           .hashCode();
        if (specification              != null) code ^= specification             .hashCode();
        if (fileDecompressionTechnique != null) code ^= fileDecompressionTechnique.hashCode();
        if (formatDistributors         != null) code ^= formatDistributors        .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this series.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(name);
    }
}
