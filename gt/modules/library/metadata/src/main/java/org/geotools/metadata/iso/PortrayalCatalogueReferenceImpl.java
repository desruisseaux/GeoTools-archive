/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso;

// J2SE direct dependencies
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.PortrayalCatalogueReference;


/**
 * Information identifying the portrayal catalogue used.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class PortrayalCatalogueReferenceImpl extends MetadataEntity
        implements PortrayalCatalogueReference
{
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -3095277682987563157L;

    /**
     * Bibliographic reference to the portrayal catalogue cited.
     */
    private Collection portrayalCatalogueCitations;

    /**
     * Construct an initially empty portrayal catalogue reference.
     */
    public PortrayalCatalogueReferenceImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public PortrayalCatalogueReferenceImpl(final PortrayalCatalogueReference source) {
        super(source);
    }

    /**
     * Creates a portrayal catalogue reference initialized to the given values.
     */
    public PortrayalCatalogueReferenceImpl(final Collection portrayalCatalogueCitations) {
        setPortrayalCatalogueCitations(portrayalCatalogueCitations);
    }
    
    /**
     * Bibliographic reference to the portrayal catalogue cited.
     */
    public synchronized Collection getPortrayalCatalogueCitations() {
        return portrayalCatalogueCitations = nonNullCollection(portrayalCatalogueCitations, Citation.class);
    }

    /**
     * Set bibliographic reference to the portrayal catalogue cited.
     */
    public synchronized void setPortrayalCatalogueCitations(Collection newValues) {
        portrayalCatalogueCitations = copyCollection(newValues, portrayalCatalogueCitations, Citation.class);
    }
}
