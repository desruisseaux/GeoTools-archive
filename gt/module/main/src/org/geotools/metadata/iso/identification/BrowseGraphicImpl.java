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
package org.geotools.metadata.iso.identification;

// J2SE direct dependencies
import java.net.URI;

// OpenGIS dependencies
import org.opengis.metadata.identification.BrowseGraphic;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Graphic that provides an illustration of the dataset (should include a legend for the graphic).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class BrowseGraphicImpl extends MetadataEntity implements BrowseGraphic {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 1715873406472953616L;

    /**
     * Name of the file that contains a graphic that provides an illustration of the dataset.
     */
    private URI applicationSchemaInformation;

    /**
     * Name of the file that contains a graphic that provides an illustration of the dataset.
     */
    private URI fileName;

    /**
     * Text description of the illustration.
     */
    private InternationalString fileDescription;

    /**
     * Format in which the illustration is encoded.
     * Examples: CGM, EPS, GIF, JPEG, PBM, PS, TIFF, XWD.
     */
    private String fileType;

    /**
     * Constructs an initially empty browse graphic.
     */
    public BrowseGraphicImpl() {
    }

    /**
     * Creates a browse graphics initialized to the specified URI.
     */
    public BrowseGraphicImpl(final URI fileName) {
        setFileName(fileName);
    }
    
    /**
     * Name of the file that contains a graphic that provides an illustration of the dataset.
     */
    public URI getApplicationSchemaInformation() {
        return applicationSchemaInformation;
    }

    /**
     * Set the name of the file that contains a graphic that provides an illustration of the
     * dataset.
     */
    public synchronized void setApplicationSchemaInformation(final URI newValue) {
        checkWritePermission();
        applicationSchemaInformation = newValue;
    }

    /**
     * Name of the file that contains a graphic that provides an illustration of the dataset.
     */
    public URI getFileName() {
        return fileName;
    }

    /**
     * Set the name of the file that contains a graphic that provides an illustration of the
     * dataset.
     */
    public synchronized void setFileName(final URI newValue) {
        checkWritePermission();
        fileName = newValue;
    }

    /**
     * Text description of the illustration.
     */
    public InternationalString getFileDescription() {
        return fileDescription;
    }

    /**
     * Set the text description of the illustration.
     */
    public synchronized void setFileDescription(final InternationalString newValue)  {
        checkWritePermission();
        fileDescription = newValue;
    }

    /**
     * Format in which the illustration is encoded.
     * Examples: CGM, EPS, GIF, JPEG, PBM, PS, TIFF, XWD.
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Set the format in which the illustration is encoded.
     */
    public synchronized void setFileType(final String newValue)  {
        checkWritePermission();
        fileType = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        fileDescription = (InternationalString) unmodifiable(fileDescription);
    }

    /**
     * Compare this browse graphic with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final BrowseGraphicImpl that = (BrowseGraphicImpl) object;
            return Utilities.equals(this.fileName,        that.fileName        ) &&
                   Utilities.equals(this.fileDescription, that.fileDescription ) &&
                   Utilities.equals(this.fileType,        that.fileType        )  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this browse graphics.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (fileName         != null) code ^= fileName       .hashCode();
        if (fileDescription  != null) code ^= fileDescription.hashCode();
        if (fileType         != null) code ^= fileType       .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this browse graphics.
     */
    public String toString() {
        return String.valueOf(fileName);
    }        
}
