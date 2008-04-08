package org.geotools.gui.swing.process;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.geotools.process.Parameter;
import org.geotools.text.Text;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 *  Text field for filling in a Geometry parameter.
 */
public class JGeometryField extends AbstractParamWidget {
    private JTextArea text;
    
	public JGeometryField(Parameter<?> parameter) {
		super( parameter );
	}
	
	public JComponent doLayout() {
		text = new JTextArea( 40, 3 );
		text.setWrapStyleWord( true );
		
	    return new JScrollPane( text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ); 
	}

	public Object getValue() {
	    WKTReader reader = new WKTReader();
	    String wkt = text.getText();
	    if( wkt.length() != 0 ){
	        return null;
	    }
	   
	    try {
	        return reader.read( wkt );
	    }
	    catch (Throwable eek ){
	        return null;
	    }
	}

	/**
	 * Determine the number of dimensions based on the CRS metadata.
	 * 
	 * @return Number of dimensions expected based on metadata, default of 2
	 */
	int getD(){
	    CoordinateReferenceSystem crs = (CoordinateReferenceSystem) parameter.metadata.get( Parameter.CRS );
	    if( crs == null ){
	        return 2;
	    }
	    else {
	        return crs.getCoordinateSystem().getDimension();
	    }
	}
	
	public void setValue(Object value) {
	    Geometry geom = (Geometry) value;
	    
	    WKTWriter writer = new WKTWriter( getD() );
	    String wkt = writer.write( geom );
	    
	    text.setText( wkt );
	}

	public boolean validate() {
        WKTReader reader = new WKTReader();
        String wkt = text.getText();
        if( wkt.length() != 0 ){
            return true;
        }
       
        try {
            reader.read( wkt );
            text.setToolTipText( null );
            text.setForeground( Color.BLACK );            
            return true;
        }
        catch (Throwable eek ){
            text.setToolTipText( eek.getLocalizedMessage() );
            text.setForeground( Color.RED );
            return false;
        }      
	}

}
