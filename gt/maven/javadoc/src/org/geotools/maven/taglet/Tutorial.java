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
package org.geotools.maven.taglet;

// J2SE dependencies
import java.util.Map;

// Standard JavaDoc dependencies
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;


/**
 * The <code>@tutorial</code> tag. This tag expects a link toward a tutorial page on the
 * Geotools wiki pages (Confluence).
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Tutorial implements Taglet {
    /**
     * Register this taglet.
     *
     * @param tagletMap the map to register this tag to.
     */
    public static void register(final Map tagletMap) {
       final Tutorial tag = new Tutorial();
       tagletMap.put(tag.getName(), tag);
    }

    /**
     * Constructs a default <code>@tutorial</code> taglet.
     */
    private Tutorial() {
        super();
    }

    /**
     * Returns the name of this custom tag.
     */
    public String getName() {
        return "tutorial";
    }

    /**
     * Returns {@code true} since <code>@tutorial</code> can be used in overview.
     */
    public boolean inOverview() {
        return true;
    }

    /**
     * Returns {@code true} since <code>@tutorial</code> can be used in package documentation.
     */
    public boolean inPackage() {
        return true;
    }

    /**
     * Returns {@code true} since <code>@tutorial</code> can be used in type documentation
     * (classes or interfaces).
     */
    public boolean inType() {
        return true;
    }

    /**
     * Returns {@code true} since <code>@tutorial</code> can be used in constructor
     */
    public boolean inConstructor() {
        return true;
    }

    /**
     * Returns {@code true} since <code>@tutorial</code> can be used in method documentation.
     */
    public boolean inMethod() {
        return true;
    }

    /**
     * Returns {@code true} since <code>@tutorial</code> can be used in field documentation.
     */
    public boolean inField() {
        return true;
    }

    /**
     * Returns {@code false} since <code>@tutorial</code> is not an inline tag.
     */
    public boolean isInlineTag() {
        return false;
    }

    /**
     * Given the <code>Tag</code> representation of this custom tag, return its string representation.
     * The default implementation invokes the array variant of this method.
     */
    public String toString(final Tag tag) {
        return toString(new Tag[] {tag});
    }

    /**
     * Given an array of {@code Tag}s representing this custom tag, return its string
     * representation.
     */
    public String toString(final Tag[] tags) {
        if (tags==null || tags.length==0) {
            return "";
        }
        final StringBuffer buffer = new StringBuffer("\n<DT><B>Tutorial:</B></DT>");
        for (int i=0; i<tags.length; i++) {
            final String url   = tags[i].text().trim();
            final String title = url.substring(url.lastIndexOf('/')+1).replace('+',' ');
            buffer.append('\n');
            buffer.append(i==0 ? "<DD>" : "    ");
            buffer.append("<A HREF=\"");
            buffer.append(url);
            buffer.append("\">");
            buffer.append(title);
            buffer.append("</A>");
        }
        buffer.append("</DD>\n");
        return buffer.toString();
    }
}
