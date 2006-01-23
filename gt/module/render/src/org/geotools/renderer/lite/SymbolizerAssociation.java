/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2002, Geotools Project Managment
 * Committee (PMC) This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * version 2.1 of the License. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * @source $URL$
 */
package org.geotools.renderer.lite;

import org.geotools.styling.Symbolizer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform2D;

public class SymbolizerAssociation
{
     public MathTransform2D  xform = null;
     public CoordinateReferenceSystem crs = null;
     
     public SymbolizerAssociation()
     {

     }
     
     public MathTransform2D getXform()
     {
     	return xform;
     }
     
     public void setXform(MathTransform2D xform)
     {
     	this.xform = xform;
     }
     
     public CoordinateReferenceSystem getCRS()
     {
     	return crs;
     }
     
     public void setCRS(CoordinateReferenceSystem crs)
     {
     	this.crs = crs;
     }
     
}
