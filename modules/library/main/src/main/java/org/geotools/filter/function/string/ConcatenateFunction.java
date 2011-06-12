package org.geotools.filter.function.string;

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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

//this code is autogenerated - you shouldnt be modifying it!
import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.util.Arrays;
import java.util.List;

import org.geotools.filter.FunctionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;

/**
 * The function concatenates strings.
 * <p>
 * It is used to create concatenated strings as arguments of functions.
 * <p>
 * Implementation of Concatenate as defined by SE1.1.
 * <p>
 * 
 * @author Jody Garnett (Refractions Research, Inc.)
 *
 *
 * @source $URL$
 */
public class ConcatenateFunction extends FunctionImpl {

    /**
     * Make the instance of FunctionName available in
     * a consistent spot.
     */
    public static final FunctionName NAME = functionName("Concatenate", "result:String", "text:String:2,");

    public ConcatenateFunction() {
        this.functionName = NAME;
    }
    
    @Override
    public String getName() {
    	return NAME.getName();
    }

    public int getArgCount() {
        return NAME.getArgumentCount();
    }

    public Object evaluate(Object feature) {
        StringBuffer text = new StringBuffer();
        for (Expression expression : (List<Expression>) getParameters()) {
            try {
                String str = (String) expression.evaluate(feature, String.class);
                if (str != null) {
                    text.append(str);
                }
            } catch (Exception couldNotCompute) {
                // log me please
            }
        }
        return text.toString();
    }
}
