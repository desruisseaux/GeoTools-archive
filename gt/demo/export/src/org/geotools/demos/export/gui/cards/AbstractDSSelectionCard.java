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

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public abstract class AbstractDSSelectionCard extends WizzardCard {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger(AbstractDSSelectionCard.class.getPackage()
                                                                                       .getName());

    /** holds the available datastore factories after the first call to */
    private static List dataStoreFactories;

    /** a JList able to show a list of DataStoreFactories */
    protected DataStoreList list;

    /** DOCUMENT ME! */
    protected DSParamsPanel paramsPanel;

    /** non null if it the user has connected to a datastore */
    private DataStore connectedDataStore;

    /** DOCUMENT ME! */
    private JButton connectButton;

    /** DOCUMENT ME! */
    private JButton disconnectButton;

    /**
     * Creates a new DataStoreView object.
     *
     * @param listener DOCUMENT ME!
     * @param prev DOCUMENT ME!
     */
    public AbstractDSSelectionCard(CardListener listener, WizzardCard prev) {
        super(listener, prev);

        //creates list and params objects, and adds
        //proper event listeners
        buildGui();

        JPanel connButtonsPanel = createConnectionButtons();
        JScrollPane listPane = new JScrollPane(list);
        listPane.setPreferredSize(new Dimension(240, 140));

        getGui().add(listPane, BorderLayout.WEST);

        getGui().add(paramsPanel, BorderLayout.CENTER);
        getGui().add(connButtonsPanel, BorderLayout.SOUTH);
        setGuiEnabled(true);
        init();
    }

    public void show(){
    	super.show();
    }
    /**
     * Builds the gui for the list of available datastores and their editable
     * parameters.
     * 
     * <p>
     * Also sets up event listeners for changing the parameters list when a
     * datastore is selected from the list, and to release the current
     * "connected" datastore instance when the selected datastore changes.
     * </p>
     */
    private void buildGui() {
        list = new DataStoreList();
        paramsPanel = new DSParamsPanel();

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        list.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    disconnect();

                    DataStoreFactorySpi dsf = (DataStoreFactorySpi) list
                        .getSelectedValue();
                    paramsPanel.setDataStore(dsf);

                    LOGGER.fine("Selected datastore: " + dsf);
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    protected void init() {
        Iterator dsIt = getDSFactories();
        DataStoreFactorySpi dsf = null;

        for (; dsIt.hasNext(); ) {
			dsf = (DataStoreFactorySpi) dsIt.next();
            System.out.println("Found datastore " + dsf);

            if (dsf != null) {
                list.addDatastore(dsf);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static Iterator getDSFactories() {
        if (dataStoreFactories == null) {
            Iterator dsfIt = DataStoreFinder.getAvailableDataStores();
            dataStoreFactories = new LinkedList();

            while (dsfIt.hasNext())
                dataStoreFactories.add(dsfIt.next());
        }

        return dataStoreFactories.iterator();
    }

    /**
     * Creates the connect/disconnect buttons and attach mouse listeners to
     * call <code>connect()</code> and <code>disconnect()</code> methods
     *
     * @return the panel containing both buttons
     */
    private JPanel createConnectionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        connectButton = new JButton("Connect...");
        disconnectButton = new JButton("Disconnect...");
        panel.add(connectButton);
        panel.add(disconnectButton);
        connectButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    connect();
                }
            });
        disconnectButton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    disconnect();
                }
            });

        return panel;
    }

    /**
     * Called when the "connect" button is pressed.
     * 
     * <p>
     * If the connection is successful, then stores the connected datastore
     * instance in the <code>connectedDataStore</code> instance variable.
     * </p>
     */
    protected void connect() {
        setGuiEnabled(false);

        Object selected = list.getSelectedValue();
        final DataStoreFactorySpi dsf;
        final Map dsParams;

        if (selected != null) {
            dsf = (DataStoreFactorySpi) selected;
        } else {
            JOptionPane.showMessageDialog(getGui(),
                "Select a DataStore from the list");
            setGuiEnabled(true);

            return;
        }

        try {
            dsParams = paramsPanel.getDataStoreParameters();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(getGui(), "Error: " + e.getMessage());
            setGuiEnabled(true);

            return;
        }

        if (!dsf.canProcess(dsParams)) {
            JOptionPane.showMessageDialog(getGui(),
                "ERROR: this datastore can't process the given parameters");
            setGuiEnabled(true);
        }

        //we do the connection on another thread to not block the
        //AWT-Event thread if connecting to the datastore takes a while.
        Thread connectThread = new Thread() {
                public void run() {
                    try {
                    	getGui().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        final DataStore ds = dsf.createDataStore(dsParams);
                        LOGGER.info("Setting current datastore as " + ds);
                        connectedDataStore = ds;

                        getListener().setNextEnabled(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(getGui(),
                            "Connection error: " + e.getMessage());
                        setGuiEnabled(true);
                    }finally{
                    	getGui().setCursor(Cursor.getDefaultCursor());
                    }
                }
            };

        connectThread.start();
    }

    /**
     * Called when the "disconnect" button is pressed.
     */
    protected void disconnect() {
        setGuiEnabled(true);

        if (connectedDataStore != null) {
            LOGGER.info("Removing previously connected datastore: "
                + connectedDataStore);
            connectedDataStore = null;
        }

        getListener().setNextEnabled(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param enable DOCUMENT ME!
     */
    private void setGuiEnabled(boolean enable) {
        list.setEnabled(enable);
        paramsPanel.setEnabled(enable);
        connectButton.setEnabled(enable);
        disconnectButton.setEnabled(!enable);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isConnected() {
        return connectedDataStore != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalStateException DOCUMENT ME!
     */
    public DataStore getDataStore() throws IllegalStateException {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }

        return connectedDataStore;
    }

    /**
     * DOCUMENT ME!
     *
     * @author Gabriel Roldan, Axios Engineering
     * @version $Id
     */
    private static class DataStoreList extends JList {
        /**
         * Creates a new DataStoreList object.
         */
        public DataStoreList() {
            super(new DefaultListModel());
            setCellRenderer(new DataStoreCellRenderer());
        }

        /**
         * DOCUMENT ME!
         *
         * @param dsf DOCUMENT ME!
         */
        public void addDatastore(DataStoreFactorySpi dsf) {
            ((DefaultListModel) getModel()).addElement(dsf);
            System.out.println("Adding datastore: " + dsf.getDisplayName());
        }

        /**
         * DOCUMENT ME!
         *
         * @author Gabriel Roldan, Axios Engineering
         * @version $Id
         */
        private static class DataStoreCellRenderer
            extends DefaultListCellRenderer {
            /**
             * DOCUMENT ME!
             *
             * @param list DOCUMENT ME!
             * @param value DOCUMENT ME!
             * @param index DOCUMENT ME!
             * @param isSelected DOCUMENT ME!
             * @param cellHasFocus DOCUMENT ME!
             *
             * @return DOCUMENT ME!
             */
            public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
                String ttip = null;

                if (value != null) {
                    DataStoreFactorySpi ds = (DataStoreFactorySpi) value;
                    value = ds.getDisplayName();
                    ttip = ds.getDescription();
                }

                Component c = super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);

                if (ttip != null) {
                    ((JComponent) c).setToolTipText(ttip);
                }

                return c;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @author Gabriel Roldan, Axios Engineering
     * @version $Id
     */
    private static class DSParamsPanel extends JPanel {

        /** DOCUMENT ME! */
        private JTable table;
		private ParamsTableModel model;

        /**
         * Creates a new DSParamsPanel object.
         */
        public DSParamsPanel() {
            super(new BorderLayout());
			model = new ParamsTableModel();
            table = new JTable(model);

            JScrollPane pane = new JScrollPane(table);
            pane.setPreferredSize(new Dimension(400, 140));
            add(pane, BorderLayout.CENTER);
        }

        /**
         * enables/disables both this panel and the contained JTable
         *
         * @param enable DOCUMENT ME!
         */
        public void setEnabled(boolean enable) {
            super.setEnabled(enable);
            table.setEnabled(enable);
        }

        /**
         * DOCUMENT ME!
         *
         * @param dsf DOCUMENT ME!
         */
        public void setDataStore(DataStoreFactorySpi dsf) {
            model.params = dsf.getParametersInfo();
            model.values = new String[model.params.length];

            for (int i = 0; i < model.params.length; i++) {
                if (model.params[i].sample != null) {
                    model.values[i] = model.params[i].text(model.params[i].sample);
                }
            }

            ((DefaultTableModel) table.getModel()).fireTableDataChanged();
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws Exception DOCUMENT ME!
         */
        public Map getDataStoreParameters() throws Exception {
            Map paramsMap = new HashMap();

            for (int i = 0; i < model.params.length; i++) {
                String key = model.params[i].key;
                Object value;

                if (model.values[i] != null) {
                    try {
                        value = model.params[i].parse(model.values[i]);
                        paramsMap.put(key, value);
                    } catch (Throwable e) {
                        throw new Exception(e.getMessage(), e);
                    }
                }
            }

            return paramsMap;
        }

        /**
         * Uses <code>params/values</code> as data model.
         */
        private static class ParamsTableModel extends DefaultTableModel {
	        /** DOCUMENT ME! */
	        DataStoreFactorySpi.Param []params;

	        /** DOCUMENT ME! */
	        private String[] values;

			/**
             * Creates a new ParamsTableModel object.
             */
            public ParamsTableModel() {
				super();
				LOGGER.fine("new ParamsTableModel");
            }

            /**
             * DOCUMENT ME!
             *
             * @return DOCUMENT ME!
             */
            public int getColumnCount() {
                return 2;
            }

            /**
             * DOCUMENT ME!
             *
             * @param column DOCUMENT ME!
             *
             * @return DOCUMENT ME!
             */
            public String getColumnName(int column) {
                if (column == 0) {
                    return "Parameter Name";
                } else {
                    return "Value";
                }
            }

            /**
             * DOCUMENT ME!
             *
             * @return DOCUMENT ME!
             */
            public int getRowCount() {
				System.out.println("params?");
				System.out.println(params);
                return (params == null) ? 0 : params.length;
            }

            /**
             * DOCUMENT ME!
             *
             * @param row DOCUMENT ME!
             * @param column DOCUMENT ME!
             *
             * @return DOCUMENT ME!
             */
            public Object getValueAt(int row, int column) {
                if (column == 0) {
                    return params[row].key;
                } else {
                    return (values[row] == null) ? "" : values[row];
                }
            }

            /**
             * DOCUMENT ME!
             *
             * @param row DOCUMENT ME!
             * @param column DOCUMENT ME!
             *
             * @return DOCUMENT ME!
             */
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }

            /**
             * DOCUMENT ME!
             *
             * @param value DOCUMENT ME!
             * @param row DOCUMENT ME!
             * @param column DOCUMENT ME!
             *
             * @throws IllegalArgumentException DOCUMENT ME!
             */
            public void setValueAt(Object value, int row, int column) {
                if (column != 1) {
                    throw new IllegalArgumentException(
                        "Just column 1 should be editable");
                }

                values[row] = String.valueOf(value);
                fireTableCellUpdated(row, column);
            }
        }
    }
}
