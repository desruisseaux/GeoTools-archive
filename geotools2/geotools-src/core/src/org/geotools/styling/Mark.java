/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Center for Computational Geography
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

import org.geotools.filter.Expression;
/**
 * A Mark element defines a "shape" which has coloring applied to it.
 *
 * The details of this object are taken from the OGC Styled-Layer
 * Descriptor Report (OGC 01-077) version 0.7.2.
 * Renderers can use this information when displaying styled features, 
 * though it must be remembered that not all renderers will be able to
 * fully represent strokes as set out by this interface.  For example, opacity
 * may not be supported.
 *
 * @version $Id: Mark.java,v 1.9 2002/07/12 15:32:18 loxnard Exp $
 * @author James Macgill
 */
public interface Mark {

    /**
     * This parameter gives the well-known name of the shape of the mark.<br>
     * Allowed names include at least "square", "circle", "triangle", "star",
     * "cross" and "x" though renderers may draw a different symbol instead
     * if they don't have a shape for all of these.<br>
     * 
     * @return The well-known name of a shape.  The default value is "square".
     */
    Expression getWellKnownName();
    
    /**
     * This paramterer defines which stroke style should be used when
     * rendering the Mark.
     * 
     * @return The Stroke definition to use when rendering the Mark.
     **/
    Stroke getStroke();
    
    /**
     * This parameter defines which fill style to use when rendering the Mark.
     *
     * @return the Fill definition to use when rendering the Mark.
     **/
    Fill getFill();
    
    
    
    Expression getSize();
    Expression getRotation();
}

