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
