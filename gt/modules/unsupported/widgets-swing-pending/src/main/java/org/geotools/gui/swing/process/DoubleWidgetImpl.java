package org.geotools.gui.swing.process;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.geotools.text.Text;

/**
 *  Widget for double values
 * 
 * @author gdavis
 */
public class DoubleWidgetImpl extends AbstractParamWidget {

    private JTextField blah;
    
	public DoubleWidgetImpl(Text defaultVal) {
		this.defaultValue = defaultVal;
	}
	
	public JComponent doLayout() {
		// TODO create the label and widget, layout them out and return the main
		// JComponent holding them.
	    JTextField blah = new JTextField(100); 
	    
		return blah;
	}

	public Object getValue() {
	    String val = blah.getText();
		return new Double(val);
	}

	public void setValue(Object value) {
	    blah.setText(((Double)value).toString());
	}

	public boolean validate() {
		String val = blah.getText();
		try {
		    Double d = Double.parseDouble(val);
		}
		catch (NumberFormatException e) {
		    return false;
		}
		return true;
	}

}
