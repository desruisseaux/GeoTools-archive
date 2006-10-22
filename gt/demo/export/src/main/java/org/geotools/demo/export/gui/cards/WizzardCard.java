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
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL$
 * @version $Id$
 */
public abstract class WizzardCard {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(WizzardCard.class.getPackage()
                                                                           .getName());

    /** DOCUMENT ME! */
    private CardListener listener;

    /** DOCUMENT ME! */
    private WizzardCard previousCard;

    /** DOCUMENT ME! */
    private WizzardCard nextCard;

    /** DOCUMENT ME! */
    private JPanel cardGui;

    /**
     * Creates a new WizzardCard object.
     *
     * @param listener DOCUMENT ME!
     * @param prev DOCUMENT ME!
     *
     * @throws NullPointerException DOCUMENT ME!
     */
    public WizzardCard(CardListener listener, WizzardCard prev) {
        cardGui = new JPanel(new BorderLayout(4, 4));

        if (listener == null) {
            throw new NullPointerException();
        }

        this.listener = listener;
        this.previousCard = prev;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JPanel getGui() {
        return cardGui;
    }

    /**
     * DOCUMENT ME!
     *
     * @param next DOCUMENT ME!
     */
    public void setNextCard(WizzardCard next) {
        this.nextCard = next;
    }

    /**
     * DOCUMENT ME!
     */
    public void show() {
        LOGGER.fine("disabling next and finish actions by default");
        listener.setFinishEnabled(false);
        listener.setNextEnabled(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param paramObject DOCUMENT ME!
     */
    public void setParameter(Object paramObject) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract String getHelpMessage();

    /**
     * DOCUMENT ME!
     *
     * @return true if the card state is consistent and it is ok to proceed
     *         with the next card.
     */
    public abstract boolean nextPressed();

    /**
     * DOCUMENT ME!
     *
     * @return true if the card state is consistent and it is ok to go back to
     *         the previous card.
     */
    public abstract boolean previousPressed();

    /**
     * DOCUMENT ME!
     *
     * @return true if the card state is consistent and it is ok to proceed
     *         with the final process.
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public boolean finishPressed() {
        throw new UnsupportedOperationException(
            "you must override this method if the card supports it");
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public abstract String getCardName();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public WizzardCard getNextCard() {
        return nextCard;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public WizzardCard getPreviousCard() {
        return previousCard;
    }

    /**
     * DOCUMENT ME!
     *
     * @param title DOCUMENT ME!
     * @param message DOCUMENT ME!
     */
    protected void showError(String title, String message) {
        JOptionPane.showMessageDialog(getGui(), message, title,
            JOptionPane.ERROR_MESSAGE);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected CardListener getListener() {
        return listener;
    }
}
