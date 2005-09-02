/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.model;

import java.util.Comparator;
import java.util.Iterator;

import org.geowidgets.framework.Res;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.*;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;

/** An entry for an EPSG object (such as f.e. an ellipsoid) that can be used
 * in a dropdown list. Later the user-selected object is retrieved, its code
 * can be read out and from that the object itself can be created. <p/>
 * This object supports <code>Comparable</code>, so entries can be sorted
 * alphabetically. Two alternative Comparators exist (by name and by code). <p/>
 * There is one special EPSGEntry, {@linkplain #OTHER}. This is a convenience
 * entry for dropdowns that allows the user to signal that a custom
 * object is to be used. This entry will always be sorted to the top. */
public class EPSGEntry implements Comparable<EPSGEntry> {
    /** "Custom ..." or a translation. Use this name for the choice entry
     * which will always get sorted to the top. */
    public static final String OTHER_NAME = Res.get(Res.CRS, "x.Custom"); //$NON-NLS-1$
    /** An EPSG entry in a dropdown list that a user can choose to signal
     * that he/she wants to use a custom object (not in the database). */
    public static final EPSGEntry OTHER = new EPSGEntry(" --- ", OTHER_NAME, true); //$NON-NLS-1$

    protected final String codeString;
    protected final Integer code;
    protected String name;
    protected boolean isCustom = false; //Flag for entries not in the database
    protected boolean isOther = false; //Flag for the "Custom..." entry

    /** @param o an CRS-related object for which to get an EPSG entry.
     * @return the EPSG entry for the given object. If the object has a code
     * a normal entry with code and name is returned. If no code is found
     * a custom EPSG entry (with name but no code) is returned, for which
     * <code>isCustom()</code> returns <code>true</code>. */
    public static EPSGEntry getEntryFor(final IdentifiedObject o) {
        String name = o.getName().getCode();
        Iterator<Identifier> it = o.getIdentifiers().iterator();
        if (!it.hasNext()) return new EPSGEntry(name);
        return new EPSGEntry(it.next().getCode(), name);
    }

    /** Constructor for custom entries (Objects not in the database).
     * Note that <b>no</b> check is performed whether there exists already
     * an object with this name in the database. 
     * @param name The name of an CRS-related object not in the EPSG database. */
    public EPSGEntry(final String name) {
        this.name = name;
        this.codeString = null;
        this.code = new Integer(-1);
        this.isCustom = true;
    }

    /** This constructor will fetch the object's name from the authority factory.
     * In case of an exception it will use the exception text as entry. 
     * @param code the EPSG code
     * @param factory an authority factory to derive the name from */
    public EPSGEntry(final String code, final AuthorityFactory factory) {
        this.codeString = code;
        this.code = new Integer(code);
        try {
            this.name = factory.getDescriptionText(code).toString();
        } catch (FactoryException e) {
            this.name = e.getLocalizedMessage();
        }
    }

    /** Use this constructor if the object's name is already known. 
     * @param code the EPSG code
     * @param name the object's name to display. This is usually the name
     * stored in the EPSG database, but need not necessary be this one. */
    public EPSGEntry(final String code, final String name) {
        this.codeString = code;
        this.code = new Integer(code);
        this.name = name;
    }

    /** Use this constructor to create the/an entry that signals to use
     * a custom object. You might use {@linkplain #OTHER} or create such
     * an entry with this constructor.
     * @param code the EPSG code
     * @param name the object's name to display. This is usually the name
     * stored in the EPSG database, but need not necessary be this one.
     * @param isOther if <code>true</code>, tells that this entry
     * is to be used to allow the user to signal that he/she wants to use
     * a custom object. */
    public EPSGEntry(final String code, final String name, boolean isOther) {
        this.codeString = code;
        this.code = (isOther) ? new Integer(-1) : new Integer(code);
        this.name = name;
        this.isOther = isOther;
        this.isCustom = isOther;
    }

    /** @return the EPSG object's code as String. */
    public String getCode() {
        return this.codeString;
    }

    /** @return the EPSG object's name. */
    public String getName() {
        return this.name;
    }

    /** @return the EPSG object's name. */
    public String toString() {
        return this.name;
    }

    /** @return <code>true</code> if the entry describes an object that is
     * not one of the database objects ... specifically if it has no code. <p/>
     * Usage example: <pre>
     * if (entry.isCustom) createObjectFromParameters(...);
     * else createObjectFromCode(entry.getCode()); </pre> <p/>
     * The if <code>entry.isOther()</code> returns <code>true</code>,
     * <code>entry.isCustom()</code> must return <code>true</code> as well.*/
    public boolean isCustom() {
        return this.isCustom;
    }

    /** @return <code>true</code> if the entry signals "Use a custom object"
     * and <code>false</code> if the entry directly describes an object.*/
    public boolean isOther() {
        return this.isOther;
    }

    /** Allows ordering of the entries, e.g. in a dropdown list. */
    public int compareTo(EPSGEntry entry) {
        return COMPARE_NAME.compare(this, entry);
    }

    /** Two entries are equal if and only if their EPSG code is equal.
     * The name doesn't matter. This means deliberately that all custom objects,
     * (non-EPSG objects) are equal since they have no code. */
    public boolean equals(Object o) {
        try {
            EPSGEntry entry = (EPSGEntry) o;
            return (this.codeString.equals(entry.codeString));
        } catch (ClassCastException e) {
            return false;
        }
    }

    //  Sorting
    /** Sorts EPSG entries by their name. */
    public static final Comparator<EPSGEntry> COMPARE_NAME = new NameComparator();
    /** Sorts EPSG entries by their code. Entries without code (custom entries)
     * are sorted to the top since they have code -1 internally. */
    public static final Comparator<EPSGEntry> COMPARE_CODE = new CodeComparator();

    /** Sorts the EPSG entries by their code, not as usual by their name. */
    public static class NameComparator implements Comparator<EPSGEntry> {
        public int compare(EPSGEntry entry1, EPSGEntry entry2) {
            if (entry1.isOther && entry2.isOther) return 0;
            else if (entry1.isOther) return -1;
            else if (entry2.isOther) return 1;
            else return entry1.name.compareTo(entry2.toString());
        }
    }

    /** Sorts the EPSG entries by their code, not as usual by their name. */
    public static class CodeComparator implements Comparator<EPSGEntry> {
        public int compare(EPSGEntry entry1, EPSGEntry entry2) {
            if (entry1.isOther && entry2.isOther) return 0;
            else if (entry1.isOther) return -1;
            else if (entry2.isOther) return 1;
            return entry1.code.compareTo(entry2.code);
        }
    }

}
