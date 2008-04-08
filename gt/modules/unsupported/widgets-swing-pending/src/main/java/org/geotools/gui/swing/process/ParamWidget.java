package org.geotools.gui.swing.process;

import javax.swing.JComponent;

/**
 *  Interface for creating parameter widgets.  A ParamWidget handles
 *  creating, validating and maintaining a widget for one process parameter.
 * 
 * @author Graham Davis
 */
public interface ParamWidget {
	
	/**
	 * Called to build the widget, initialize it (setting defaults or
	 * whatever) and setup any listeners needed for validation of the widget value.
	 * The returned JComponent will contain the widget with a label.
	 * 
	 * @return JComponent or null if error
	 */	
	public JComponent doLayout();

	/**
	 * Validates the current value of the widget, returns false if not valid,
	 * true otherwise
	 * 
	 * @return boolean if validated
	 */	
	public boolean validate();
	
	/**
	 * Sets the value of the widget.  Returns true if the value was sucessfully
	 * set to the new value, returns false otherwise.
	 * 
	 * @param Object an object containing the value to set for the widget
	 * @return boolean true if successfully set, false otherwise
	 */	
	public boolean setValue(Object value);
	
	/**
	 * Returns the current value of the widget.  
	 * 
	 * @return Object representing the current value of the widget
	 */	
	public Object getValue();	
}
