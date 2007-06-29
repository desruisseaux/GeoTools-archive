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
     * Sample dimensions. Will be created when first needed.
     */
    private transient SampleDimensions sampleDimensions;

    /**
     * Creates a parser for the specified metadata.
     *
     * @throws IllegalArgumentException if the specified metadata doesn't support
     *         the {@value GeographicMetadataFormat#FORMAT_NAME} format.
     */
    public GeographicMetadataParser(final IIOMetadata metadata) throws IllegalArgumentException {
        super(metadata.getAsTree(GeographicMetadataFormat.FORMAT_NAME), null, null);
    }

    /**
     * Returns a parser for sample dimensions.
     */
    public SampleDimensions getSampleDimensions() {
        if (sampleDimensions == null) {
            sampleDimensions = new SampleDimensions(parent);
        }
        return sampleDimensions;
    }
}
