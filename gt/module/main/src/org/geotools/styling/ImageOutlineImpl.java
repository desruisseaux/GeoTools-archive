/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;
import org.geotools.resources.Utilities;

public class ImageOutlineImpl extends AbstractGTComponent implements
		ImageOutline {

	Symbolizer symbolizer;
	
	public Symbolizer getSymbolizer() {
		return symbolizer;
	}

	public void setSymbolizer(Symbolizer symbolizer) {
		if (
			symbolizer instanceof LineSymbolizer ||
			symbolizer instanceof PolygonSymbolizer
		) {
			Symbolizer old = this.symbolizer;
			this.symbolizer = symbolizer;
			
			fireChildChanged("symbolizer",this.symbolizer,old);
		}
		else {
			throw new IllegalArgumentException("Symbolizer must be Line or Polygon.");
		}
	}
	
	public boolean equals(Object obj) {
		 if (this == obj) {
            return true;
        }
	        
        if (obj instanceof ImageOutlineImpl) {
        	ImageOutlineImpl other = (ImageOutlineImpl) obj;
            return Utilities.equals(symbolizer, other.symbolizer);
        }
        
        return false;
	}
	
	public int hashCode() {
		final int PRIME = 1000003;
	    int result = 0;
	    
	    if (symbolizer != null) {
	    	result = PRIME * result + symbolizer.hashCode();
	    }
        
        return result;
	}

}
