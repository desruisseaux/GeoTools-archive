/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, 2004 Geotools Project Managment Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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

package org.geotools.referencing.wkt;

// J2SE dependencies
//import java.util.Set;
//import java.util.HashSet;
import java.util.Collections;

// Parsing
import java.util.Locale;
import java.text.ParsePosition;
import java.text.ParseException;
import java.text.FieldPosition;

// Geotools dependencies
import org.geotools.referencing.FactoryFinder;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// OpenGIS dependencies
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.OperationParameter;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.NoninvertibleTransformException;

/*
 * 
 * @version $Id:$
 * @author Remi Eve
 * @author Martin Desruisseaux
 * @author Rueben Schulz
 */
public final class MathTransformParser extends AbstractParser {
    
    /**
     * The factory to use for creating math transform.
     */   
    private final MathTransformFactory mtFactory;
    
    /**
     * Construct a parser for the specified locale using default factories.
     *
     * @param locale The locale for parsing and formatting numbers.
     */
    public MathTransformParser(final Locale locale) {
        this(locale, (org.geotools.referencing.operation.MathTransformFactory)FactoryFinder.getMathTransformFactory());
    }
    
    /**
     * Construct a parser for the specified locale and factory.
     *
     * @param locale The locale for parsing and formatting numbers.
     * @param mtFactory The {@link MathTransformFactory} to use to create 
     * {@link MathTransform} objects.
     */
    public MathTransformParser(final Locale locale, final MathTransformFactory mtFactory) {
        super(locale);
        this.mtFactory = mtFactory;
    }
    
    /**
     * Parses the next element in the specified <cite>Well Know Text</cite> (WKT) tree.
     *
     * @param  element The element to be parsed.
     * @return The object.
     * @throws ParseException if the element can't be parsed.
     */
    protected Object parse(final Element element) throws ParseException {
        return parseMathTransform(element, true);
    }
    
    /**
     * Parses the next element (a {@link MathTransform}) in the specified
     * <cite>Well Know Text</cite> (WKT) tree.
     *
     * @param  element The parent element.
     * @param  required True if parameter is required and false in other case.
     * @return The next element as a {@link MathTransform} object.
     * @throws ParseException if the next element can't be parsed.
     */
    private MathTransform parseMathTransform(final Element element, final boolean required) throws ParseException {
        final Object key = element.peek();
        if (key instanceof Element) {
            final String keyword = ((Element) key).keyword.trim().toUpperCase(locale);
            if ("PARAM_MT"      .equals(keyword))  return parseParamMT      (element);
            if ("CONCAT_MT"     .equals(keyword))  return parseConcatMT     (element);
            if ("INVERSE_MT"    .equals(keyword))  return parseInverseMT    (element);
            if ("PASSTHROUGH_MT".equals(keyword))  return parsePassThroughMT(element);
        }
        if (required) {
            throw element.parseFailed(null, Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, key));
        }
        return null;
    }
    
    /**
     * Parses a "PARAM_MT" element. This element has the following pattern:
     *
     * <blockquote><code>
     * PARAM_MT["<classification-name>" {,<parameter>}* ]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "PARAM_MT" element as an {@link MathTransform} object.
     * @throws ParseException if the "PARAM_MT" element can't be parsed.
     */
    private MathTransform parseParamMT(final Element parent) throws ParseException {     
        final Element    element = parent.pullElement("PARAM_MT");
        final String classification = element.pullString("classification");
        GeneralParameterValue[] parameters;
        try {
            parameters = mtFactory.getDefaultParameters(classification);
        } catch (NoSuchIdentifierException exception) {
            throw element.parseFailed(exception, null);
        }

        /*
         * Scan over all PARAMETER["name", value] elements and
         * set the corresponding parameter in the ParameterList.
         */
        Element param;
        ParameterValueGroup params = new org.geotools.parameter.ParameterValueGroup(
            Collections.singletonMap("name", "params"), parameters);
        while ((param=element.pullOptionalElement("PARAMETER")) != null) {
            final String name = param.pullString("name");
//I am assuming this will not be a ParameterValueGroup (bad assumption?)
            ParameterValue parameter = (ParameterValue) params.getValue(name);
//should be able to use parameter.getValue().getClass() here, but it returns null            
            if (Integer.class.equals(((OperationParameter)parameter.getDescriptor()).getValueClass())) {
                parameter.setValue(param.pullInteger("value"));
            } else {
                parameter.setValue(param.pullDouble("value"));
            }
            param.close();
        }
        element.close();
        try {
            return mtFactory.createParameterizedTransform(classification, parameters);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }    
    
    /**
     * Parses a "INVERSE_MT" element. This element has the following pattern:
     *
     * <blockquote><code>
     * INVERSE_MT[<math transform>]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "INVERSE_MT" element as an {@link MathTransform} object.
     * @throws ParseException if the "INVERSE_MT" element can't be parsed.
     */
    private MathTransform parseInverseMT(final Element parent) throws ParseException {       
        final Element element = parent.pullElement("INVERSE_MT");
        try {
            final MathTransform transform = ((MathTransform)parseMathTransform(element, true)).inverse();
            element.close();
            return transform;
        }
        catch (NoninvertibleTransformException exception) {
            throw element.parseFailed(exception, null);
        }
    }
    
    /**
     * Parses a "PASSTHROUGH_MT" element. This element has the following pattern:
     *
     * <blockquote><code>
     * PASSTHROUGH_MT[<integer>, <math transform>]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "PASSTHROUGH_MT" element as an {@link MathTransform} object.
     * @throws ParseException if the "PASSTHROUGH_MT" element can't be parsed.
     */
    private MathTransform parsePassThroughMT(final Element parent) throws ParseException {
        final Element        element = parent.pullElement("PASSTHROUGH_MT");
        final int firstAffectedOrdinate = parent.pullInteger("firstAffectedOrdinate");
        final MathTransform   transform = parseMathTransform(element, true);
        element.close();
        try {
            return mtFactory.createPassThroughTransform(firstAffectedOrdinate, transform, 0);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }   
    }  
        
    /**
     * Parses a "CONCAT_MT" element. This element has the following pattern:
     *
     * <blockquote><code>
     * CONCAT_MT[<math transform> {,<math transform>}*]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "CONCAT_MT" element as an {@link MathTransform} object.
     * @throws ParseException if the "CONCAT_MT" element can't be parsed.
     */
    private MathTransform parseConcatMT(final Element parent) throws ParseException {       
        final Element element = parent.pullElement("CONCAT_MT");
        MathTransform transform = parseMathTransform(element, true);
        
        MathTransform optionalTransform;
        while ((optionalTransform = parseMathTransform(element, false)) != null) {
            try {
                transform = mtFactory.createConcatenatedTransform(transform, optionalTransform);
            } catch (FactoryException exception) {
                throw element.parseFailed(exception, null);
            }
        }
        element.close();
        return transform;
    }
    
    /**
     * Format the specified object. Current implementation just append {@link Object#toString},
     * since the <code>toString()</code> implementation for most {@link org.geotools.cs.Info}
     * objects is to returns a WKT.
     *
     * @task TODO: Provides pacakge private <code>Info.toString(WKTFormat)</code> implementations.
     *             It would allows us to invoke <code>((Info)obj).toString(this)</code> here.
     */
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return toAppendTo.append(obj);
    }     
}


