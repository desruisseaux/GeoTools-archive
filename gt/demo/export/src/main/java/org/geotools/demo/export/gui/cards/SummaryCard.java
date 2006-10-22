/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.demo.export.gui.cards;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.geotools.demo.export.ExportParameters;
import org.geotools.feature.FeatureType;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL$
 * @version $Id$
 */
public class SummaryCard extends WizzardCard {
    /** DOCUMENT ME! */
    private static final String HELP_MSG =
        "This is a summary of the actions that will\n"
        + "be done during the export process. Please verify that all is correct\n"
        + "and press the Finish button to start the process.";

    /** DOCUMENT ME!  */
    private ExportParameters exportParams;

    private JTextPane textPane;
    
	/**
     * DOCUMENT ME!
     *
     * @param listener
     * @param prev
     */
    public SummaryCard(CardListener listener, WizzardCard prev) {
        super(listener, prev);
        buildGui();
    }

    /**
     * DOCUMENT ME!
     */
    public void show() {
        super.show();
        getListener().setPreviousEnabled(true);
        getListener().setNextEnabled(false);
        getListener().setFinishEnabled(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getHelpMessage() {
        return HELP_MSG;
    }

    /**
     * DOCUMENT ME!
     */
    public boolean nextPressed() {
    	return true;
    }

    /**
     * DOCUMENT ME!
     */
    public boolean previousPressed() {
    	return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getCardName() {
        return "SUMMARY";
    }

    /**
     * DOCUMENT ME!
     */
    public boolean finishPressed() {
    	WizzardCard next = getNextCard();
    	next.setParameter(this.exportParams);
    	return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param paramObject DOCUMENT ME!
     */
    public void setParameter(Object paramObject) {
        if (!(paramObject instanceof ExportParameters)) {
            throw new IllegalArgumentException("Expected ExportParameters: "
                + paramObject);
        }

        this.exportParams = (ExportParameters) paramObject;
        fillSummary(textPane);
    }

    /**
     * DOCUMENT ME!
     */
    private void buildGui() {
        JPanel panel = getGui();
        panel.add(new JLabel("Summary of actions to perform:"),
            BorderLayout.NORTH);

        textPane = new JTextPane();
        textPane.setFont(new Font("Arial", Font.PLAIN, 10));
        textPane.setBorder(BorderFactory.createEtchedBorder());
        textPane.setEnabled(false);
        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param textPane DOCUMENT ME!
     */
    private void fillSummary(JTextPane textPane) {
        textPane.setContentType("text/html");
        final ExportParameters params = exportParams;
        StringBuffer sb = new StringBuffer("<html><body style='font-family:arial;font-size:10px'>");
        FeatureType sourceSchema = params.getFeatureSource().getSchema();
        
        sb.append("<b>Feature source</b>: ");
        //we should have a better way of describing a datastore...
        sb.append(params.getFeatureSource().getDataStore().getClass().getName());
        sb.append(sourceSchema.getTypeName()).append("<br>");

        sb.append("<b>Destination DataStore</b>: ");
        //we should have a better way of describing a datastore...
        sb.append(params.getDestDataStore().getClass().getName());
        sb.append("<br>");
        
        sb.append("<b>Modifications</b>:<ul>");
        
        if (exportParams.getNewTypeName() != null) {
            sb.append("<li>Feature type name change from <b>");
            sb.append(sourceSchema.getTypeName());
            sb.append("</b> to <b>");
            sb.append(params.getNewTypeName());
            sb.append("</b>");
        }
        
        if(params.getOverrideCRS() != null){
            sb.append("<li>Force source Coordinate Reference System from <b>");
            sb.append(sourceSchema.getDefaultGeometry().getCoordinateSystem());
            sb.append("</b> to <b>");
            sb.append(params.getOverrideCRS());
            sb.append("</b>");
        }

        if(params.getOverrideCRS() != null){
            sb.append("<li>Reproject source data to the following CRS: <b>");
            sb.append(params.getReprojectCRS()).append("</b>");
        }
        

        sb.append("</body></html");
        textPane.setText(sb.toString());
    }
}
