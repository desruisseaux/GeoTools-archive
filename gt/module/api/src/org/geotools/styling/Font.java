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

import org.geotools.event.GTComponent;
import org.geotools.filter.Expression;


/**
 * A system-independent object for holding SLD font information. This holds
 * information on the text font to use in text processing. Font-family,
 * font-style, font-weight and font-size. $Id: Font.java,v 1.3 2002/10/16
 * 16:56:47 ianturton Exp $
 *
 * @author Ian Turton, CCG
 * @source $URL$
 */
public interface Font extends GTComponent {
	
	/** default font-size value **/
	static final int DEFAULT_FONTSIZE = 10;
	
	/**
	 * Enumeration of allow font-style values.
	 */
	interface Style {
		static final String NORMAL = "normal";
		static final String ITALIC = "italic";
		static final String OBLIQUE = "oblique";
	}
	
	/**
	 * Enumeration of allow font-weight values.
	 */
	interface Weight {
		static final String NORMAL = "normal";
		static final String BOLD = "bold";
	}
	
    Expression getFontFamily();

    void setFontFamily(Expression family);

    Expression getFontStyle();

    void setFontStyle(Expression style);

    Expression getFontWeight();

    void setFontWeight(Expression weight);

    Expression getFontSize();

    void setFontSize(Expression size);
}
