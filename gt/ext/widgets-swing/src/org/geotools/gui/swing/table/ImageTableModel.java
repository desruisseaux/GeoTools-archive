/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.gui.swing.table;

// J2SE dependencies
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.table.AbstractTableModel;


/**
 * A table model for image sample values (or pixels). This model is serialiable if the
 * {@link RenderedImage} is serializable.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ImageTableModel extends AbstractTableModel {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -408603520054548181L;

    /**
     * The image to display.
     */
    private RenderedImage image;

    /**
     * The format to use for formatting sample values.
     */
    private NumberFormat format = NumberFormat.getNumberInstance();

    /**
     * The format to use for formatting line and column labels.
     */
    private NumberFormat titleFormat = NumberFormat.getIntegerInstance();

    /**
     * The band to show.
     */
    private int band;

    /**
     * Image properites computed by {@link #update}. Those properties are used everytime
     * {@link #getValueAt} is invoked, which is why we cache them.
     */
    private transient int minX, minY, tileGridXOffset, tileGridYOffset, tileWidth, tileHeight, dataType;
    
    /**
     * The type of sample values. Is computed by {@link #update}.
     */
    private transient Class type = Number.class;

    /**
     * The row and column names. Will be created only when first needed.
     */
    private transient String[] rowNames, columnNames;

    /**
     * Creates a new table model.
     */
    public ImageTableModel() {
    }

    /**
     * Creates a new table model for the specified image.
     */
    public ImageTableModel(final RenderedImage image) {
        setRenderedImage(image);
    }

    /**
     * Returns the image to display, or {@code null} if none.
     */
    public RenderedImage getRenderedImage() {
        return image;
    }

    /**
     * Sets the image to display.
     */
    public void setRenderedImage(final RenderedImage image) {
        this.image = image;
        rowNames    = null;
        columnNames = null;
        format.setMinimumFractionDigits(update());
        fireTableStructureChanged();
    }

    /**
     * Update transient fields after an image change. Also invoked after deserialization.
     * Returns the number of fraction digits to use for the format (to be ignored in the
     * case of deserialization, since the format is serialized).
     */
    private int update() {
        int digits = 0;
        if (image != null) {
            minX            = image.getMinX();
            minY            = image.getMinY();
            tileGridXOffset = image.getTileGridXOffset();
            tileGridYOffset = image.getTileGridYOffset();
            tileWidth       = image.getTileWidth();
            tileHeight      = image.getTileHeight();
            dataType        = image.getSampleModel().getDataType();
            switch (dataType) {
                case DataBuffer.TYPE_BYTE:    // Fall through
                case DataBuffer.TYPE_SHORT:   // Fall through
                case DataBuffer.TYPE_USHORT:  // Fall through
                case DataBuffer.TYPE_INT:     type=Integer.class;           break;
                case DataBuffer.TYPE_FLOAT:   type=Float  .class; digits=1; break;
                case DataBuffer.TYPE_DOUBLE:  type=Double .class; digits=1; break;
                default:                      type=Number .class;           break;
            }
        } else {
            type = Number.class;
        }
        return digits;
    }

    /**
     * Recomputes transient fields after deserializations.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        update();
    }

    /**
     * Returns the band to display.
     */
    public int getBand() {
        return band;
    }

    /**
     * Set the band to display.
     */
    public void setBand(final int band) {
        if (band<0 || (image!=null && band>=image.getSampleModel().getNumBands())) {
            throw new IndexOutOfBoundsException();
        }
        this.band = band;
        fireTableDataChanged();
    }

    /**
     * Returns the format to use for formatting sample values.
     */
    public NumberFormat getNumberFormat() {
        return format;
    }

    /**
     * Sets the format to use for formatting sample values.
     */
    public void setNumberFormat(final NumberFormat format) {
        this.format = format;
        fireTableDataChanged();
    }

    /**
     * Returns the number of rows in the model, which is, which is
     * the {@linkplain RenderedImage#getHeight image height}.
     */
    public int getRowCount() {
        return (image!=null) ? image.getHeight() : 0;
    }

    /**
     * Returns the number of columns in the model, which is
     * the {@linkplain RenderedImage#getWidth image width}.
     */
    public int getColumnCount() {
        return (image!=null) ? image.getWidth() : 0;
    }

    /**
     * Returns the row name. The names are the pixel row number, starting at
     * the {@linkplain RenderedImage#getMinY min y} value.
     */
    public String getRowName(final int row) {
        if (rowNames == null) {
            rowNames = new String[image.getHeight()];
        }
        String candidate = rowNames[row];
        if (candidate == null) {
            rowNames[row] = candidate = titleFormat.format(minY + row);
        }
        return candidate;
    }

    /**
     * Returns the column name. The names are the pixel column number, starting at
     * the {@linkplain RenderedImage#getMinX min x} value.
     */
    public String getColumnName(final int column) {
        if (columnNames == null) {
            if (image == null) {
                return super.getColumnName(column);
            }
            columnNames = new String[image.getWidth()];
        }
        String candidate = columnNames[column];
        if (candidate == null) {
            columnNames[column] = candidate = titleFormat.format(minX + column);
        }
        return candidate;
    }

    /**
     * Returns a column given its name.
     */
    public int findColumn(final String name) {
        if (image!=null) try {
            return titleFormat.parse(name).intValue() - minX;
        } catch (ParseException exception) {
            // Ignore; fallback on the default algorithm.
        }
        return super.findColumn(name);
    }

    /**
     * Returns the type of sample values regardless of column index.
     */
    public Class getColumnClass(final int column) {
        return type;
    }

    /**
     * Returns the sample value at the specified row and column.
     */
    public Object getValueAt(int x, int y) {
        int tx, ty, lg;
        x += minX; tx = x-tileGridXOffset; if (x<0) tx += 1-tileWidth;
        y += minY; ty = y-tileGridYOffset; if (y<0) ty += 1-tileHeight;
        final Raster raster = image.getTile(tx/tileWidth, ty/tileHeight);
        switch (dataType) {
            default:                      return new Integer(raster.getSample      (x,y,band));
            case DataBuffer.TYPE_FLOAT:   return new Float  (raster.getSampleFloat (x,y,band));
            case DataBuffer.TYPE_DOUBLE:  return new Double (raster.getSampleDouble(x,y,band));
        }
    }
}
