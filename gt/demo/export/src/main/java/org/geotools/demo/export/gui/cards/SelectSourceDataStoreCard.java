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

import java.util.logging.Logger;

import org.geotools.data.DataStore;

/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL$
 * @version $Id$
 */
public class SelectSourceDataStoreCard extends AbstractDSSelectionCard {
    private static final Logger LOGGER = Logger.getLogger(SelectSourceDataStoreCard.class.getPackage()
            .getName());
    /** DOCUMENT ME! */
    private static final String HELP_MESSAGE = "Select a DataStore type for the source data";

    /**
     * Creates a new SelectSourceDataStoreCard object.
     *
     * @param listener DOCUMENT ME!
     */
    public SelectSourceDataStoreCard(CardListener listener) {
        super(listener, null);
    }
    
    public String getCardName(){
    	return "DS_SOURCE_CARD";
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getHelpMessage() {
        return HELP_MESSAGE;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object getParameter() {
        return getDataStore();
    }

    /**
     * DOCUMENT ME!
     */
    public void show() {
    	super.show();
    	getListener().setPreviousEnabled(false);
    	getListener().setNextEnabled(isConnected());
    }

    /**
     * DOCUMENT ME!
     *
     * @param nextCard DOCUMENT ME!
     */
    public boolean nextPressed() {
        LOGGER.info("next pressed");
        DataStore ds = getDataStore();
        WizzardCard c = getNextCard();
        c.setParameter(ds);
        return true;
    }

    /**
     * DOCUMENT ME!
     */
    public boolean previousPressed() {
        LOGGER.info("previous pressed");
        return true;
    }
}
