/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.pt;

// OpenGIS dependencies
import org.opengis.pt.PT_CoordinatePoint;
import org.opengis.pt.PT_Envelope;
import org.opengis.pt.PT_Matrix;


/**
 * Provide methods for interoperability with OpenGIS PT package.
 * All methods accept null argument. All OpenGIS objects are suitable for RMI use.
 *
 * A default instance of the <code>Adapters</code> classes is provided in the
 * {@link org.geotools.ct.Adapters#getDefault() org.geotools.ct} package.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.ct.Adapters#getDefault()
 *
 * @deprecated The legacy OpenGIS PT package is deprecated.
 *             There is no replacement at this time for RMI objects.
 */
public class Adapters {
    /**
     * Default constructor.
     */
    protected Adapters() {
    }
    
    /**
     * Returns an OpenGIS structure for a coordinate point.
     * Changes to the returned structure will not affect the original point.
     */
    public PT_CoordinatePoint export(final CoordinatePoint point) {
        if (point==null) {
            return null;
        }
        final PT_CoordinatePoint pt = new PT_CoordinatePoint();
        pt.ord = (double[]) point.ord.clone();
        return pt;
    }
    
    /**
     * Returns an OpenGIS structure for an envelope.
     * Changes to the returned structure will not affect the original envelope.
     */
    public PT_Envelope export(final Envelope envelope) {
        if (envelope==null) {
            return null;
        }
        final int  dimension = envelope.getDimension();
        final PT_Envelope ep = new PT_Envelope();
        ep.minCP = new PT_CoordinatePoint();
        ep.maxCP = new PT_CoordinatePoint();
        ep.minCP.ord = new double[dimension];
        ep.maxCP.ord = new double[dimension];
        for (int i=0; i<dimension; i++) {
            ep.minCP.ord[i] = envelope.getMinimum(i);
            ep.maxCP.ord[i] = envelope.getMaximum(i);
        }
        return ep;
    }
    
    /**
     * Returns an OpenGIS structure for a matrix.
     * Changes to the returned structure will not affect the original matrix.
     */
    public PT_Matrix export(final Matrix matrix) {
        final PT_Matrix m = new PT_Matrix();
        m.elt = matrix.getElements();
        return m;
    }
    
    /**
     * Returns a coordinate point from an OpenGIS's structure.
     * Changes to the returned point will not affect the original structure.
     */
    public CoordinatePoint wrap(final PT_CoordinatePoint point) {
        return (point!=null) ? new CoordinatePoint(point.ord) : null;
    }
    
    /**
     * Returns an envelope from an OpenGIS's structure.
     * Changes to the returned envelope will not affect the original structure.
     */
    public Envelope wrap(final PT_Envelope envelope) {
        return (envelope!=null) ? new Envelope(envelope.minCP.ord, envelope.maxCP.ord) : null;
    }
    
    /**
     * Returns a matrix from an OpenGIS's structure.
     * Changes to the returned matrix will not affect the original structure.
     */
    public Matrix wrap(final PT_Matrix matrix) {
        return (matrix!=null) ? new Matrix(matrix.elt) : null;
    }
}
