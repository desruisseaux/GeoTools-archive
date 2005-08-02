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
package org.geotools.demo.export.gui;

import org.geotools.demo.export.gui.cards.CardListener;
import org.geotools.demo.export.gui.cards.DestDataStoreCard;
import org.geotools.demo.export.gui.cards.EditPropertiesCard;
import org.geotools.demo.export.gui.cards.ExportProcessCard;
import org.geotools.demo.export.gui.cards.SelectSourceDataStoreCard;
import org.geotools.demo.export.gui.cards.SourceFeatureTypeCard;
import org.geotools.demo.export.gui.cards.SummaryCard;
import org.geotools.demo.export.gui.cards.WizzardCard;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class ExportWizzard extends JFrame implements CardListener {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(ExportWizzard.class.getPackage()
                                                                             .getName());

    /** DOCUMENT ME! */
    private JButton prevButton;

    /** DOCUMENT ME! */
    private JButton nextButton;

    /** DOCUMENT ME! */
    private JButton finishButton;

    /** DOCUMENT ME! */
    private JButton closeButton;

    /** DOCUMENT ME! */
    private JPanel cardsPanel;

    /** DOCUMENT ME! */
    private JTextArea helpArea;

    /** DOCUMENT ME! */
    private List cardList = new ArrayList();

    /**
     * Creates a new ExportWizzard object.
     */
    public ExportWizzard() {
        super("Geotools data export demo");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    LOGGER.info("closing window");
                    close();
                }
            });

        getContentPane().setLayout(new BorderLayout(2, 5));

        JPanel helpPanel = new JPanel();
        helpPanel.setBackground(Color.white);
        helpPanel.setPreferredSize(new Dimension(100, 55)); //height is what we need constant
        getContentPane().add(helpPanel, BorderLayout.NORTH);
        helpArea = new JTextArea();
        helpArea.setEditable(false);
        helpArea.setWrapStyleWord(true);
        helpPanel.add(helpArea);

        cardsPanel = new JPanel(new CardLayout(2, 2));
        cardsPanel.setBackground(Color.green);

        JPanel buttonsPanel = createButtonsPanel();
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        setUpCards();
        getContentPane().add(cardsPanel, BorderLayout.CENTER);
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 10));
        buttonsPanel.add(prevButton = new JButton("<< Back"));
        buttonsPanel.add(nextButton = new JButton("Next >>"));
        buttonsPanel.add(finishButton = new JButton("Finish"));
        buttonsPanel.add(closeButton = new JButton("Cancel"));

        prevButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    previous();
                }
            });

        nextButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    next();
                }
            });

        finishButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    finish();
                }
            });

        closeButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    close();
                }
            });

        return buttonsPanel;
    }

    /**
     * DOCUMENT ME!
     */
    private void setUpCards() {
        WizzardCard card1 = new SelectSourceDataStoreCard(this);
        WizzardCard card2 = new SourceFeatureTypeCard(this, card1);
        card1.setNextCard(card2);

        WizzardCard card3 = new DestDataStoreCard(this, card2);
        card2.setNextCard(card3);

        WizzardCard card4 = new EditPropertiesCard(this, card3);
        card3.setNextCard(card4);

        WizzardCard card5 = new SummaryCard(this, card4);
        card4.setNextCard(card5);

        WizzardCard card6 = new ExportProcessCard(this, card5);
        card5.setNextCard(card6);
        card6.setNextCard(card1);

        addCard(card1);
        addCard(card2);
        addCard(card3);
        addCard(card4);
        addCard(card5);
        addCard(card6);

        setMessage(card1.getHelpMessage());
        card1.show();
    }

    /**
     * DOCUMENT ME!
     *
     * @param card
     */
    private void addCard(WizzardCard card) {
        cardsPanel.add(card.getGui(), card.getCardName());
        cardList.add(card);
    }

    /**
     * DOCUMENT ME!
     *
     * @param text DOCUMENT ME!
     */
    private void setMessage(String text) {
        helpArea.setText(text);
    }

    /**
     * DOCUMENT ME!
     */
    private void previous() {
        getCurrentCard().previousPressed();

        CardLayout cl = (CardLayout) cardsPanel.getLayout();
        cl.previous(cardsPanel);
        setMessage(getCurrentCard().getHelpMessage());
        getCurrentCard().show();
    }

    /**
     * Called when the "next" button is pressed, shows the nex card.
     * 
     * <p>
     * Before showing the next card, calls the current card's
     * <code>nextPressed()</code> method and if it returns <code>true</code>
     * proceeds to show the next one. If it returns false, nothing happens,
     * since it means the current card is not in a consistent state as to
     * proceed with the next wizzard step.
     * </p>
     */
    private void next() {
        WizzardCard currCard = getCurrentCard();

        if (!currCard.nextPressed()) {
            return;
        }

        WizzardCard nextCard = currCard.getNextCard();
        CardLayout cl = (CardLayout) cardsPanel.getLayout();

        cl.show(cardsPanel, nextCard.getCardName());
        nextCard.show();
        setMessage(nextCard.getHelpMessage());
    }

    /**
     * DOCUMENT ME!
     */
    private void finish() {
        try {
            getCurrentCard().finishPressed();
            next();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void close() {
        //by now just exit
        System.exit(0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param enable DOCUMENT ME!
     */
    public void setNextEnabled(boolean enable) {
        nextButton.setEnabled(enable);
    }

    /**
     * DOCUMENT ME!
     *
     * @param enable DOCUMENT ME!
     */
    public void setPreviousEnabled(boolean enable) {
        prevButton.setEnabled(enable);
    }

    /**
     * DOCUMENT ME!
     *
     * @param enable DOCUMENT ME!
     */
    public void setFinishEnabled(boolean enable) {
        finishButton.setEnabled(enable);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalStateException DOCUMENT ME!
     */
    private WizzardCard getCurrentCard() {
        CardLayout cl = (CardLayout) cardsPanel.getLayout();

        Component[] c = cardsPanel.getComponents();
        WizzardCard card = null;

        for (int i = 0; i < c.length; i++) {
            if (c[i].isVisible()) {
                card = (WizzardCard) cardList.get(i);

                break;
            }
        }

        if (card == null) {
            throw new IllegalStateException("There is no visible card");
        }

        return card;
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        ExportWizzard wizz = new ExportWizzard();
        wizz.pack();
        wizz.setVisible(true);
    }
}
