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
import org.geotools.event.GTConstant;
import org.geotools.filter.ConstantExpression;
import org.geotools.filter.Expression;


/**
 * A Displacement gives X and Y offset displacements to use for rendering a
 * text label near a point. $Id: Displacement.java,v 1.4 2003/08/01 16:54:12
 * ianturton Exp $
 *
 * @author Ian Turton, CCG
 * @source $URL$
 */
public interface Displacement extends GTComponent {
     
	/**
	 * Default Displacment instance. 
	 */
    static final Displacement DEFAULT = new ConstantDisplacement() {

		public Expression getDisplacementX() {
			return ConstantExpression.ZERO;
		}

		public Expression getDisplacementY() {
			return ConstantExpression.ZERO;
		}
    	
    };
    
    /**
     * Null Displacement instance.
     */
    static final Displacement NULL = new ConstantDisplacement() {
    
    	public Expression getDisplacementX() {
    		return ConstantExpression.NULL;
    	}
    	
    	public Expression getDisplacementY() {
    		return ConstantExpression.NULL;
    	}
    };
    
	//TODO: add Displacement to GeoAPI
    /**
     * Returns an expression that computes a pixel offset from the geometry
     * point.  This offset point is where the text's anchor point gets
     * located. If this expression is null, the default offset of zero is
     * used.
     *
     * @return DOCUMENT ME!
     */
    Expression getDisplacementX();

    /**
     * Sets the expression that computes a pixel offset from the geometry
     * point.
     *
     * @param x DOCUMENT ME!
     */
    void setDisplacementX(Expression x);

    /**
     * Returns an expression that computes a pixel offset from the geometry
     * point.  This offset point is where the text's anchor point gets
     * located. If this expression is null, the default offset of zero is
     * used.
     *
     * @return DOCUMENT ME!
     */
    Expression getDisplacementY();

    /**
     * Sets the expression that computes a pixel offset from the geometry
     * point.
     *
     * @param y DOCUMENT ME!
     */
    void setDisplacementY(Expression y);

    void accept(StyleVisitor visitor);
}

abstract class ConstantDisplacement extends GTConstant implements Displacement {

	private void cannotModifyConstant(){
		throw new UnsupportedOperationException("Constant Displacement may not be modified");
	}
	
	public void setDisplacementX(Expression x) {
		cannotModifyConstant();
	}

	public void setDisplacementY(Expression y) {
		cannotModifyConstant();
	}

	public void accept(StyleVisitor visitor) {
		cannotModifyConstant();
	}
};
