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
package org.geotools.metadata.iso.quality;

// J2SE direct dependencies
import java.util.Collection;
import java.util.Date;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.quality.Element;
import org.opengis.metadata.quality.EvaluationMethodType;
import org.opengis.metadata.quality.Result;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Type of test applied to the data specified by a data quality scope.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class ElementImpl extends MetadataEntity implements Element {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3542504624077298894L;
    
    /**
     * Name of the test applied to the data.
     */
    private Collection namesOfMeasure;

    /**
     * Code identifying a registered standard procedure, or <code>null</code> if none.
     */
    private Identifier measureIdentification;

    /**
     * Description of the measure being determined.
     */
    private InternationalString measureDescription;

    /**
     * Type of method used to evaluate quality of the dataset, or <code>null</code> if unspecified.
     */
    private EvaluationMethodType evaluationMethodType;

    /**
     * Description of the evaluation method.
     */
    private InternationalString evaluationMethodDescription;

    /**
     * Reference to the procedure information, or <code>null</code> if none.
     */
    private Citation evaluationProcedure;

    /**
     * Date or range of dates on which a data quality measure was applied.
     * The array length is 1 for a single date, or 2 for a range. Returns
     * <code>null</code> if this information is not available.
     */
    private long date1, date2;

    /**
     * Value (or set of values) obtained from applying a data quality measure or the out
     * come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     */
    private Result result;

    /**
     * Constructs an initially empty element.
     */
    public ElementImpl() {
    }

    /**
     * Creates an element initialized to the given result.
     */
    public ElementImpl(final Result result) {
        setResult(result);
    }
    
    /**
     * Returns the name of the test applied to the data.
     */
    public synchronized Collection getNamesOfMeasure() {
        return namesOfMeasure = nonNullCollection(namesOfMeasure, InternationalString.class);
    }

    /**
     * Set the name of the test applied to the data.
     */
    public synchronized void setNamesOfMeasure(final Collection newValues) {
        namesOfMeasure = copyCollection(newValues, namesOfMeasure, InternationalString.class);
    }

    /**
     * Returns the code identifying a registered standard procedure, or <code>null</code> if none.
     */
    public Identifier getMeasureIdentification() {
        return measureIdentification;
    }

    /**
     * Set the code identifying a registered standard procedure.
     */
    public synchronized void setMeasureIdentification(final Identifier newValue)  {
        checkWritePermission();
        measureIdentification = newValue;
    }

    /**
     * Returns the description of the measure being determined.
     */
    public InternationalString getMeasureDescription() {
        return measureDescription;
    }

    /**
     * Set the description of the measure being determined.
     */
    public synchronized void setMeasureDescription(final InternationalString newValue)  {
        checkWritePermission();
        measureDescription = newValue;
    }

    /**
     * Returns the type of method used to evaluate quality of the dataset,
     * or <code>null</code> if unspecified.
     */
    public EvaluationMethodType getEvaluationMethodType() {
        return evaluationMethodType;
    }

    /**
     * Set the ype of method used to evaluate quality of the dataset.
     */
    public synchronized void setEvaluationMethodType(final EvaluationMethodType newValue)  {
        checkWritePermission();
        evaluationMethodType = newValue;
    }

    /**
     * Returns the description of the evaluation method.
     */
    public InternationalString getEvaluationMethodDescription() {
        return evaluationMethodDescription;
    }

    /**
     * Set the description of the evaluation method.
     */
    public synchronized void setEvaluationMethodDescription(final InternationalString newValue)  {
        checkWritePermission();
        evaluationMethodDescription = newValue;
    }

    /**
     * Returns the reference to the procedure information, or <code>null</code> if none.
     */
    public Citation getEvaluationProcedure() {
        return evaluationProcedure;
    }

    /**
     * Set the reference to the procedure information.
     */
    public synchronized void setEvaluationProcedure(final Citation newValue) {
        checkWritePermission();
        evaluationProcedure = newValue;
    }

    /**
     * Returns the date or range of dates on which a data quality measure was applied.
     * The array length is 1 for a single date, or 2 for a range. Returns
     * <code>null</code> if this information is not available.
     */
    public synchronized Date[] getDate() {
        if (date1 == Long.MIN_VALUE) {
            return null;
        }
        if (date2 == Long.MIN_VALUE) {
            return new Date[] {new Date(date1)};
        }
        return new Date[] {new Date(date1), new Date(date2)};
    }

    /**
     * Set the date or range of dates on which a data quality measure was applied.
     * The array length is 1 for a single date, or 2 for a range.
     */
    public synchronized void setDate(final Date[] newValue) {
        checkWritePermission();
        date1 = date2 = Long.MIN_VALUE;
        if (newValue != null) {
            switch (newValue.length) {
                default: throw new IllegalArgumentException(); // TODO: provide a localized message
                case  2: date2 = newValue[1].getTime(); // Fall through
                case  1: date1 = newValue[0].getTime(); // Fall through
                case  0: break;
            }
        }
    }

    /**
     * Returns the value (or set of values) obtained from applying a data quality measure or
     * the out come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     */
    public Result getResult() {
        return result;
    }

    /**
     * Set the value (or set of values) obtained from applying a data quality measure or
     * the out come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     */
    public synchronized void setResult(final Result newValue) {
        checkWritePermission();
        result = newValue;
    }
    
    /**
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        namesOfMeasure              = (Collection)          unmodifiable(namesOfMeasure);
        measureIdentification       = (Identifier)          unmodifiable(measureIdentification);
        measureDescription          = (InternationalString) unmodifiable(measureDescription);
        evaluationMethodDescription = (InternationalString) unmodifiable(evaluationMethodDescription);
        evaluationProcedure         = (Citation)            unmodifiable(evaluationProcedure);
        result                      = (Result)              unmodifiable(result);
    }

    /**
     * Compare this element with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ElementImpl that = (ElementImpl) object;
            return Utilities.equals(this.namesOfMeasure,              that.namesOfMeasure              ) &&
                   Utilities.equals(this.measureIdentification,       that.measureIdentification       ) &&
                   Utilities.equals(this.measureDescription,          that.measureDescription          ) &&
                   Utilities.equals(this.evaluationMethodType,        that.evaluationMethodType        ) &&
                   Utilities.equals(this.evaluationMethodDescription, that.evaluationMethodDescription ) &&
                   Utilities.equals(this.evaluationProcedure,         that.evaluationProcedure         ) &&
                   Utilities.equals(this.result,                      that.result                      ) &&
                                    this.date1                     == that.date1                         &&
                                    this.date2                     == that.date2;
        }
        return false;
    }

    /**
     * Returns a hash code value for this address. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (namesOfMeasure != null)        code ^= namesOfMeasure       .hashCode();
        if (measureIdentification != null) code ^= measureIdentification.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this citation.
     *
     * @todo localize
     */
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringBuffer buffer = new StringBuffer();
        if (measureDescription != null) {
            buffer.append("Measure: ");
            buffer.append(measureDescription);
            buffer.append(lineSeparator);
        }
        buffer.append(result);
        return buffer.toString();
    }
}
