/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.filter.text.cql2;

import java.util.EmptyStackException;
import java.util.Stack;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.geotools.filter.text.cql2.Token;
import org.geotools.text.filter.FilterBuilderException;


/**
 * Holds the results of the building process in a stack
 *
 * @since 2.4
 * @author Mauricio Pazos - Axios Engineering
 * @author Gabriel Roldan - Axios Engineering
 * @version $Id$
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/cql/src/main/java/org/geotools/text/filter/BuildResultStack.java $
 */
final class BuildResultStack {
    private Stack stack = new Stack();

    private BuildResultStack() {
        // no-op
    }

    /**
     * @return BuildResultStack this single instance
     */
    public static BuildResultStack getInstance() {
        return new BuildResultStack();
    }

    /**
     * Clear all result elements
     */
    public void clear() {
        stack.clear();
    }

    public Result peek() {
        return (Result) stack.peek();
    }

    public boolean empty() {
        return stack.empty();
    }

    public Result popResult() throws CQLException {
        Result item = null;

        try {
            return (Result) stack.pop();
        } catch (ClassCastException cce) {
            throw new FilterBuilderException("Expecting Expression, but found Filter",
                item.getToken());
        } catch (EmptyStackException ese) {
            throw new FilterBuilderException("No items on stack");
        }
    }

    public org.opengis.filter.expression.Expression popExpression()
        throws CQLException {
        Result item = null;

        try {
            item = (Result) stack.pop();

            return (org.opengis.filter.expression.Expression) item.getBuilt();
        } catch (ClassCastException cce) {
            throw new FilterBuilderException("Expecting Expression, but found Filter",
                item.getToken());
        } catch (EmptyStackException ese) {
            throw new FilterBuilderException("No items on stack");
        }
    }

    public Literal popLiteral() throws CQLException {
        Result item = null;

        try {
            item = (Result) stack.pop();

            return (Literal) item.getBuilt();
        } catch (ClassCastException cce) {
            throw new FilterBuilderException("Expecting Expression, but found Filter",
                item.getToken());
        } catch (EmptyStackException ese) {
            throw new FilterBuilderException("No items on stack");
        }
    }

    public PropertyName popPropertyName() throws CQLException {
        Result item = null;

        try {
            item = (Result) stack.pop();

            return (PropertyName) item.getBuilt();
        } catch (ClassCastException cce) {
            throw new FilterBuilderException("Expecting Expression, but found Filter",
                item.getToken());
        } catch (EmptyStackException ese) {
            throw new FilterBuilderException("No items on stack");
        }
    }

    public org.opengis.filter.Filter popFilter() throws CQLException {
        Result item = null;

        try {
            item = (Result) stack.pop();

            return (org.opengis.filter.Filter) item.getBuilt();
        } catch (ClassCastException cce) {
            throw new FilterBuilderException("Expecting Filter, but found Expression",
                item.getToken());
        } catch (EmptyStackException ese) {
            throw new FilterBuilderException("No items on stack");
        }
    }

    public PeriodNode popPeriod() throws CQLException {
        Result item = null;

        try {
            item = (Result) stack.pop();

            return (PeriodNode) item.getBuilt();
        } catch (ClassCastException cce) {
            throw new FilterBuilderException("Expecting Filter, but found Expression",
                item.getToken());
        } catch (EmptyStackException ese) {
            throw new FilterBuilderException("No items on stack");
        }
    }

    public double popDoubleValue() throws CQLException {
        try {
            Literal expr = this.popLiteral();
            Double number = new Double(expr.getValue().toString());

            return number.doubleValue();
        } catch (ClassCastException cce) {
            throw new FilterBuilderException("Expected double");
        }
    }

    public int popIntegerValue() throws CQLException {
        try {
            Literal expr = this.popLiteral();
            Integer number = (Integer) expr.getValue();

            return number.intValue();
        } catch (ClassCastException cce) {
            throw new FilterBuilderException("Expected double");
        }
    }

    public String popStringValue() throws CQLException {
        Literal literal = this.popLiteral();

        return literal.toString();
    }

    public String popIdentifierPart() throws CQLException {
        try {
            Result resultPart = (Result) stack.pop();
            Token token = resultPart.getToken();

            return token.image;
        } catch (ClassCastException e) {
            throw new FilterBuilderException("identifier part is expected");
        }
    }

    public String popIdentifier() throws CQLException {
        try {
            Result result = (Result) stack.pop();
            String identifier = (String) result.getBuilt();

            return identifier;
        } catch (ClassCastException e) {
            throw new FilterBuilderException("fail in identifier parsing");
        }
    }

    public void push(Result item) {
        stack.push(item);
    }

    public int size() {
        return stack.size();
    }
}
