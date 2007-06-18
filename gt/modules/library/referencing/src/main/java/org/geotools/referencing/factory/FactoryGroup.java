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
 */
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.*;

import javax.units.Unit;
import javax.units.ConversionException;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;  // For javadoc
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.*;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.factory.Factory;
import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;
import org.geotools.parameter.Parameters;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.referencing.operation.MathTransformProvider;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.matrix.MatrixFactory;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.cs.AbstractCS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.XArray;


/**
 * A set of utilities methods working on factories. Many of those methods requires more than
 * one factory. Consequently, they can't be a method in a single factory. Furthermore, since
 * they are helper methods and somewhat implementation-dependent, they are not part of GeoAPI.
 *
 * @deprecated Renamed to ReferencingFactoryContainer in order to reflect use 
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class FactoryGroup extends ReferencingFactoryContainer {
    public FactoryGroup(final Hints userHints) {
        super( userHints );
    }
    /**
     * Creates an instance from the specified hints. This method recognizes the
     * {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS}, {@link Hints#DATUM_FACTORY DATUM}
     * and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM} {@code FACTORY} hints.
     *
     * @param  hints The hints, or {@code null} if none.
     * @return A factory group created from the specified set of hints.
     *
     * @since 2.2
     */
    public static FactoryGroup createInstance(final Hints hints) {
        /* just so we can play the deprecation game */            
        return (FactoryGroup) ReferencingFactoryContainer.instance( hints );               
    }
}