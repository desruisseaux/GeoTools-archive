/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gui.swing.table;

// J2SE dependencies
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;

// Geotools dependencies
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;


/**
 * An implementation of Swing's table model which allows feature tables to be displayed.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author James Macgill, CCG
 *
 * @todo It would be excellent if there were custom cell renderers available for Geometry types.
 */
public class FeatureTableModel extends AbstractTableModel implements TableModel {
    /**
     * Holds the feature table that will be represented by this model.
     */
    private FeatureCollection featureTable;

    /**
     * {@link #featureTable} as an array. Will be created only when first needed.
     */
    private transient Feature[] featureArray;

    /**
     * Creates a new instance of feature table model.
     */
    public FeatureTableModel() {
    }

    /**
     * Creates a new instance of FeatureTableModel based on the feature collection provided.
     */
    public FeatureTableModel(final FeatureCollection features) {
        setFeatureCollection(features);
    }

    /**
     * Sets which featureTable to represent
     *
     * @param features The featureTable to represent. This could fire
     *        a Table Structure Changed event.
     */
    public void setFeatureCollection(final FeatureCollection features) {
        featureArray = null;
        featureTable = features;
        fireTableStructureChanged();
    }

    /**
     * The number of columns in the feature table. Note: for the moment, this is
     * determined by the first feature.
     *
     * @return the number of columns in this feature table.
     *
     * @todo Just gets first feature type - should use typed feature
     *       collection.  Revisit when we have FeatureDocument.
     */
    public int getColumnCount() {
        if (featureTable==null || featureTable.isEmpty()) {
            return 0;
        }
        return featureTable.features().next().getNumberOfAttributes();
    }

    /**
     * Gets the row count for the featureTable.
     *
     * @return the number of features in feature table.
     */
    public int getRowCount() {
        if (featureTable == null) {
            return 0;
        }
        return featureTable.size();
    }

    /**
     * Gets the name of a specified column.
     *
     * @param col the index of the column to get the name of.
     * @return the name of {@code col}.
     *
     * @todo Just gets first feature type - should use typed feature
     *       collection.  Revisit when we have FeatureDocument.
     */
    public String getColumnName(int col) {
        if (featureTable==null || featureTable.isEmpty()) {
            return null;
        }
        Feature firstFeature = featureTable.features().next();
        FeatureType firstType = firstFeature.getFeatureType();
        return firstType.getAttributeType(col).getLocalName();
    }

    /**
     * Gets the value stored in a specified cell. In this case, {@code row}={@code Feature}
     * and {@code col}={@code Attribute}.
     *
     * @param row the row number.
     * @param col the column number.
     * @return the value in the specified cell.
     */
    public Object getValueAt(final int row, final int col) {
        if (featureArray == null) {
            featureArray = (Feature[]) featureTable.toArray(new Feature[featureTable.size()]);
        }
        return featureArray[row].getAttribute(col);
    }
}
