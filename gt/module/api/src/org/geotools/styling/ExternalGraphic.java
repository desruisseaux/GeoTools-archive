/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
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
 */
/*
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Center for Computational Geography
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
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */
package org.geotools.styling;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


/**
 * Holds a reference to an external graphics file with a URL to its location
 * and its expected MIME type. Knowing the MIME type in advance allows stylers
 * to select best-supported formats from a list of external graphics.
 * 
 * <p></p>
 * 
 * <p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="ExternalGraphic"&gt;
 *   &lt;xsd:annotation&gt;
 *     &lt;xsd:documentation&gt;
 *       An "ExternalGraphic" gives a reference to an external raster or
 *       vector graphical object.
 *     &lt;/xsd:documentation&gt;
 *   &lt;/xsd:annotation&gt;
 *   &lt;xsd:complexType&gt;
 *     &lt;xsd:sequence&gt;
 *       &lt;xsd:element ref="sld:OnlineResource"/&gt;
 *       &lt;xsd:element ref="sld:Format"/&gt;
 *     &lt;/xsd:sequence&gt;
 *   &lt;/xsd:complexType&gt;
 * &lt;/xsd:element&gt;
 * </code></pre>
 * </p>
 * 
 * <p>
 * Renderers can use this information when displaying styled features, though
 * it must be remembered that not all renderers will be able to fully
 * represent strokes as set out by this interface.  For example, opacity may
 * not be supported.
 * </p>
 * 
 * <p>
 * Notes:
 * 
 * <ul>
 * <li>
 * The graphical parameters and their values are derived from SVG/CSS2
 * standards with names and semantics which are as close as possible.
 * </li>
 * </ul>
 * </p>
 *
 * @author James Macgill, CCG
 * @source $URL$
 * @version $Id$
 */
public interface ExternalGraphic extends Symbol {
	public static final ExternalGraphic[] EXTERNAL_GRAPHICS_EMPTY = new ExternalGraphic[0];
    /**
     * converts a URI in a string to the location URL
     *
     * @param uri the uri of the external graphic
     */
    public void setURI(String uri);

    /**
     * Provides the URL for where the external graphic resource can be located.
     *
     * @return The URL of the ExternalGraphic
     *
     * @throws MalformedURLException If the url held in the ExternalGraphic is
     *         malformed.
     */
    URL getLocation() throws MalformedURLException;

    /**
     * Provides the URL for where the external graphic resource can be located.
     *
     * @param url The URL of the ExternalGraphic
     */
    void setLocation(URL url);

    /**
     * Provides the format of the external graphic.
     *
     * @return The format of the external graphic.  Reported as its MIME type
     *         in a String object.
     */
    String getFormat();

    /**
     * Provides the format of the external graphic.
     *
     * @param format The format of the external graphic.  Reported as its MIME
     *        type in a String object.
     */
    void setFormat(String format);

    public void setCustomProperties(Map list);

    public Map getCustomProperties();
}
