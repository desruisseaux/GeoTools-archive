/*
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Center for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.styling;

/**
 * How to style a feature type.  This is introduced as a convenient package
 * that can be used independently for feature types, for example in
 * GML Default Styling.  The "layer" concept is discarded inside of this
 * element and all processing is relative to feature types.
 * The FeatureTypeName is allowed to be optional, but only one feature
 * type may be in context and it must match the syntax and semantics of all
 * attribute references inside of the FeatureTypeStyle.
 *
 * @version $Id: FeatureTypeStyle.java,v 1.6 2002/07/12 15:26:42 loxnard Exp $
 * @author James Macgill, CCG
 */
public interface FeatureTypeStyle {
    //public String getName();
    //public String getTitle();
    //public String getAbstract();
    
    /**
     * Only features with the type name returned by this method should
     * be styled by this feature type styler.
     * @return the name of types that this styler applies to
     */
    String getFeatureTypeName();
    
    /**
     * Sets the type name of the features that this styler should be
     * applied to.
     * TODO: should a set method be declared in this interface at all?
     * @param name The TypeName of the features to be styled by this instance.
     */
    void setFeatureTypeName(String name);
    
    /**
     * The SemanticTypeIdentifiers is experimental and is intended to be used
     * to identify, using a community-controlled name(s), what the style is 
     * suitable to be used for.
     * For example, a single style may be suitable to use with many
     * different feature types.  The syntax of the SemanticTypeIdentifiers
     * string is undefined, but the strings "generic:line_string",
     * "generic:polygon", "generic:point", "generic:text",
     * "generic:raster", and "generic:any" are reserved to indicate
     * that a FeatureTypeStyle may be used with any feature type
     * with the corresponding default geometry type (i.e., no feature 
     * properties are referenced in the feature type style).
     * 
     * TODO: Method name contains typo.  Should be getSemanticTypeIdentifiers
     *
     * @return An array of strings representing systematic types which
     *         could be styled by this instance.
     **/
    String[] getSymantecTypeIdentifiers();
    
    /**
     * Rules govern the appearance of any given feature to be styled by
     * this styler.  Each rule contains conditions based on scale and
     * feature attribute values.  In addition, rules contain the symbolizers
     * which should be applied when the rule holds true.
     *
     * @return The full set of rules contained in this styler.
     */
    Rule[] getRules();
}

