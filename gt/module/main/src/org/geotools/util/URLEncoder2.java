/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.util;

// J2SE dependencies
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Utility method for URL encoding.
 *
 * @version $Id$
 * @author Richard Gould
 *
 * @see URLEncoder
 *
 * @todo Should it belong to the <code>org.geotools.io</code> package instead?
 */
public class URLEncoder2 {

    /**
     * The list of characters to be encoded
     */
    protected static char[] validChars = { ' ' };


    /**
     * This code behaves exactly like {@link URLEncoder#encode(String, String)} except
     * that it encodes ONLY spaces:
     * 
     * If there is demand, this could be extended to encode characters on demand 
     * 
     * @param str the string to be encoded
     * @param encoding the type of encoding to use
     * @return the encoded string
     * @throws UnsupportedEncodingException if encoding is not valid
     * @see URLEncoder#encode(String, String)
     *
     * @todo Use of += operation on {@link String} objects is inefficient.
     *       Implementation should use {@link StringBuffer} instead.
     */
    public static String encode (String str, String encoding) throws UnsupportedEncodingException {
        if (str == null) {
            return null;
        }

        String results = "";

        for (int i = 0; i < str.length(); i++) {
            if (needsEncoding(str.charAt(i))) {
                results += URLEncoder.encode(""+str.charAt(i), encoding);
            } else {
                results += str.charAt(i);
            }
        }

        return results;
    }

    /**
     * Returns <code>true</code> if the specified character need to be encoded.
     */
    protected static boolean needsEncoding(char c) {
        for (int i = 0; i < validChars.length; i++) {
            if (validChars[i] == c) {
                return true;
            }
        }
        return false;
    }
}
