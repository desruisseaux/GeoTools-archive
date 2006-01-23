/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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

import org.geotools.event.AbstractGTComponent;
import org.geotools.event.GTList;
import org.geotools.filter.Filter;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;

// J2SE dependencies
import java.util.Arrays;
import java.util.List;


/**
 * Provides the default implementation of Rule.
 *
 * @author James Macgill
 * @source $URL$
 * @version $Id$
 */
public class RuleImpl extends AbstractGTComponent implements Rule, Cloneable {
    private List symbolizers = new GTList(this, "symbolizers");
    private List graphics = new GTList(this, "graphics");
    private String name = "name";
    private String title = "title";
    private String abstractStr = "Abstract";
    private Filter filter = null;
    private boolean hasElseFilter = false;
    private double maxScaleDenominator = Double.POSITIVE_INFINITY;
    private double minScaleDenominator = 0.0;

    /**
     * Creates a new instance of DefaultRule
     */
    protected RuleImpl() {
    }

    /**
     * Creates a new instance of DefaultRule
     *
     * @param symbolizers DOCUMENT ME!
     */
    protected RuleImpl(Symbolizer[] symbolizers) {
        this.symbolizers.addAll(Arrays.asList(symbolizers));
    }

    public Graphic[] getLegendGraphic() {
        return (Graphic[]) graphics.toArray(new Graphic[0]);
    }

    public void addLegendGraphic(Graphic graphic) {
        graphics.add(graphic);
    }

    /**
     * A set of equivalent Graphics in different formats which can be used as a
     * legend against features stylized by the symbolizers in this rule.
     *
     * @param graphics An array of Graphic objects, any of which can be used as
     *        the legend.
     */
    public void setLegendGraphic(Graphic[] graphics) {
        List graphicList = Arrays.asList(graphics);
    	
        this.graphics.clear();
        this.graphics.addAll(graphicList);
    }

    public void addSymbolizer(Symbolizer symb) {
        this.symbolizers.add(symb);
    }

    public void setSymbolizers(Symbolizer[] syms) {
        List symbols = Arrays.asList(syms);
        this.symbolizers.clear();
        this.symbolizers.addAll(symbols);
    }

    public Symbolizer[] getSymbolizers() {
        return (Symbolizer[]) symbolizers.toArray(new Symbolizer[symbolizers
            .size()]);
    }

    /**
     * Getter for property abstractStr.
     *
     * @return Value of property abstractStr.
     */
    public java.lang.String getAbstract() {
        return abstractStr;
    }

    /**
     * Setter for property abstractStr.
     *
     * @param abstractStr New value of property abstractStr.
     */
    public void setAbstract(java.lang.String abstractStr) {
        this.abstractStr = abstractStr;
        fireChanged();
    }

    /**
     * Getter for property name.
     *
     * @return Value of property name.
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     */
    public void setName(java.lang.String name) {
        this.name = name;
        fireChanged();
    }

    /**
     * Getter for property title.
     *
     * @return Value of property title.
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Setter for property title.
     *
     * @param title New value of property title.
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
        fireChanged();
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        Filter old = this.filter;
        this.filter = filter;
        fireChildChanged("filder", filter, old);
    }

    public boolean hasElseFilter() {
        return hasElseFilter;
    }

    public void setIsElseFilter(boolean flag) {
        hasElseFilter = flag;
        fireChanged();
    }

    public void setHasElseFilter() {
        hasElseFilter = true;
        fireChanged();
    }

    /**
     * Getter for property maxScaleDenominator.
     *
     * @return Value of property maxScaleDenominator.
     */
    public double getMaxScaleDenominator() {
        return maxScaleDenominator;
    }

    /**
     * Setter for property maxScaleDenominator.
     *
     * @param maxScaleDenominator New value of property maxScaleDenominator.
     */
    public void setMaxScaleDenominator(double maxScaleDenominator) {
        this.maxScaleDenominator = maxScaleDenominator;
        fireChanged();
    }

    /**
     * Getter for property minScaleDenominator.
     *
     * @return Value of property minScaleDenominator.
     */
    public double getMinScaleDenominator() {
        return minScaleDenominator;
    }

    /**
     * Setter for property minScaleDenominator.
     *
     * @param minScaleDenominator New value of property minScaleDenominator.
     */
    public void setMinScaleDenominator(double minScaleDenominator) {
        this.minScaleDenominator = minScaleDenominator;
        fireChanged();
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Creates a deep copy clone of the rule.
     *
     * @see org.geotools.styling.Rule#clone()
     */
    public Object clone() {
        try {
            RuleImpl clone = (RuleImpl) super.clone();
            clone.graphics = new GTList(clone, "graphics");
            clone.symbolizers = new GTList(clone, "symbolizers");
            clone.filter = filter; // TODO: we must duplicate!

            Graphic[] legends = new Graphic[graphics.size()];

            for (int i = 0; i < legends.length; i++) {
                Graphic legend = (Graphic) graphics.get(i);
                legends[i] = (Graphic) ((Cloneable) legend).clone();
            }

            clone.setLegendGraphic(legends);

            Symbolizer[] symbArray = new Symbolizer[symbolizers.size()];

            for (int i = 0; i < symbArray.length; i++) {
                Symbolizer symb = (Symbolizer) symbolizers.get(i);
                symbArray[i] = (Symbolizer) ((Cloneable) symb).clone();
            }

            clone.setSymbolizers(symbArray);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("This will never happen", e);
        }
    }

    /**
     * Generates a hashcode for the Rule.
     * 
     * <p>
     * For complex styles this can be an expensive operation since the hash
     * code is computed using all the hashcodes of the object within the
     * style.
     * </p>
     *
     * @return The hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        result = (PRIME * result) + symbolizers.hashCode();

        if (graphics != null) {
            result = (PRIME * result) + graphics.hashCode();
        }

        if (name != null) {
            result = (PRIME * result) + name.hashCode();
        }

        if (title != null) {
            result = (PRIME * result) + title.hashCode();
        }

        if (abstractStr != null) {
            result = (PRIME * result) + abstractStr.hashCode();
        }

        if (filter != null) {
            result = (PRIME * result) + filter.hashCode();
        }

        result = (PRIME * result) + (hasElseFilter ? 1 : 0);

        long temp = Double.doubleToLongBits(maxScaleDenominator);
        result = (PRIME * result) + (int) (temp >>> 32);
        result = (PRIME * result) + (int) (temp & 0xFFFFFFFF);
        temp = Double.doubleToLongBits(minScaleDenominator);
        result = (PRIME * result) + (int) (temp >>> 32);
        result = (PRIME * result) + (int) (temp & 0xFFFFFFFF);

        return result;
    }

    /**
     * Compares this Rule with another for equality.
     * 
     * <p>
     * Two RuleImpls are equal if all their properties are equal.
     * </p>
     * 
     * <p>
     * For complex styles this can be an expensive operation since it checks
     * all objects for equality.
     * </p>
     *
     * @param oth The other rule to compare with.
     *
     * @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof RuleImpl) {
            RuleImpl other = (RuleImpl) oth;

            return Utilities.equals(name, other.name)
            && Utilities.equals(title, other.title)
            && Utilities.equals(abstractStr, other.abstractStr)
            && Utilities.equals(filter, other.filter)
            && (hasElseFilter == other.hasElseFilter)
            && Utilities.equals(graphics, other.graphics)
            && Utilities.equals(symbolizers, other.symbolizers)
            && (Double.doubleToLongBits(maxScaleDenominator) == Double
            .doubleToLongBits(other.maxScaleDenominator))
            && (Double.doubleToLongBits(minScaleDenominator) == Double
            .doubleToLongBits(other.minScaleDenominator));
        }

        return false;
    }
}
