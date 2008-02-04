/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Management Committee (PMC)
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

import org.geotools.filter.text.generated.parsers.Token;

/**
 * Adapts Token to {@link IToken}.
 * <p>
 * Token class is generated by javacc, this adapter allow reuse this class in
 * different context (Parser, Compiler and FilterBuilder)
 * </p>
 * <p>
 * The token class, used by the specific parser, requires to be adapted to
 * implementing the interface {@link IToken} which is used by the
 * {@link FilterBuilder}.
 * </p>
 * 
 * @see IToken
 * @see FilterBuilder
 * @see CQLCompiler
 * @see CQLParser
 * 
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
public class TokenAdapter implements IToken {

    private final Token cqlToken;

    private TokenAdapter(Token token) {
        this.cqlToken = token;
    }

    public static IToken newAdapterFor(Token token) {
        return new TokenAdapter(token);
    }

    public String toString() {
        return this.cqlToken.toString();
    }

    public boolean hasNext() {
        return this.cqlToken.next != null;
    }

    public IToken next() {
        return newAdapterFor(this.cqlToken.next);
    }

    public int beginColumn() {
        return this.cqlToken.beginColumn;
    }

    public int endColumn() {
        return this.cqlToken.endColumn;
    }

    public Object getAdapted() {
        return this.cqlToken;
    }

}
