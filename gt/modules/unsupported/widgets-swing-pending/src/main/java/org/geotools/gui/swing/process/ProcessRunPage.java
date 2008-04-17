package org.geotools.gui.swing.process;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.geotools.gui.swing.ProgressWindow;
import org.geotools.process.Parameter;
import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.process.ProcessFactoryFinder;
import org.geotools.process.literal.IntersectionFactory;
import org.geotools.text.Text;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Geometry;


/**
 * This page is responsible for actually executing the process with the given
 * parameters and then displaying the result.
 * 
 * @author gdavis
 */
public class ProcessRunPage extends JPage {
    ProcessFactory factory;
    Map<String, Object> paramMap;

	public ProcessRunPage(ProcessFactory factory) {
        this(factory, null);
    }
    public ProcessRunPage( ProcessFactory factory, Map<String, Object> params ) {
        super("Run Process");
        this.factory = factory;
        this.paramMap = params;
    }
    
    public String getBackPageIdentifier() {   	
    	if (this.factory == null) {
        	ProcessSelectionPage selectionPage = new ProcessSelectionPage();    	
        	this.getJProcessWizard().registerWizardPanel( selectionPage );
            selectionPage.setJProcessWizard(this.getJProcessWizard());
            return selectionPage.getIdentifier();
    	}
        ProcessParameterPage inputPage = new ProcessParameterPage(this.factory);
        this.getJProcessWizard().registerWizardPanel( inputPage );  
        inputPage.setJProcessWizard(this.getJProcessWizard());
        return inputPage.getIdentifier();
    }
    public String getNextPageIdentifier() {
    	return FINISH;
    }
 
	public void aboutToDisplayPanel() {
		
		page.removeAll();
		page.setLayout(new GridLayout(0, 2));
		
		Process process = this.factory.create();
		final ProgressListener progress = new ProgressWindow(this.getJProcessWizard());
		Map<String, Object> resultMap = process.process( paramMap, progress );

		// when we get here, the processing is over so show the result
        JLabel title = new JLabel(factory.getTitle().toString());
        page.add(title);
        JLabel description = new JLabel("Your process results are below:");
        page.add(description);
        for( Entry<String, Object> entry : resultMap.entrySet() ) {
        	Parameter< ? > parameter = new Parameter(entry.getKey(), entry.getValue().getClass(), Text.text("Result") );
            JLabel label = new JLabel(entry.getKey());
            page.add(label);

            ParamWidget widget;
            if (Double.class.isAssignableFrom( parameter.type )) {
                widget = new JDoubleField(parameter);
            } else if (Geometry.class.isAssignableFrom( parameter.type )) {
                widget = new JGeometryField(parameter);
            }
            else {
                // We got nothing special, let's hope the converter api can deal
                widget = new JField( parameter );
            }
            JComponent field = widget.doLayout();
            widget.setValue(entry.getValue());            
            page.add(field);

        }
    }
}
