/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 * NamedStyle.java
 *
 * Created on November 3, 2003, 12:40 PM
 */
package org.geotools.styling;

/**
 * A NamedStyle is used to refer to a style that has a name in a WMS.
 * 
 * <p>
 * A NamedStyle is a Style that has only Name, so all setters other than
 * setName will throw an <code>UnsupportedOperationException</code>
 * </p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="NamedStyle"&gt;
 *   &lt;xsd:annotation&gt;
 *     &lt;xsd:documentation&gt;
 *       A NamedStyle is used to refer to a style that has a name in a WMS.
 *     &lt;/xsd:documentation&gt;
 *   &lt;/xsd:annotation&gt;
 *   &lt;xsd:complexType&gt;
 *     &lt;xsd:sequence&gt;
 *       &lt;xsd:element ref="sld:Name"/&gt;
 *     &lt;/xsd:sequence&gt;
 *   &lt;/xsd:complexType&gt;
 * &lt;/xsd:element&gt;
 * </code></pre>
 *
 * @author James Macgill
 * @source $URL$
 */
public interface NamedStyle extends Style {
    public String getName();

    public void setName(String name);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @deprecated Not supported
     */
    public String getTitle();

    /**
     * DOCUMENT ME!
     *
     * @param title DOCUMENT ME!
     *
     * @deprecated Not supported
     */
    public void setTitle(String title);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @deprecated Not supported
     */
    public String getAbstract();

    /**
     * DOCUMENT ME!
     *
     * @param abstractStr DOCUMENT ME!
     *
     * @deprecated Not supported
     */
    public void setAbstract(String abstractStr);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @deprecated Not supported
     */
    public boolean isDefault();

    /**
     * DOCUMENT ME!
     *
     * @param isDefault DOCUMENT ME!
     *
     * @deprecated Not supported
     */
    public void setDefault(boolean isDefault);

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @deprecated Not supported
     */
    public FeatureTypeStyle[] getFeatureTypeStyles();

    /**
     * DOCUMENT ME!
     *
     * @param types DOCUMENT ME!
     *
     * @deprecated Not supported
     */
    public void setFeatureTypeStyles(FeatureTypeStyle[] types);

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @deprecated Not supported
     */
    public void addFeatureTypeStyle(FeatureTypeStyle type);

    public void accept(StyleVisitor visitor);
}
