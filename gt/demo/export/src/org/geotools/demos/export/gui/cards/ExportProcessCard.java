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
package org.geotools.demos.export.gui.cards;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.demos.export.ExportParameters;
import org.geotools.feature.FeatureType;


/**
 * The card thar does the export process while showing its progress in a
 * progressbar.
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class ExportProcessCard extends WizzardCard implements FeatureListener {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(ExportProcessCard.class.getPackage()
                                                                                 .getName());

    /** DOCUMENT ME! */
    private static String HELP_MSG = "Please wait while the export process is being done.";

    /** DOCUMENT ME! */
    private ExportParameters params;

    /** DOCUMENT ME! */
    private JLabel statusMessage;

    /** DOCUMENT ME! */
    private JProgressBar progressBar;

    /** The current number of features exported */
    private int currentCount;

    /**
     * DOCUMENT ME!
     *
     * @param listener
     * @param prev
     */
    public ExportProcessCard(CardListener listener, WizzardCard prev) {
        super(listener, prev);
        buildGui();
    }

    /**
     * DOCUMENT ME!
     */
    private void buildGui() {
        JPanel panel = getGui();
        statusMessage = new JLabel();

        statusMessage.setBorder(BorderFactory.createBevelBorder(
                BevelBorder.LOWERED));
        panel.add(statusMessage, BorderLayout.SOUTH);

        progressBar = new JProgressBar();
        panel.add(progressBar);
        progressBar.setMinimum(0);
        progressBar.setStringPainted(true);
    }

    /**
     *
     */
    private void startProcess() {
        if ((params == null) || !params.hasEnoughInfo()) {
            showError("Illegal state",
                "There is not enough information gathered to start the export process");

            return;
        }

        FeatureSource source;

        try {
            source = params.createExportSource();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error building source", e.getMessage());

            return;
        }

        ExportThread thread = new ExportThread(source, params.getDestDataStore());
        thread.start();
    }

    /**
     * Gets called when a Feature is exported.
     * 
     * <p>
     * This method implements FeatureListener.changed
     * </p>
     *
     * @param featureEvent The FeatureEvent being fired
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void changed(FeatureEvent featureEvent) {
        if (featureEvent.getEventType() != FeatureEvent.FEATURES_ADDED) {
            throw new IllegalArgumentException(
                "It is expected to recieve only FEATURE_ADDED events");
        }

        progressBar.setValue(++currentCount);
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
    public void show() {
        getListener().setNextEnabled(false);
        getListener().setPreviousEnabled(false);
        getListener().setFinishEnabled(false);
        startProcess();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean nextPressed() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean previousPressed() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param paramObject DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void setParameter(Object paramObject) {
        if (!(paramObject instanceof ExportParameters)) {
            throw new IllegalArgumentException("Expected ExportParameters: "
                + paramObject);
        }

        this.params = (ExportParameters) paramObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getCardName() {
        return "EXPORT";
    }

    /**
     * DOCUMENT ME!
     *
     * @param message DOCUMENT ME!
     */
    private void setStatus(String message) {
        statusMessage.setText(message);
    }

    /**
     * DOCUMENT ME!
     *
     * @param nFeatures DOCUMENT ME!
     */
    private void setResultCount(int nFeatures) {
        progressBar.setIndeterminate(nFeatures == -1);

        if (nFeatures > -1) {
            progressBar.setMaximum(nFeatures);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author Gabriel Roldan, Axios Engineering
     * @version $Id
     */
    private class ExportThread extends Thread {
        /** DOCUMENT ME! */
        private FeatureSource source;

        /** DOCUMENT ME! */
        private DataStore dest;

        /**
         * Creates a new ExportThread object.
         *
         * @param source DOCUMENT ME!
         * @param dest DOCUMENT ME!
         *
         * @throws NullPointerException DOCUMENT ME!
         */
        public ExportThread(FeatureSource source, DataStore dest) {
            super("Feature export thread");

            if ((source == null) || (dest == null)) {
                throw new NullPointerException();
            }

            this.source = source;
            this.dest = dest;
        }

        /**
         * DOCUMENT ME!
         */
        public void run() {
            FeatureStore store = null;

            try {
                getGui().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                setStatus("Querying source data...");

                FeatureType sourceSchema = source.getSchema();
                setStatus("Creating " + sourceSchema.getTypeName() + " at "
                    + dest);
                dest.createSchema(sourceSchema);

                setStatus("FeatureType created, querying source data...");
                LOGGER.info("getting FeatureResults...");

                FeatureResults results = source.getFeatures();
                LOGGER.info("Querying result count...");

                int resCount = results.getCount();
                LOGGER.info("Result count: " + resCount);
                setResultCount(resCount);

                setStatus("Result count: " + resCount
                    + ". Got results, opening stream...");

                final FeatureReader reader = results.reader();
                LOGGER.info("Result count: " + resCount
                    + ". Got FeatureReader...");

                store = (FeatureStore) dest.getFeatureSource(sourceSchema
                        .getTypeName());
                store.addFeatureListener(ExportProcessCard.this);
                store.addFeatures(reader);
                JOptionPane.showMessageDialog(getGui(), "Export successful!!");
            } catch (Exception e) {
                e.printStackTrace();
                setStatus("Export failed: " + e.getMessage());
                showError("Export failed", e.getMessage());
            } finally {
                if (store != null) {
                    store.removeFeatureListener(ExportProcessCard.this);
                }

                getListener().setPreviousEnabled(true);
                getGui().setCursor(Cursor.getDefaultCursor());
                progressBar.setValue(0);
            }
        }
    }
}
