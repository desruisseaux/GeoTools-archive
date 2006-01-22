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
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.demo.export.ExportParameters;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL$
 * @version $Id$
 */
public class DestDataStoreCard extends AbstractDSSelectionCard {
    /** DOCUMENT ME!  */
    private static final String HELP_MSG = "Select a destination DataStore for the exported data";

    /** DOCUMENT ME!  */
    private FeatureSource fSource;
    
    private JLabel sourceLabel = new JLabel();

    /**
     * Creates a new DestDataStoreCard object.
     *
     * @param listener DOCUMENT ME!
     * @param previous DOCUMENT ME!
     */
    public DestDataStoreCard(CardListener listener, WizzardCard previous) {
        super(listener, previous);
        JPanel sourcePanel = new JPanel(new FlowLayout());
        sourcePanel.add(new JLabel("Feature source: "));
        sourcePanel.add(sourceLabel);
        getGui().add(sourcePanel, BorderLayout.NORTH);
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
    	WizzardCard next = getNextCard();
    	DataStore ds = getDataStore();
        ExportParameters params = new ExportParameters();
        params.setFeatureSource(this.fSource);
        params.setDestDataStore(ds);
    	next.setParameter(params);
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
        return "DEST_DS";
    }

    /**
     * DOCUMENT ME!
     *
     * @param paramObject DOCUMENT ME!
     */
    public void setParameter(Object paramObject) {
        if (!(paramObject instanceof FeatureSource)) {
            throw new IllegalArgumentException("Expected FeatureSource: "
                + paramObject);
        }

        this.fSource = (FeatureSource) paramObject;
        sourceLabel.setText(fSource.getDataStore().toString() + "/" + fSource.getSchema().getTypeName());
    }
    
    public void show() {
    	super.show();
    	getListener().setPreviousEnabled(false);
    	getListener().setNextEnabled(isConnected());
    }    
}
