/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.metadata;

// J2SE direct dependencies
import java.net.URL;

// OpenGIS direct dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.SpatialAttributeSupplement;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Information about the application schema used to build the dataset.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class ApplicationSchemaInformation extends MetadataEntity
        implements org.opengis.metadata.ApplicationSchemaInformation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3109191272905767382L;
    
    /**
     * Name of the application schema used.
     */
    private Citation name;

    /**
     * Identification of the schema language used.
     */
    private String schemaLanguage;

    /**
     * Formal language used in Application Schema.
     */
    private String constraintLanguage;

    /**
     * Full application schema given as an ASCII file.
     */
    private URL schemaAscii;

    /**
     * Full application schema given as a graphics file.
     */
    private URL graphicsFile;

    /**
     * Full application schema given as a software development file.
     */
    private URL softwareDevelopmentFile;

    /**
     * Software dependent format used for the application schema software dependent file.
     */
    private String softwareDevelopmentFileFormat;

    /**
     * Information about the spatial attributes in the application schema for the feature types.
     */
    private SpatialAttributeSupplement featureCatalogueSupplement;

    /**
     * Construct an initially empty application schema information.
     */
    public ApplicationSchemaInformation() {
    }

    /**
     * Creates a application schema information initialized to the specified values.
     */
    public ApplicationSchemaInformation(final Citation name,
                                        final String schemaLanguage,
                                        final String constraintLanguage)
    {
        setName              (name              );
        setSchemaLanguage    (schemaLanguage    );
        setConstraintLanguage(constraintLanguage);
    }
    
    /**
     * Name of the application schema used.
     */
    public Citation getName() {
        return name;
    }

    /**
     * Set the name of the application schema used.
     */
    public synchronized void setName(final Citation newValue) {
        checkWritePermission();
        name = newValue;
    }

    /**
     * Identification of the schema language used.
     */
    public String getSchemaLanguage() {
        return schemaLanguage;
    }

    /**
     * Set the identification of the schema language used.
     */
    public synchronized void setSchemaLanguage(final String newValue) {
        checkWritePermission();
        schemaLanguage = newValue;
    }

    /**
     * Formal language used in Application Schema.
     */
    public String getConstraintLanguage()  {
        return constraintLanguage;
    }

    /**
     * Set the formal language used in Application Schema.
     */
    public synchronized void setConstraintLanguage(final String newValue) {
        checkWritePermission();
        constraintLanguage = newValue;
    }

    /**
     * Full application schema given as an ASCII file.
     */
    public URL getSchemaAscii()  {
        return schemaAscii;
    }

    /**
     * Set the full application schema given as an ASCII file.
     */
    public synchronized void setSchemaAscii(final URL newValue) {
        checkWritePermission();
        schemaAscii = newValue;
    }

    /**
     * Full application schema given as a graphics file.
     */
    public URL getGraphicsFile()  {
        return graphicsFile;
    }

    /**
     * Set the full application schema given as a graphics file.
     */
    public synchronized void setGraphicsFile(final URL newValue) {
        checkWritePermission();
        graphicsFile = newValue;
    }

    /**
     * Full application schema given as a software development file.
     */
    public URL getSoftwareDevelopmentFile()  {
        return softwareDevelopmentFile;
    }

    /**
     * Set the full application schema given as a software development file.
     */
    public synchronized void setSoftwareDevelopmentFile(final URL newValue) {
        checkWritePermission();
        softwareDevelopmentFile = newValue;
    }

    /**
     * Software dependent format used for the application schema software dependent file.
     */
    public String getSoftwareDevelopmentFileFormat()  {
        return softwareDevelopmentFileFormat;
    }

    /**
     * Set the software dependent format used for the application schema software dependent file.
     */
    public synchronized void setSoftwareDevelopmentFileFormat(final String newValue) {
        checkWritePermission();
        softwareDevelopmentFileFormat = newValue;
    }

    /**
     * Information about the spatial attributes in the application schema for the feature types.
     */
    public SpatialAttributeSupplement getFeatureCatalogueSupplement() {
        return featureCatalogueSupplement;
    }

    /**
     * Set information about the spatial attributes in the application schema for the feature types.
     */
    public synchronized void setFeatureCatalogueSupplement(final SpatialAttributeSupplement newValue) {
        checkWritePermission();
        featureCatalogueSupplement = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        name                          = (Citation)                  unmodifiable(name);
        schemaLanguage                = (String)                    unmodifiable(schemaLanguage);
        constraintLanguage            = (String)                    unmodifiable(constraintLanguage);
        schemaAscii                   = (URL)                       unmodifiable(schemaAscii);
        graphicsFile                  = (URL)                       unmodifiable(graphicsFile);
        softwareDevelopmentFile       = (URL)                       unmodifiable(softwareDevelopmentFile);
        softwareDevelopmentFileFormat = (String)                    unmodifiable(softwareDevelopmentFileFormat);
        featureCatalogueSupplement    = (SpatialAttributeSupplement)unmodifiable(featureCatalogueSupplement);
    }

    /**
     * Compare this application schema information with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ApplicationSchemaInformation that = (ApplicationSchemaInformation) object;
            return Utilities.equals(this.name,                          that.name                         ) &&
                   Utilities.equals(this.schemaLanguage,                that.schemaLanguage               ) &&
                   Utilities.equals(this.constraintLanguage,            that.constraintLanguage           ) &&
                   Utilities.equals(this.schemaAscii,                   that.schemaAscii                  ) &&
                   Utilities.equals(this.graphicsFile,                  that.graphicsFile                 ) &&
                   Utilities.equals(this.softwareDevelopmentFile,       that.softwareDevelopmentFile      ) &&
                   Utilities.equals(this.softwareDevelopmentFileFormat, that.softwareDevelopmentFileFormat) &&
                   Utilities.equals(this.featureCatalogueSupplement,    that.featureCatalogueSupplement   );
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
        if (name           != null) code ^= name          .hashCode();
        if (schemaLanguage != null) code ^= schemaLanguage.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return String.valueOf(name);
    }        
}
