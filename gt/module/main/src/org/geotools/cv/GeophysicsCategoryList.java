/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cv;

// J2SE dependencies
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.AbstractList;
import java.util.Locale;

import org.geotools.resources.Utilities;
import org.geotools.resources.XMath;
import org.geotools.units.Unit;


/**
 * An immutable list of geophysics category. Elements are usually (but not always) instances
 * of [@link GeophysicsCategory}. Exception to this rule includes categories wrapping an
 * identity transforms.
 *
 * This list can transform geophysics values into sample values using
 * the list of {@link Category}. This transform is thread safe if each
 * {@link Category#getSampleToGeophysics} transform is thread-safe too.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by {@link org.geotools.coverage.GeophysicsCategoryList}
 *             in the <code>org.geotools.coverage</code> package.
 */
final class GeophysicsCategoryList extends CategoryList {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 98602310176453958L;

    /**
     * Maximum value for {@link #ndigits}. This is the number of
     * significant digits to allow when formatting a geophysics value.
     */
    private static final int MAX_DIGITS = 6;
    
    /**
     * Unit�s des mesures g�ophysiques repr�sent�es par les cat�gories.
     * Ce champ peut �tre nul s'il ne s'applique pas ou si les unit�s
     * ne sont pas connues.
     */
    private final Unit unit;
    
    /**
     * Nombre de chiffres significatifs apr�s la virgule.
     * Cette information est utilis�e pour les �critures
     * des valeurs g�ophysiques des cat�gories.
     */
    private final int ndigits;
    
    /**
     * Locale used for creating {@link #format} last time.
     * May be <code>null</code> if default locale was requested.
     */
    private transient Locale locale;
    
    /**
     * Format � utiliser pour �crire les
     * valeurs g�ophysiques des th�mes.
     */
    private transient NumberFormat format;
    
    /**
     * Objet temporaire pour {@link NumberFormat}.
     */
    private transient FieldPosition dummy;

    /**
     * Construct a category list using the specified array of categories.
     *
     * @param  categories The list of categories. Elements should be
     *                    instances of {@link GeophysicsCategory}
     *                    (most of the time, but not always).
     * @param  unit       The unit information for all quantitative categories.
     *                    May be <code>null</code> if no category has units.
     * @param inverse     The {@link CategoryList} which is constructing this
     *                    {@link GeophysicsCategoryList}.
     *
     * @throws IllegalArgumentException if two or more categories
     *         have overlapping sample value range.
     */
    GeophysicsCategoryList(Category[] categories, final Unit unit, final CategoryList inverse) {
        super(categories, unit, true, inverse);
        this.unit    = unit;
        this.ndigits = getFractionDigitCount(categories);
        assert isScaled(true);
    }
    
    /**
     * Compute the smallest number of fraction digits necessary to resolve all
     * quantitative values. This method assume that geophysics values in the range
     * <code>Category.geophysics(true).getRange</code> are stored as integer sample
     * values in the range <code>Category.geophysics(false).getRange</code>.
     */
    private static int getFractionDigitCount(final Category[] categories) {
        int ndigits = 0;
        final double EPS = 1E-6;
        for (int i=0; i<categories.length; i++) {
            final Category geophysics = categories[i].geophysics(true);
            final Category samples    = categories[i].geophysics(false);
            final double ln = XMath.log10((geophysics.maximum - geophysics.minimum)/
                                          (   samples.maximum -    samples.minimum));
            if (!Double.isNaN(ln)) {
                final int n = -(int)(Math.floor(ln + EPS));
                if (n>ndigits) {
                    ndigits = Math.min(n, MAX_DIGITS);
                }
            }
        }
        return ndigits;
    }

    /**
     * If <code>toGeophysics</code> is <code>false</code>, cancel the action of a previous
     * call to <code>geophysics(true)</code>. This method always returns a list of categories
     * in which <code>{@link Category#geophysics(boolean) Category.geophysics}(toGeophysics)</code>
     * has been invoked for each category.
     */
    public CategoryList geophysics(final boolean toGeophysics) {
        final CategoryList scaled = toGeophysics ? this : inverse;
        assert scaled.isScaled(toGeophysics);
        return scaled;
    }
    
    /**
     * Returns the unit information for quantitative categories in this list.
     * May returns <code>null</code>  if there is no quantitative categories
     * in this list, or if there is no unit information.
     */
    public Unit getUnits() {
        return unit;
    }
    
    /**
     * Formatte la valeur sp�cifi�e selon les conventions locales. Le nombre sera
     * �crit avec un nombre de chiffres apr�s la virgule appropri� pour la cat�gorie.
     * Le symbole des unit�s sera ajout� apr�s le nombre si <code>writeUnit</code>
     * est <code>true</code>.
     *
     * @param  value Valeur du param�tre g�ophysique � formatter.
     * @param  writeUnit Indique s'il faut �crire le symbole des unit�s apr�s le nombre.
     *         Cet argument sera ignor� si aucune unit� n'avait �t� sp�cifi�e au constructeur.
     * @param  locale Conventions locales � utiliser, ou <code>null</code> pour les conventions par
     *         d�faut.
     * @param  buffer Le buffer dans lequel �crire la valeur.
     * @return Le buffer <code>buffer</code> dans lequel auront �t� �crit la valeur et les unit�s.
     */
    synchronized StringBuffer format(final double value, final boolean writeUnits,
                                     final Locale locale, StringBuffer buffer)
    {
        if (format==null || !Utilities.equals(this.locale, locale)) {
            this.locale = locale;
            format=(locale!=null) ? NumberFormat.getNumberInstance(locale) :
                                    NumberFormat.getNumberInstance();
            format.setMinimumFractionDigits(ndigits);
            format.setMaximumFractionDigits(ndigits);
            dummy = new FieldPosition(0);
        }
        buffer = format.format(value, buffer, dummy);
        if (writeUnits && unit!=null) {
            final int position = buffer.length();
            buffer.append('\u00A0'); // No-break space
            buffer.append(unit);
            if (buffer.length() == position+1) {
                buffer.setLength(position);
            }
        }
        return buffer;
    }
    
    /**
     * Compares the specified object with this category list for equality.
     * If the two objects are instances of {@link CategoryList}, then the
     * test is a little bit stricter than the default {@link AbstractList#equals}.
     */
    public boolean equals(final Object object) {
        if (object instanceof GeophysicsCategoryList) {
            final GeophysicsCategoryList that = (GeophysicsCategoryList) object;
            return this.ndigits == that.ndigits &&
                   Utilities.equals(this.unit, that.unit) &&
                   super.equals(that);
        }
        return ndigits==0 && unit==null && super.equals(object);
    }
}
