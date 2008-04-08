package org.geotools.gui.swing.process;

import javax.swing.JComponent;
import org.geotools.text.Text;

/**
 * Super class that provides additional helper methods
 * useful when implementing your own ParamWidget.

 * @author gdavis
 */
public abstract class AbstractParamWidget implements ParamWidget {

	protected static Text labelText;
	protected static Object defaultValue;
	protected static JComponent mainComponent;
	
}
