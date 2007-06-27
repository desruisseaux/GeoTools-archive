/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.image.io.metadata;

// J2SE dependencies
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.imageio.metadata.IIOMetadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Geotools dependencies
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.LoggedFormat;


/**
 * Provides convenience methods for decoding metadata information. The metadata object is
 * typically an instance of {@link GeographicMetadata}, but doesn't have to. However the
 * metadata must be organized in nodes following the {@linkplain GeographicMetadataFormat
 * geographic metadata format} defined in this package.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeographicMetadataParser extends MetadataAccessor {
    /**
     * The separator between names in a node path.
     */
    private static final char SEPARATOR = '/';

    /**
     * The metadata to decode as a XML tree.
     */
    protected final Node metadata;

    /**
     * The sample dimensions. Will be created only when first needed.
     */
    private transient SampleDimensions sampleDimensions;

    /**
     * Creates a parser for the specified metadata.
     *
     * @throws IllegalArgumentException if the specified metadata doesn't support
     *         the {@value GeographicMetadataFormat#FORMAT_NAME} format.
     */
    public GeographicMetadataParser(final IIOMetadata metadata) throws IllegalArgumentException {
        this.metadata = metadata.getAsTree(GeographicMetadataFormat.FORMAT_NAME);
    }

    /**
     * Returns a parser for sample dimensions.
     */
    public SampleDimensions getSampleDimensions() {
        if (sampleDimensions == null) {
            sampleDimensions = new SampleDimensions(this);
        }
        return sampleDimensions;
    }

    /**
     * Returns child nodes at the given path. This method is for {@link #getElements(String)}
     * implementation only, and invokes itself recursively.
     */
    private static void getElements(final Node parent, final String path, final int base,
            final List/*<Element>*/ elements)
    {
        final int upper = path.indexOf(SEPARATOR, base);
        final String name = ((upper >= 0) ? path.substring(base, upper) : path.substring(base)).trim();
        final NodeList list = parent.getChildNodes();
        final int length = list.getLength();
        for (int i=0; i<length; i++) {
            Node candidate = list.item(i);
            if (name.equals(candidate.getNodeName())) {
                if (upper >= 0) {
                    getElements(candidate, path, upper+1, elements);
                } else if (candidate instanceof Element) {
                    // For the very last node, we require an element.
                    elements.add((Element) candidate);
                }
            }
        }
    }

    /**
     * Returns child nodes at the given path, or an empty list if none. Paths are separated
     * by the {@code '/'} character. Examples of valid paths that may have many elements are:
     * <ul>
     *   <li>{@code "CoordinateReferenceSystem/CoordinateSystem/Axis"}</li>
     *   <li>{@code "GridGeometry/Envelope/CoordinateValues"}</li>
     *   <li>{@code "SampleDimensions/SampleDimension"}</li>
     * </ul>
     *
     * @param  path The path of the elements to search.
     * @return The list of elements. May be empty but never null.
     */
    protected List/*<Element>*/ getElements(final String path) {
        final List elements = new ArrayList();
        getElements(metadata, path, 0, elements);
        return elements;
    }

    /**
     * Returns a child node of the given path, or {@code null} if none. If more than one node exist
     * for the given name, the first one is returned and a warning is logged. Examples of valid
     * paths that usually have only one element are:
     * <ul>
     *   <li>{@code "CoordinateReferenceSystem/Datum"}</li>
     *   <li>{@code "CoordinateReferenceSystem/CoordinateSystem"}</li>
     *   <li>{@code "GridGeometry/Envelope"}</li>
     * </ul>
     */
    protected Element getElement(final String path) {
        final List elements = getElements(path);
        final int count = elements.size();
        switch (count) {
            default: {
                log("getElement", ErrorKeys.TOO_MANY_OCCURENCES_$2,
                        new Object[] {path, new Integer(count)});
                // Fall through
            }
            case 1: return (Element) elements.get(0);
            case 0: return null;
        }
    }
}
