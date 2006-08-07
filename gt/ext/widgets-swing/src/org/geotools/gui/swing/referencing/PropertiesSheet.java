/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.gui.swing.referencing;

// J2SE dependencies
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

// OpenGIS dependencies
import org.opengis.referencing.IdentifiedObject;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.referencing.wkt.UnformattableObjectException;


/**
 * Display informations about a CRS object. Current implementation only display the
 * <cite>Well Known Text</cite> (WKT). We may provide more informations in a future
 * version.
 *
 * @since 2.3
 * @version $Id$
 * @source $URL$
 * @author Martin Desruisseaux
 */
public class PropertiesSheet extends JComponent {
    /**
     * Provides different view of the CRS object (properties, WKT, etc.).
     */
    private final JTabbedPane tabs;

    /**
     * The <cite>Well Known Text</cite> area.
     */
    private final JTextArea wktArea;

    /**
     * Creates a new, initially empty, property sheet.
     */
    public PropertiesSheet() {
        tabs    = new JTabbedPane();
        wktArea = new JTextArea();
        wktArea.setEditable(false);
        tabs.addTab("WKT", new JScrollPane(wktArea));
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    /**
     * Sets the object to display in this property sheet.
     *
     * @param item The object to display info about.
     */
    public void setIdentifiedObject(final IdentifiedObject item) {
        String text;
        try {
            text = item.toWKT();
        } catch (UnsupportedOperationException e) {
            text = e.getLocalizedMessage();
            if (text == null) {
                text = Utilities.getShortClassName(e);
            }
            final String lineSeparator = System.getProperty("line.separator", "\n");
            if (e instanceof UnformattableObjectException) {
                text = Vocabulary.format(VocabularyKeys.WARNING) + ": " + text +
                        lineSeparator + lineSeparator + item + lineSeparator;
            } else {
                text = Vocabulary.format(VocabularyKeys.ERROR) + ": " + text + lineSeparator;
            }
        }
        wktArea.setText(text);
    }

    /**
     * Sets an error message to display instead of the current identified object.
     *
     * @param message The error message.
     */
    public void setErrorMessage(final String message) {
        wktArea.setText(Vocabulary.format(VocabularyKeys.ERROR_$1, message));
    }
}
