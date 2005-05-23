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
package org.geotools.metadata.iso.citation;

// J2SE direct dependencies
import java.net.URI;
import java.net.URISyntaxException;

// OpenGIS dependencies
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Information about on-line sources from which the dataset, specification, or
 * community profile name and extended metadata elements can be obtained.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class OnLineResourceImpl extends MetadataEntity implements OnLineResource {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5412370008274334799L;
    
    /**
     * The online resources for the <A HREF="http://www.opengeospatial.org">Open Geospatial Consortium</A>.
     * "Open Geospatial consortium" is the new name for "OpenGIS consortium".
     *
     * @see #OPEN_GIS
     */
    public static final OnLineResource OGC;
    
    /**
     * The online resources for the <A HREF="http://www.opengis.org">OpenGIS consortium</A>.
     * "OpenGIS consortium" is the old name for "Open Geospatial consortium".
     *
     * @see #OGC
     */
    public static final OnLineResource OPEN_GIS;
    
    /**
     * The online resources for the
     * <A HREF="http://www.epsg.org">European Petroleum Survey Group</A>.
     */
    public static final OnLineResource EPSG;

    /**
     * The online resources for the
     * <A HREF="http://www.remotesensing.org/geotiff/geotiff.html">GeoTIFF</A> group.
     */
    public static final OnLineResource GEOTIFF;

    /**
     * The online resources for <A HREF="http://www.esri.com">ESRI</A>.
     */
    public static final OnLineResource ESRI;

    /**
     * The online resources for <A HREF="http://www.oracle.com">Oracle</A>.
     */
    public static final OnLineResource ORACLE;
    
    /**
     * The online resources for the <A HREF="http://www.geotools.org">Geotools</A> project.
     */
    public static final OnLineResource GEOTOOLS;
    static {
        OnLineResourceImpl r;
        try {
            OGC = r = new OnLineResourceImpl(new URI("http://www.opengeospatial.org/"));
            r.setFunction(OnLineFunction.INFORMATION);
            r.freeze();

            OPEN_GIS = r = new OnLineResourceImpl(new URI("http://www.opengis.org"));
            r.setFunction(OnLineFunction.DOWNLOAD);
            r.freeze();

            EPSG = r = new OnLineResourceImpl(new URI("http://www.epsg.org"));
            r.setFunction(OnLineFunction.DOWNLOAD);
            r.freeze();

            GEOTIFF = r = new OnLineResourceImpl(new URI("http://www.remotesensing.org/geotiff"));
            r.setFunction(OnLineFunction.DOWNLOAD);
            r.freeze();

            ESRI = r = new OnLineResourceImpl(new URI("http://www.esri.com"));
            r.setFunction(OnLineFunction.INFORMATION);
            r.freeze();

            ORACLE = r = new OnLineResourceImpl(new URI("http://www.oracle.com"));
            r.setFunction(OnLineFunction.INFORMATION);
            r.freeze();

            GEOTOOLS = r = new OnLineResourceImpl(new URI("http://www.geotools.org"));
            r.setFunction(OnLineFunction.DOWNLOAD);
            r.freeze();
        } catch (URISyntaxException exception) {
            // Should never happen.
            throw new ExceptionInInitializerError(exception);
        }
    }
    
    /**
     * Name of an application profile that can be used with the online resource.
     */
    private String applicationProfile;
    
    /**
     * Detailed text description of what the online resource is/does.
     */
    private InternationalString description;

    /**
     * Code for function performed by the online resource.
     */
    private OnLineFunction function;
    
    /**
     * Location (address) for on-line access using a Uniform Resource Locator address or
     * similar addressing scheme such as http://www.statkart.no/isotc211.
     */
    private URI linkage;
    
    /**
     * Creates an initially empty on line resource.
     */
     public OnLineResourceImpl() {
     }
    
    /**
     * Creates an on line resource initialized to the given URI.
     */
     public OnLineResourceImpl(final URI linkage) {
         setLinkage(linkage);
     }
    
    /**
     * Returns the name of an application profile that can be used with the online resource.
     * Returns <code>null</code> if none.
     */
    public String getApplicationProfile() {
        return applicationProfile;
    }
    
    /**
     * Set the name of an application profile that can be used with the online resource.
     */
    public synchronized void setApplicationProfile(final String newValue) {
        checkWritePermission();
        applicationProfile = newValue;
    }

    /**
     * Returns the detailed text description of what the online resource is/does.
     * Returns <code>null</code> if none.
     */
    public InternationalString getDescription() {
        return description;
    }
    
    /**
     * Set the detailed text description of what the online resource is/does.
     */
    public synchronized void setDescription(final InternationalString newValue) {
        checkWritePermission();
        description = newValue;
    }
    
    /**
     * Returns the code for function performed by the online resource.
     * Returns <code>null</code> if unspecified.
     */
    public OnLineFunction getFunction() {
        return function;
    }
    
    /**
     * Set the code for function performed by the online resource.
     */
    public synchronized void setFunction(final OnLineFunction newValue) {
        checkWritePermission();
        function = newValue;
    }
    
    /**
     * Returns the location (address) for on-line access using a Uniform Resource Locator address or
     * similar addressing scheme such as http://www.statkart.no/isotc211.
     */
    public URI getLinkage() {
        return linkage;
    }
    
    /**
     * Set the location (address) for on-line access using a Uniform Resource Locator address or
     * similar addressing scheme such as http://www.statkart.no/isotc211.
     */
    public synchronized void setLinkage(final URI newValue) {
        checkWritePermission();
        linkage = newValue;
    }
    
    /**
     * Returns the connection protocol to be used. 
     * Returns <code>null</code> if none.
     */
    public String getProtocol() {
        final URI linkage = this.linkage;
        return (linkage!=null) ? linkage.getScheme() : null;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        description = (InternationalString) unmodifiable(description);
    }

    /**
     * Compare this on line resource with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final OnLineResourceImpl that = (OnLineResourceImpl) object;
            return Utilities.equals(this.applicationProfile, that.applicationProfile) &&
                   Utilities.equals(this.description,        that.description       ) &&
                   Utilities.equals(this.function,           that.function          ) &&
                   Utilities.equals(this.linkage,            that.linkage           );
        }
        return false;
    }

    /**
     * Returns a hash code value for this on line resource. For performance reason, this method
     * do not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (linkage != null) code ^= linkage.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this on line resource.
     */
    public String toString() {
        return linkage.toString();
    }    
}
