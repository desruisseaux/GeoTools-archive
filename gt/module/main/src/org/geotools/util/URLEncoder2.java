package org.geotools.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class URLEncoder2 {
	
	/**
	 * The list of characters to be encoded
	 */
	protected static char[] validChars = { ' ' };
	
	/**
	 * This code behaves exactly like java.net.URLEncoder(String, String) except
	 * that it encodes ONLY spaces:
	 * 
	 * If there is demand, this could be extended to encode characters on demand 
	 * 
	 * @see java.net.URLEncoder.encode(String, String)
	 * @param str the string to be encoded
	 * @param encoding the type of encoding to use
	 * @return the encoded string
	 * @throws UnsupportedEncodingException if encoding is not valid
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
	
	protected static boolean needsEncoding(char c) {
		for (int i = 0; i < validChars.length; i++) {
			if (validChars[i] == c) {
				return true;
			}
		}
		return false;
	}
}