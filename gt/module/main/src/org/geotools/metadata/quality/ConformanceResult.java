/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
import org.geotools.resources.Utilities;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.InternationalString;


/**
 * Information about the outcome of evaluating the obtained value (or set of values) against
 * a specified acceptable conformance quality level.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class ConformanceResult extends Result
        implements org.opengis.metadata.quality.ConformanceResult
{

    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 6429932577869033286L;
    
    /**
     * Citation of product specification or user requirement against which data is being evaluated.
     */
    private Citation specification;

    /**
     * Explanation of the meaning of conformance for this result.
     */
    private InternationalString explanation;

    /**
     * Indication of the conformance result.
     */
    private boolean pass;
    
    /**
     * Construct an initially empty conformance result.
     */
    public ConformanceResult() {
    }

    /**
     * Creates a conformance result.
     */
    public ConformanceResult(final Citation specification) {
        setSpecification(specification);
    }
    
    /**
     * Citation of product specification or user requirement against which data is being evaluated.
     */
    public Citation getSpecification() {
        return specification;
    }

    /**
     * Set the citation of product specification or user requirement against which data
     * is being evaluated.
     */
    public synchronized void setSpecification(final Citation newValue) {
        checkWritePermission();
        specification = newValue;
    }

    /**
     * Explanation of the meaning of conformance for this result.
     */
    public InternationalString getExplanation() {
        return explanation;
    }

    /**
     * Set the explanation of the meaning of conformance for this result.
     */
    public synchronized void setExplanation(final InternationalString newValue) {
        checkWritePermission();
        explanation = newValue;
    }

    /**
     * Indication of the conformance result.
     */
    public boolean pass() {
        return pass;
    }

    /**
     * Set the indication of the conformance result.
     */
    public synchronized void setPass(final boolean newValue) {
        checkWritePermission();
        pass = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        specification = (Citation)            unmodifiable(specification);
        explanation   = (InternationalString) unmodifiable(explanation  );
    }

    /**
     * Compare this conformance result with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ConformanceResult that = (ConformanceResult) object;
            return Utilities.equals(this.specification, that.specification ) &&
                   Utilities.equals(this.explanation,   that.explanation   ) &&
                                   (this.pass        == that.pass          );
        }
        return false;
    }

    /**
     * Returns a hash code value for this series.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (specification != null) code ^= specification.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this result.
     */
    public String toString() {
        return String.valueOf(specification);
    }
}
