/** This class stems from another library, published under LGPL. It should
 * at the current state not be considered part of the core GeoWidgets code. */
package org.MaBaUtils;

import java.util.*;

/** Contains conversion utilties <ul>
 * <li>Fast conversion between data types
 * <li>Useful string operations, e.g. getStringAfter(separator), trim with [space]
 * </ul>
 * @version 20050424 Matthias Basler: Updated to Java1.5, some functions removed
 * @version 20050728 isEmpty(String) moved to "Util"
 * @author  Matthias Basler
 */
public abstract class C {
//------------------------------------------------------------------------------
//Conversion
//------------------------------------------------------------------------------
    
//------------------------------------------------------------------------------
//Integer
    /** Converts a String to an int. If the String is "", 0 gets returned.*/
    public static int getint(String str) throws NumberFormatException{
        if (str.length() == 0) return 0;
        return Integer.parseInt(str);
    }
    
    /** Returns the int of the String. If this is not possible the errValue
     * is returned. */
    public static int getintX(String str, int errValue){
        try{
            return Integer.parseInt(str);
        } catch (Exception e){return errValue;}
    }
    
    /** Returns the Integer of the String. If this is not possible the errValue
     * is returned. */
    public static Integer getIntegerX(String str, int errValue){
        try{
            return new Integer(str);
        } catch (Exception e){return new Integer(errValue);}
    }
    
    /** Parses an array of Strings decribing floating point numbers.*/
    public static int[] getints(String[] str) throws NumberFormatException{
        int[] v = new int[str.length];
        for (int i = 0; i < str.length; ++i) v[i] = Integer.parseInt(str[i]);
        return v;
    }    
    /** Parses an array of Strings decribing floating point numbers.*/
    public static int[] getintsX(String[] str, int errValue){
        int[] v = new int[str.length];
        for (int i = 0; i < str.length; ++i) v[i] = getintX(str[i], errValue);
        return v;
    }
    
//------------------------------------------------------------------------------
//Double
    /** Converts a String to a double. If the String is "", 0.0 gets returned.*/
    public static double getdouble(String str) throws NumberFormatException{
        if (str.length() == 0) return 0.0;
        return Double.parseDouble(str.replace(',', '.'));
    }
    /** Returns the double of the String. If this is not possible the errValue
     * is returned. */
    public static double getdoubleX(String str, double errValue){
        try{
            return Double.parseDouble(str.replace(',', '.'));
        } catch (Exception e){return errValue;}
    }
    /** Returns the Double of the String. */
    public static Double getDouble(String str)  throws NumberFormatException{
        return new Double(str.replace(',', '.'));
    }
    /** Returns the Double of the String. If this is not possible the errValue
     * is returned. */
    public static Double getDoubleX(String str, double errValue){
        try{
            return new Double(str.replace(',', '.'));
        } catch (Exception e){return new Double(errValue);}
    }
    /** Parses an array of Strings decribing floating point numbers.*/
    public static double[] getdoubles(String[] str) throws NumberFormatException{
        double[] d = new double[str.length];
        for (int i = 0; i < str.length; ++i) d[i] = Double.parseDouble(str[i].replace(',', '.'));
        return d;
    }    
    /** Parses an array of Strings decribing floating point numbers.*/
    public static double[] getdoublesX(String[] str, double errValue){
        double[] d = new double[str.length];
        for (int i = 0; i < str.length; ++i) d[i] = getdoubleX(str[i].replace(',', '.'), errValue);
        return d;
    }
    
//------------------------------------------------------------------------------
//Float
    /** Converts a String to a float. If the String is "", 0.0 gets returned.*/
    public static float getfloat(String str) throws NumberFormatException{
        if (str.length() == 0) return 0f;
        return Float.parseFloat(str.replace(',', '.'));
    }
    /** Converts a String to a float. If this is not possible the errValue is returned.*/
    public static float getfloatX(String str, float errValue){
        try{
            if (str.length() == 0) return 0f;
            return Float.parseFloat(str.replace(',', '.'));
        } catch (Exception e){return errValue;}
    }    
    /** Converts a String to a float. If this is not possible the errValue is returned.*/
    public static Float getFloatX(String str, float errValue){
        try{
            if (str.length() == 0) return new Float(0f);
            return new Float(str.replace(',', '.'));
        } catch (Exception e){return new Float(errValue);}
    }
//------------------------------------------------------------------------------
//Short
    
    /** Converts a String to a short.*/
    public static short getshort(String str) throws NumberFormatException{
        if (str.length() == 0) return (new Integer(0)).shortValue();
        return new Short(str).shortValue();
    }
    /** Converts a String to a short. If this is not possible the errValue is returned.*/
    public static short getshortX(String str, short errValue){
        try{
            if (str.length() == 0) return 0;
            return Short.parseShort(str);
        } catch (Exception e){return errValue;}
    }
          /** Converts a String to a Short. If this is not possible the errValue is returned.*/
    public static Short getShortX(String str, short errValue){
        try{
            if (str.length() == 0) return new Short((short)0);
            return new Short(str);
        } catch (Exception e){return new Short(errValue);}
    }
//------------------------------------------------------------------------------
//Conversion from object
//------------------------------------------------------------------------------
    /** Converts an object containing any integer number to an int. */
    public static int getint(Object obj) throws NumberFormatException{
        if (obj instanceof String) return Integer.parseInt((String)obj);
        else if (obj instanceof Long) return (int)((Long)obj).longValue();
        else if (obj instanceof Integer) return ((Integer)obj).intValue();
        else if (obj instanceof Short) return ((Short)obj).shortValue();
        else if (obj instanceof Byte) return ((Byte)obj).byteValue();
        throw new NumberFormatException();
    }
    /** Converts an object containing any integer number to a long. */
    public static long getlong(Object obj) throws NumberFormatException{
        if (obj instanceof String) return Long.parseLong((String)obj);
        else if (obj instanceof Long) return ((Long)obj).longValue();
        else if (obj instanceof Integer) return ((Integer)obj).intValue();
        else if (obj instanceof Short) return ((Short)obj).shortValue();
        else if (obj instanceof Byte) return ((Byte)obj).byteValue();
        throw new NumberFormatException();
    }
    /** Converts an object containing any integer number to an short. */
    public static short getshort(Object obj) throws NumberFormatException{
        if (obj instanceof String) return Short.parseShort(((String)obj).replace(',', '.'));
        else if (obj instanceof Long) return (short)((Long)obj).longValue();
        else if (obj instanceof Integer) return (short)((Integer)obj).intValue();
        else if (obj instanceof Short) return ((Short)obj).shortValue();
        else if (obj instanceof Byte) return ((Byte)obj).byteValue();
        throw new NumberFormatException();
    }
    /** Converts an object containing any integer number to an short. */
    public static short getbyte(Object obj) throws NumberFormatException{
        if (obj instanceof String) return Byte.parseByte((String)obj);
        else if (obj instanceof Long) return (byte)((Long)obj).longValue();
        else if (obj instanceof Integer) return (byte)((Integer)obj).intValue();
        else if (obj instanceof Short) return (byte)((Short)obj).shortValue();
        else if (obj instanceof Byte) return ((Byte)obj).byteValue();
        throw new NumberFormatException();
    }
    /** Converts an object containing a floating point number to a double.*/
    public static double getdouble(Object obj) throws NumberFormatException{
        if (obj instanceof String) return Double.parseDouble(((String)obj).replace(',', '.'));
        else if (obj instanceof Double) return ((Double)obj).doubleValue();
        else if (obj instanceof Float) return ((Float)obj).floatValue();
        throw new NumberFormatException();
    }    
    /** Converts an object containing a floating point number to a float.*/
    public static float getfloat(Object obj) throws NumberFormatException{
        if (obj instanceof String) return Float.parseFloat((String)obj);
        else if (obj instanceof Double) return (float)((Double)obj).doubleValue();
        else if (obj instanceof Float) return ((Float)obj).floatValue();
        throw new NumberFormatException();
    }
     
    /** Converts an object containing a Boolean to boolean.*/
    public static boolean getbool(Object obj) throws NumberFormatException{
        if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        throw new NumberFormatException();
    }
//------------------------------------------------------------------------------
//Conversion from key-value string
//------------------------------------------------------------------------------
        /** Returns the value from a String like "ROWS = 50" or "LAYOUT BIL" */
    public static String getValueString(String keyValueMapping, char delimiter){
        String after = getStringAfter(keyValueMapping, delimiter);
        return trimLeft(after);
    }
    /** Returns the value from a String like "ROWS = 50" or "NBITS 16" */
    public static int getValueint(String keyValueMapping, char delimiter){
        String after = getStringAfter(keyValueMapping, delimiter);
        return new Integer(trimLeft(after)).intValue();
    }
    /** Returns the value from a String like "XDIM = 0.008333" */
    public static float getValuefloat(String keyValueMapping, char delimiter){
        String after = getStringAfter(keyValueMapping, delimiter);
        return new Float(trimLeft(after)).floatValue();
    }
//------------------------------------------------------------------------------
//Conversion from lists
//------------------------------------------------------------------------------
    /** Converts a list containing Strings into an array of Strings. */
    public static String[] getStringsFromList(List<String> list){
        return list.toArray(new String[list.size()]);
    }
    /** Converts a list containing doubles into an array of Strings. */
    public static Double[] getdoublesFromList(List<Double> list){
        return list.toArray(new Double[list.size()]);
    }
    /** Converts a list containing floats into an array of Strings. */
    public static Float[] getfloatsFromList(List<Float> list){
        return list.toArray(new Float[list.size()]);
    }
    /** Converts a list containing ints into an array of Strings. */
    public static Integer[] getintsFromList(List<Integer> list){
        return list.toArray(new Integer[list.size()]);
    }
    /** Converts a list containing ints into an array of Strings. */
    public static Short[] getshortsFromList(List<Short> list){
        return list.toArray(new Short[list.size()]);
    }
    /** Converts a list containing ints into an array of Strings. */
    public static Long[] getlongsFromList(List<Long> list){
        return list.toArray(new Long[list.size()]);
    }
    /** Converts a list containing ints into an array of Strings. */
    public static Boolean[] getboolsFromList(List<Boolean> list){
        return list.toArray(new Boolean[list.size()]);
    }
    
    
    /** Converts a list of Strings into an array of Strings. */
    public static String[] getStringsFromStringList(List<String> listOfStrings){
        String[] str = new String[listOfStrings.size()];
        for (int i=0; i<listOfStrings.size();++i)
          str[i] = listOfStrings.get(i);
        return str;
    }
    /** Converts a list of Strings into an array of doubles. */
    public static double[] getdoublesFromStringList(List<String> listOfStrings){
        double[] doubles = new double[listOfStrings.size()];
        for (int i=0; i<listOfStrings.size();++i)
          doubles[i] = Float.parseFloat(listOfStrings.get(i));
        return doubles;        
    }
    /** Converts a list of Strings into an array of floats. */
    public static float[] getfloatsFromStringList(List<String>  listOfStrings){
        float[] floats = new float[listOfStrings.size()];
        for (int i=0; i<listOfStrings.size();++i)
          floats[i] = Float.parseFloat(listOfStrings.get(i));
        return floats;        
    }
    /** Converts a list of Strings into an array of ints. */
    public static int[] getintsFromStringList(List<String> listOfStrings){
        int[] ints = new int[listOfStrings.size()];
        for (int i=0; i<listOfStrings.size();++i)
          ints[i] = Integer.parseInt(listOfStrings.get(i));
        return ints;        
    }
    /** Converts a list of Strings into an array of shorts. */
    public static short[] getshortsFromStringList(List<String> listOfStrings){
        short[] shorts = new short[listOfStrings.size()];
        for (int i=0; i<listOfStrings.size();++i)
          shorts[i] = Short.parseShort(listOfStrings.get(i));
        return shorts;
    }
    /** Converts a list of Strings into an array of longs. */
    public static long[] getlongsFromStringList(List<String> listOfStrings){
        long[] longs = new long[listOfStrings.size()];
        for (int i=0; i<listOfStrings.size();++i)
          longs[i] = Long.parseLong(listOfStrings.get(i));
        return longs;
    }
    
//------------------------------------------------------------------------------
//Conversion from maps
//------------------------------------------------------------------------------    
    /** Converts a list containing Strings into an array of Strings. */
    public static String[] getStringsFromMap(Map<Object,String> map){
        return map.values().toArray(new String[map.size()]);
    }
    /** Converts a list containing doubles into an array of Strings. */
    public static Double[] getdoublesFromMap(Map<Object,Double> map){
        return map.values().toArray(new Double[map.size()]);
    }
    /** Converts a list containing floats into an array of Strings. */
    public static Float[] getfloatsFromMap(Map<Object,Float> map){
        return map.values().toArray(new Float[map.size()]);
    }
    /** Converts a list containing ints into an array of Strings. */
    public static Integer[] getintsFromMap(Map<Object,Integer> map){
        return map.values().toArray(new Integer[map.size()]);
    }
    /** Converts a list containing ints into an array of Strings. */
    public static Short[] getshortsFromMap(Map<Object,Short> map){
        return map.values().toArray(new Short[map.size()]);
    }
    /** Converts a list containing ints into an array of Strings. */
    public static Long[] getlongsFromMap(Map<Object,Long> map){
        return map.values().toArray(new Long[map.size()]);
    }
    /** Converts a list containing ints into an array of Strings. */
    public static Boolean[] getboolsFromMap(Map<Object,Boolean> map){
        return map.values().toArray(new Boolean[map.size()]);
    }
    
//------------------------------------------------------------------------------
//Conversion between collections
//------------------------------------------------------------------------------

    /** Builds a HashSet with the contents of the given String array.*/
    public static HashSet<String> createHashSetFromArray(String[] str){
        HashSet<String> set = new HashSet<String>(str.length);
        for (int i=0; i< str.length; ++i) set.add(str[i]);
        return set;
    }
    /** Builds a HashSet with the contents of the given integer array.*/
    public static HashSet<Integer> createHashSetFromArray(int[] ints){
        HashSet<Integer> set = new HashSet<Integer>(ints.length);
        for (int i=0; i< ints.length; ++i) set.add(new Integer(ints[i]));
        return set;
    }
    /** Builds a HashSet with the contents of the given double array.*/
    public static HashSet<Double> createHashSetFromArray(double[] d){
        HashSet<Double> set = new HashSet<Double>(d.length);
        for (int i=0; i< d.length; ++i) set.add(new Double(d[i]));
        return set;
    }
    /** Creates a Hashmap with the Strings given as <code>str</code> as key and
     * their corresponding Strings from a resource bundle as values.
     */
    public static HashMap<String,String> createHashMapFromArray(String resourceBundle, String[] str) throws MissingResourceException{
        ResourceBundle rb = ResourceBundle.getBundle(resourceBundle);
        for (int i=0; i< str.length; ++i){
            String s = rb.getString(str[i]);
            if (s == null || s.equals("")) //$NON-NLS-1$
              throw new MissingResourceException("Resource missing.", resourceBundle, str[i]); //$NON-NLS-1$
        }
        HashMap<String,String> map = new HashMap<String,String>(str.length);
        for (int i=0; i< str.length; ++i) map.put(str[i], rb.getString(str[i]));
        return map;
    }
//------------------------------------------------------------------------------
//String operations
//------------------------------------------------------------------------------
    /** Returns a part of a String.
     * For example getString("incarnation", 2, 3) returns "car".
     * If the requested length exeeds the String's length, the String is
     * returned until the end.
     */
    public static String getString(String str, int start, int length) throws IndexOutOfBoundsException{
        if (start < 1) start = 1;
        if (start + length >= str.length())
            return str.substring(start).trim();
        else
            return str.substring(start, start+length).trim();
    }
    
    /** Returns a section of a String that contains a value as double. 
     * The subset gets trimmed before being converted to double. For
     * example <code> getDouble("ASDF123.4567  ZYX", 4,10)</code> would return
     * <code>123.4567</code> as double value.
     * @param str the string that contains the value as text
     * @param start the first character that is part of the number. Please
     * note that the String starts with character #0.
     * @param length the number of characters belonging to the value
     */
    public static double getdouble(String str, int start, int length) throws IndexOutOfBoundsException, NumberFormatException{
        if (start < 1) start = 1;
        if (start+length >= str.length())
            return (new Double(str.substring(start).trim())).doubleValue();
        else 
            return (new Double(str.substring(start, start+length).trim())).doubleValue();
    }
    
    /** Returns a section of a String that contains an integer. 
     * The subset gets trimmed before being converted to double. For
     * example <code> getDouble("ASDF1234567  ZYX", 4,8)</code> would return
     * <code>1234567</code> as double value.
     * @param str the string that contains the value as text
     * @param start the first character that is part of the number. Please
     * note that the String starts with character #0.
     * @param length the number of characters belonging to the value
     */
    public static int getint(String str, int start, int length) throws IndexOutOfBoundsException, NumberFormatException{
        if (start < 1) start = 1;
        if (start+length >= str.length())
            return (Integer.parseInt(str.substring(start).trim()));
        else 
            return (Integer.parseInt(str.substring(start, start+length).trim()));
    }
    /** Returns a section of a String that contains a short. 
     * @see <code>getint</code> for details.*/
    public static short getshort(String str, int start, int length) throws IndexOutOfBoundsException, NumberFormatException{
        if (start < 1) start = 1;
        if (start+length >= str.length())
            return (Short.parseShort(str.substring(start).trim()));
        else 
            return (Short.parseShort(str.substring(start, start+length).trim()));
    }
      
    /** Returns the part of a String until the first appearance of a character.
     * If the character is not in the String, the whole String is returned.
     * @param string the input String
     * @param searchChar the character that defines the end of the result string.*/
    public static String getStringUntil(String string, char searchChar){
        if (string.indexOf(searchChar) == -1) return string;
        else return string.substring(0, string.indexOf(searchChar) + 0);
    }
    /** Returns the part of a String until the first appearance of a character
     * behind the position specified by fromIndex.
     * If the character is not in the String, the whole String is returned.
     * @param string the input String
     * @param searchChar the character that defines the end of the result string.
     * @param fromIndex if the searchChar occurres before this index this is ignored.
     * @param include specifies if the result string should include
     * the searchChar as its last character. */
    public static String getStringUntil(String string, char searchChar, int fromIndex, boolean include){
        if (string.indexOf(searchChar, fromIndex) == -1) return string;
        else return string.substring(0, string.indexOf(searchChar, fromIndex) + ((include)? 1 : 0));
    }
    /** Returns the part of a String starting after the first appearance of a
     * character. If the character is not in the String, "" is returned.
     * @param string the input String
     * @param searchChar the character after which the result string starts.*/
    public static String getStringAfter(String string, char searchChar){
        if (string.indexOf(searchChar) == -1) return ""; //$NON-NLS-1$
        else return string.substring(string.indexOf(searchChar) + 1);
    }
    /** Returns the part of a String starting after the first appearance of a
     * character behind the position specified by fromIndex.
     * If the character is not in the String, "" is returned.
     * @param string the input String
     * @param searchChar the character that defines the start of the result string.
     * @param fromIndex if the searchChar occurres before this index this is ignored.
     * @param include specifies if the result string should include
     * the searchChar as its first character. */
    public static String getStringAfter(String string, char searchChar, int fromIndex, boolean include){
        if (string.indexOf(searchChar, fromIndex) == -1) return ""; //$NON-NLS-1$
        else return string.substring(string.indexOf(searchChar, fromIndex) + ((include)? 0 : 1));
    }
    
//------------------------------------------------------------------------------
//String operations - others
//------------------------------------------------------------------------------   
    /** Deletes all leading whitespaces (e.g. ASCII 0, " ").*/
    public static String trimLeft(String str){
        if ( str.length() == 0) return ""; //$NON-NLS-1$
        StringBuffer s = new StringBuffer(str);
        while (s.length()>0 && Character.isWhitespace(s.charAt(0)))
            s.deleteCharAt(0);
        return s.toString();        
    }
    /** Deletes all trailing whitespaces (e.g. ASCII 0, " ").*/
    public static String trimRight(String str){
        if ( str.length() == 0) return ""; //$NON-NLS-1$
        StringBuffer s = new StringBuffer(str);
        boolean isWhitespace = Character.isWhitespace(s.charAt(s.length()-1));
        while (isWhitespace){
            s.deleteCharAt(s.length()-1);
            if (s.length() == 0) break;
            isWhitespace = Character.isWhitespace(s.charAt(s.length()-1));
        }
        return s.toString();        
    }
    
    /** Deletes all leading and trailing whitespaces (e.g. ASCII 0, " ").*/
    public static String trim(String str){
        return trimLeft(trimRight(str));
    }
    
    
    /** Removes the last character from a String */
    public static String removeLastChar(String str){
        return str.substring(0, str.length()-1);
    }
    
    /** Creates a String with the length len filled with the character c. */
    public static String createString(char c, int len){
        StringBuffer b = new StringBuffer();
        for (int i=0; i<len; ++i) b.append(c);
        return b.toString();
    }
    
    /** Reads a string and stores all its tokens (separated by delim)
     * into a String array. */
    public static String[] tokenize(String str, String delim){
        StringTokenizer tok = new StringTokenizer(str, delim);
        int count = tok.countTokens();
        String[] s = new String[count];
        for (int i = 0; i < count; ++i) s[i] = tok.nextToken();
        return s;
    }
    
    /** Converts an array of Strings to one large string
     * by inserting the given delimiter between them..
     * @param delim the delimiter to insert between the Strings
     * of the source array. Use "\n" to create a multi-line string.
     * @see concatenate(String[] linesOfText)
     */
    public static String concatenate(String[] str, String delim){
        if (str.length<1) return ""; //$NON-NLS-1$
        StringBuffer b = new StringBuffer(str[0]);
        for (int i = 1; i < str.length; ++i) b.append(delim).append(str[i]);
        return b.toString();
    }
    
    /** Returns a multi-line String from an array of single lines.
     * @see concatenate(String[] str, String delim)
     */
    public static String concatenate(String[] linesOfText){
        return concatenate(linesOfText, "\n"); //$NON-NLS-1$
    }

    /** Returns how often the <code>searchString</code> is included in
     * <code>string</code>. */
    public static int getHowOftenFound(String string, String searchString){
        int found = 0;
        int index = 0;
        int i = string.indexOf(searchString, index);
        while (i != -1){
            ++found;
            index = i + searchString.length();
            i = string.indexOf(searchString, index);
        }
        return found;
    }
}
