/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le D�veloppement
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
package org.geotools.gp;

// J2SE dependencies
import java.rmi.RemoteException;

import javax.media.jai.Interpolation;

import org.geotools.cv.Coverage;
import org.geotools.gc.GridCoverage;
import org.opengis.cv.CV_Coverage;
import org.opengis.gc.GC_GridCoverage;


/**
 * Provide methods for interoperability with OpenGIS GC package.
 * All methods accept null argument.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated The legacy OpenGIS GP package is deprecated.
 *             There is no replacement at this time for RMI objects.
 */
public class Adapters extends org.geotools.gc.Adapters {
    /**
     * Default adapters. Will be constructed
     * only when first requested.
     */
    private static Adapters DEFAULT;
    
    /**
     * Default constructor.
     *
     * @param CTS The adapters from <cite>Coordinate Transformation Services</cite>
     *            (package <code>org.geotools.ct</code>).
     */
    protected Adapters(final org.geotools.ct.Adapters CTS) {
        super(CTS);
    }
    
    /**
     * Returns the default adapters.
     */
    public static synchronized Adapters getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new Adapters(org.geotools.ct.Adapters.getDefault());
        }
        return DEFAULT;
    }

    /**
     * Performs the wrapping of an OpenGIS's interface. This method is invoked by
     * {@link #wrap(CV_Coverage)} and {@link #wrap(GC_GridCoverage)} if a Geotools
     * object is not already presents in the cache.
     *
     * @param  coverage The OpenGIS object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    protected Coverage doWrap(final CV_Coverage coverage) throws RemoteException {
        Coverage wrapped = super.doWrap(coverage);
        if (coverage instanceof GridCoverage.Remote) {
            final Interpolation interp = ((GridCoverage.Remote) coverage).getInterpolation();
            wrapped = GridCoverageProcessor.getDefault().doOperation(
                        "Interpolate", (GridCoverage) coverage, "Type", interp);
        }
        return wrapped;
    }
}
