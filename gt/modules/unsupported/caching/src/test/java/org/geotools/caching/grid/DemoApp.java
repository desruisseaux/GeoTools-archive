/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.caching.grid;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.caching.FeatureCacheException;
import org.geotools.caching.grid.GridFeatureCache;
import org.geotools.caching.spatialindex.store.BufferedDiskStorage;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.FeatureCollection;


public class DemoApp extends JFrame {
    MemoryDataStore ds;
    GridFeatureCache cache;
    JPanel jContentPane = null;
    JPanel statsPanel = null;
    JPanel graphPanel = null;
    CacheDisplayPanel panel = null;
    JButton runQueryButton = null;
    JLabel lblNumData = null;
    JLabel lblNumReads = null;
    JLabel lblNumWrites = null;
    JLabel lblNumEvictions = null;

    DemoApp() {
        initDataStore();
        initDataCache();
        panel = new CacheDisplayPanel(cache);
        this.setContentPane(getJContentPane());
    }

    void initDataStore() {
        ds = new MemoryDataStore();

        FeatureCollection fc = DataUtilities.createUnitsquareDataSet(500);
        ds.addFeatures(fc);
    }

    void initDataCache() {
        try {
            cache = new GridFeatureCache(ds.getFeatureSource(ds.getTypeNames()[0]), 100, 100,
                    BufferedDiskStorage.createInstance());
        } catch (FeatureCacheException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();

            GridBagLayout bag = new GridBagLayout();
            jContentPane.setLayout(bag);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            bag.setConstraints(getStatsPanel(), c);
            jContentPane.add(getStatsPanel());
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            bag.setConstraints(panel, c);
            jContentPane.add(panel, c);
            c.weightx = 0;
            c.weighty = 0;
            bag.setConstraints(getGraphPanel(), c);
            jContentPane.add(getGraphPanel(), c);
        }

        return jContentPane;
    }

    JPanel getStatsPanel() {
        if (statsPanel == null) {
            statsPanel = new JPanel();
            statsPanel.setLayout(new GridLayout(5, 2, 1, 1));
            statsPanel.setSize(100, 400);
            statsPanel.add(new JLabel("Stats"));
            statsPanel.add(new JLabel("Panel"));
            statsPanel.add(new JLabel("Data = "));
            lblNumData = new JLabel("0");
            statsPanel.add(lblNumData);
            statsPanel.add(new JLabel("Reads = "));
            lblNumReads = new JLabel("0");
            statsPanel.add(lblNumReads);
            statsPanel.add(new JLabel("Writes = "));
            lblNumWrites = new JLabel("0");
            statsPanel.add(lblNumWrites);
            lblNumEvictions = new JLabel("0");
            statsPanel.add(new JLabel("Evictions = "));
            statsPanel.add(lblNumEvictions);
        }

        return statsPanel;
    }

    JButton getRunQueryButton() {
        if (runQueryButton == null) {
            runQueryButton = new JButton("Run queries");
            runQueryButton.addMouseListener(new MouseListener() {
                    public void mouseClicked(MouseEvent ev) {
                        Runnable task = new Runnable() {
                                public void run() {
                                    runQueries();
                                }
                            };

                        new Thread(task, "RunQueriesThread").start();
                    }

                    public void mouseEntered(MouseEvent arg0) {
                        // TODO Auto-generated method stub
                    }

                    public void mouseExited(MouseEvent arg0) {
                        // TODO Auto-generated method stub
                    }

                    public void mousePressed(MouseEvent arg0) {
                        // TODO Auto-generated method stub
                    }

                    public void mouseReleased(MouseEvent arg0) {
                        // TODO Auto-generated method stub
                    }
                });
        }

        return runQueryButton;
    }

    JPanel getGraphPanel() {
        if (graphPanel == null) {
            graphPanel = new JPanel();
            graphPanel.setSize(300, 100);
            graphPanel.add(new JLabel("GraphPanel"));
            graphPanel.add(getRunQueryButton());
        }

        return graphPanel;
    }

    void runQueries() {
        runQueryButton.setEnabled(false);

        //		cache.clear();
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 10; i++) {
                try {
                    //System.out.print("i = " + i + ", j = " + j);
                    Envelope query = new Envelope(i * .1, (i + 1) * .1, j * .1, (j + 1) * .1);
                    cache.get(query);
                    lblNumData.setText(new Long(cache.tracker.getStatistics().getNumberOfData())
                        .toString());
                    lblNumReads.setText(new Long(cache.tracker.getStatistics().getReads()).toString());
                    lblNumWrites.setText(new Long(cache.tracker.getStatistics().getWrites())
                        .toString());
                    lblNumEvictions.setText(new Integer(cache.tracker.getEvictions()).toString());
                    panel.setCurrentQuery(query);
                    panel.repaint();

                    Object waiter = new Object();

                    synchronized (waiter) {
                        try {
                            waiter.wait(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        runQueryButton.setEnabled(true);
    }

    public static void main(String[] args) {
        DemoApp thisClass = new DemoApp();
        thisClass.setSize(600, 400);
        thisClass.setTitle("Google SoC : Feature Cache Demo Application");
        thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        thisClass.setVisible(true);

        //thisClass.runQueries();
    }
}
