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

package org.geotools.feature;

// J2SE dependencies
import java.util.*;
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.data.*;

/** 
 * <p>A GeoTools representation of a simple feature.
 * A flat feature type enforces the following properties:<ul>
 * <li>Attribute types are restricted to Java primitives and Strings. 
 * <li>Attributes may only have one occurrence.
 * <li>Each feature has a single, non-changing geometry attribute.</ul></p>
 *
 * <p>Flat feature types define features types that may be thought of as
 * 'layers' in traditional GIS parlance.  They are called flat because they
 * do not allow any nested elements, but they also restrict the attribute
 * objects to be very simple data types.</p>
 *
 * @version $Id: FeatureTypeFactory.java,v 1.4 2002/08/06 22:27:15 desruisseaux Exp $
 * @author Rob Hranac, VFNY
 */
public class FeatureTypeFactory {

    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    /**
     * Constructor.
     */
    private FeatureTypeFactory () {
    }


    /* ***********************************************************************
     * Handles all attribute interface implementation.                       *
     * ***********************************************************************/
    /**
     * Always true.
     *
     * @return Whether or not this represents a feature type (over a
     * 'flat' attribute).
     */
    public static FeatureType create(AttributeType[] attributeTypes)
        throws SchemaException {

        LOGGER.entering("FeatureTypeFactory", "create");

        return new FeatureTypeFlat(attributeTypes);
    }


}
