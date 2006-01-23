/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;

// J2SE dependencies
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public class ExternalGraphicImpl extends AbstractGTComponent
    implements ExternalGraphic, Symbol, Cloneable {
    /** The logger for the default core module. */
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
        .getLogger("org.geotools.core");
    private URL location = null;
    private String format = null;
    private String uri = null;
    private Map customProps = null;

    public void setURI(String uri) {
        this.uri = uri;
        fireChanged();
    }

    /**
     * Provides the format of the external graphic.
     *
     * @return The format of the external graphic.  Reported as its MIME type
     *         in a String object.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Provides the URL for where the external graphic resouce can be located.
     *
     * @return The URL of the ExternalGraphic
     *
     * @throws MalformedURLException DOCUMENT ME!
     */
    public java.net.URL getLocation() throws MalformedURLException {
        if (location == null) {
            location = new URL(uri);
        }

        return location;
    }

    /**
     * Setter for property Format.
     *
     * @param format New value of property Format.
     */
    public void setFormat(java.lang.String format) {
        this.format = format;
    }

    /**
     * Setter for property location.
     *
     * @param location New value of property location.
     */
    public void setLocation(java.net.URL location) {
        this.uri = location.toString();
        this.location = location;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns a clone of the ExternalGraphic
     *
     * @see org.geotools.styling.ExternalGraphic#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // This will never happen
            throw new AssertionError(e);
        }
    }

    /**
     * Generates a hashcode for the ExternalGraphic
     *
     * @return The hash code.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (format != null) {
            result = (PRIME * result) + format.hashCode();
        }

        if (uri != null) {
            result = (PRIME * result) + uri.hashCode();
        }

        return result;
    }

    /**
     * Compares this ExternalGraphi with another.
     * 
     * <p>
     * Two external graphics are equal if they have the same uri and format.
     * </p>
     *
     * @param oth The other External graphic.
     *
     * @return True if this and the other external graphic are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof ExternalGraphicImpl) {
            ExternalGraphicImpl other = (ExternalGraphicImpl) oth;

            return Utilities.equals(uri, other.uri)
            && Utilities.equals(format, other.format);
        }

        return false;
    }

    public java.util.Map getCustomProperties() {
        return customProps;
    }

    public void setCustomProperties(java.util.Map list) {
        customProps = list;
    }
}
