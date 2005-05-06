/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.quality;

// OpenGIS direct dependencies
import org.opengis.metadata.quality.Result;
import org.opengis.metadata.quality.EvaluationMethodType;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.citation.Citation;
import org.geotools.util.SimpleInternationalString;


/**
 * Accuracy of the position of features.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class PositionalAccuracy extends Element
       implements org.opengis.metadata.quality.PositionalAccuracy
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6043381860937480828L;

    /**
     * Indicates that a {@linkplain org.opengis.referencing.operation.Transformation transformation}
     * requires a datum shift and some method has been applied. Datum shift methods often use
     * {@linkplain org.geotools.referencing.datum.BursaWolfParameters Bursa Wolf parameters},
     * but other kind of method may have been applied as well.
     *
     * @see org.opengis.referencing.operation.Transformation#getPositionalAccuracy
     * @see org.geotools.referencing.operation.AbstractCoordinateOperationFactory#DATUM_SHIFT
     */
    public static final PositionalAccuracy DATUM_SHIFT_APPLIED;

    /**
     * Indicates that a {@linkplain org.opengis.referencing.operation.Transformation transformation}
     * requires a datum shift, but no method has been found applicable. This usually means that no
     * {@linkplain org.geotools.referencing.datum.BursaWolfParameters Bursa Wolf parameters} have
     * been found. Such datum shifts are approximative and may have 1 kilometer error. This
     * pseudo-transformation is allowed by
     * {@linkplain org.geotools.referencing.operation.CoordinateOperationFactory coordinate
     * operation factory} only if it was created with
     * {@link org.geotools.factory.Hints#LENIENT_DATUM_SHIFT} set to {@link Boolean#TRUE}.
     *
     * @see org.opengis.referencing.operation.Transformation#getPositionalAccuracy
     * @see org.geotools.referencing.operation.AbstractCoordinateOperationFactory#ELLIPSOID_SHIFT
     */
    public static final PositionalAccuracy DATUM_SHIFT_OMITTED;
    static {
        // TODO: localize.
        final InternationalString desc = new SimpleInternationalString("Transformation accuracy");
        final InternationalString eval = new SimpleInternationalString("Is a datum shift method applied?");
        final ConformanceResult   pass = new ConformanceResult(Citation.GEOTOOLS, eval, true );
        final ConformanceResult   fail = new ConformanceResult(Citation.GEOTOOLS, eval, false);
        pass.freeze();
        fail.freeze();
        DATUM_SHIFT_APPLIED = new AbsoluteExternalPositionalAccuracy(pass);
        DATUM_SHIFT_OMITTED = new AbsoluteExternalPositionalAccuracy(fail);
        DATUM_SHIFT_APPLIED.setMeasureDescription(desc);
        DATUM_SHIFT_OMITTED.setMeasureDescription(desc);
        DATUM_SHIFT_APPLIED.setEvaluationMethodDescription(eval);
        DATUM_SHIFT_OMITTED.setEvaluationMethodDescription(eval);
        DATUM_SHIFT_APPLIED.setEvaluationMethodType(EvaluationMethodType.DIRECT_INTERNAL);
        DATUM_SHIFT_OMITTED.setEvaluationMethodType(EvaluationMethodType.DIRECT_INTERNAL);
        DATUM_SHIFT_APPLIED.freeze();
        DATUM_SHIFT_OMITTED.freeze();
    }

    /**
     * Constructs an initially empty positional accuracy.
     */
    public PositionalAccuracy() {
    }

    /**
     * Creates an positional accuracy initialized to the given result.
     */
    public PositionalAccuracy(final Result result) {
        super(result);
    }
}
