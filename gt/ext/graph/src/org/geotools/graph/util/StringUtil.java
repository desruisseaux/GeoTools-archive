/*
 $Id: StringUtil.java,v 1.1 2004/04/19 15:29:38 jdeolive Exp $
  
  Copyright (c) 2003, Ministry of Sustainable Resource Management
   Government of British Columbia, Canada

   All rights reserved.
   This information contained herein may not be used in whole
   or in part without the express written consent of the
   Government of British Columbia, Canada.
  
*/

package org.geotools.graph.util;

/**
 *  Various string utilities. 
 */
public class StringUtil {
	
  /**
   * Strips any white space of the end of a string.
   */
  public static String strip(String s) {
    StringBuffer sb = new StringBuffer();
    int spaces = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == ' ') {
        if (spaces > 0) return(sb.toString());
        else spaces++;
      }
      else {
       sb.append(s.charAt(i));
       spaces = 0;
      }
    }
    return(sb.toString());
  }
  
  /**
   * Removes any spaces within a string.
   */
  public static String noSpaces(String s) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) != ' ') sb.append(s.charAt(i));    
    }
    
    return(sb.toString());
  }
  
  /**
   * Places a post fix on a filename just before the extension dot.
   */
  public static String postfixFilename(String filename, String postfix) {
  	int index = filename.lastIndexOf(".");
	return(filename.substring(0, index) + postfix + filename.substring(index));
  }

  /**
   * Places a prefix on the filename.
   * <br><br>
   * Note: Only works for windows style paths. 
   */
  public static String prefixFilename(String path, String prefix) {
    int index = path.lastIndexOf("\\");
    return (path.substring(0, index) + "\\" + prefix + path.substring(index+1));
  }

  /**
   * Places an extension on a filename if it does not already exist.
   * @param filename Path to the file.
   * @param ext Extension of the file.
   */
  public static String addExtIfNecessary(String filename, String ext) {
    if (hasExtension(filename, ext)) return(filename);
    return(setExtension(filename, ext));
  }
  
  public static String setExtension(String filename, String ext) {
    if (hasExtension(filename, ext)) return(filename);
    int index = filename.lastIndexOf(".");
    if (index == -1) return(filename + "." + ext);
    return(filename.substring(0, index+1) + ext);
      
  }
  
  public static boolean hasExtension(String filename, String ext) {
    int index = filename.lastIndexOf(".");
    if (index == -1) return(false);
    return(filename.substring(index+1).compareTo(ext) == 0);   
  }
  
  public static String stripPath(String filename) {
    int index = filename.lastIndexOf("\\");
    if (index == -1) return(filename);
    return(filename.substring(index+1));
  }
  
  public static String stripExtension(String filename) {
    int index = filename.lastIndexOf(".");
    return(filename.substring(0, index));
  }
  
  
}
