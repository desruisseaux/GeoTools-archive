/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.resources.Utilities;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.util.Cloneable;

// J2SE dependencies
import java.util.ArrayList;
import java.util.Iterator;


/**
 * DOCUMENT ME!
 *
 * @author Ian Turton, CCG
 * @source $URL$
 * @version $Id$
 */
public class GraphicImpl extends AbstractGTComponent implements Graphic,
    Cloneable {
    /** The logger for the default core module. */
    private static final java.util.logging.Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.core");
    private FilterFactory filterFactory;
    private String geometryPropertyName = "";
    private java.util.List externalGraphics = new java.util.ArrayList();
    private java.util.List marks = new java.util.ArrayList();
    private java.util.List symbols = new java.util.ArrayList();
    private Expression rotation = null;
    private Expression size = null;
    private Displacement displacement = null;
    private Expression opacity = null;

    /**
     * Creates a new instance of DefaultGraphic
     */
    protected GraphicImpl() {
        this( CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints())); 
    }

    public GraphicImpl(FilterFactory factory) {
        filterFactory = factory;
    }

    public void setFilterFactory(FilterFactory factory) {
        filterFactory = factory;
    }

    /**
     * Convenience method for logging a message with an exception.
     *
     * @param method the name of the calling method
     * @param message the error message
     * @param exception The exception thrown
     */
    private static void severe(final String method, final String message,
        final Exception exception) {
        final java.util.logging.LogRecord record = new java.util.logging.LogRecord(java.util.logging.Level.SEVERE,
                message);
        record.setSourceMethodName(method);
        record.setThrown(exception);
        LOGGER.log(record);
    }

    /**
     * Provides a list of external graphics which can be used to represent this
     * graphic. Each one should be an equivalent representation but in a
     * different format. If none are provided, or if none of the formats are
     * supported, then the list of Marks should be used instead.
     *
     * @return An array of ExternalGraphics objects which should be equivalents
     *         but in different formats.  If null is returned use getMarks
     *         instead.
     */
    public ExternalGraphic[] getExternalGraphics() {
        ExternalGraphic[] ret = null;

        if (externalGraphics.size() > 0) {
            ret = (ExternalGraphic[]) externalGraphics.toArray(new ExternalGraphic[0]);
        }

        return ret;
    }

    public void setExternalGraphics(ExternalGraphic[] externalGraphics) {
        this.externalGraphics.clear();

        for (int i = 0; i < symbols.size();) {
            Object symbol = symbols.get(i);

            if (symbol instanceof ExternalGraphic) {
                symbols.remove(i);
            } else {
                i++;
            }
        }

        if (externalGraphics != null) {
            for (int i = 0; i < externalGraphics.length; i++) {
                addExternalGraphic(externalGraphics[i]);
            }
        }

        fireChanged();
    }

    public void addExternalGraphic(ExternalGraphic externalGraphic) {
        externalGraphics.add(externalGraphic);
        symbols.add(externalGraphic);
    }

    /**
     * Provides a list of suitable marks which can be used to represent this
     * graphic. These should only be used if no ExternalGraphic is provided,
     * or if none of the external graphics formats are supported.
     *
     * @return An array of marks to use when displaying this Graphic. By
     *         default, a "square" with 50% gray fill and black outline with a
     *         size of 6 pixels (unless a size is specified) is provided.
     */
    public Mark[] getMarks() {
        Mark[] ret = new Mark[0];

        if (marks.size() > 0) {
            ret = (Mark[]) marks.toArray(new Mark[0]);
        }

        return ret;
    }

    public void setMarks(Mark[] marks) {
        this.marks.clear();

        for (int i = 0; i < symbols.size();) {
            Object symbol = symbols.get(i);

            if (symbol instanceof Mark) {
                symbols.remove(i);
            } else {
                i++;
            }
        }

        for (int i = 0; i < marks.length; i++) {
            addMark(marks[i]);
        }

        fireChanged();
    }

    public void addMark(Mark mark) {
        if (mark == null) {
            return;
        }

        marks.add(mark);
        symbols.add(mark);
        mark.setSize(size);
        mark.setRotation(rotation);
    }

    /**
     * Provides a list of all the symbols which can be used to represent this
     * graphic
     * <p>
     * A symbol is an ExternalGraphic, Mark or any other object which
     * implements the Symbol interface. These are returned in the order they
     * were set.
     * <p>
     * This class operates as a "view" on getMarks() and getExternalGraphics()
     * with the added magic that if nothing has been set ever a single default
     * MarkImpl will be provided. This default will not effect the internal
     * state it is only there as a sensible default for rendering.
     *
     * @return An array of symbols to use when displaying this Graphic. By
     *         default, a "square" with 50% gray fill and black outline with a
     *         size of 6 pixels (unless a size is specified) is provided.
     */
    public Symbol[] getSymbols() {
        Symbol[] ret = null;

        if (symbols.size() > 0) {
            ret = (Symbol[]) symbols.toArray(new Symbol[symbols.size()]);
        } else {
            ret = new Symbol[] { new MarkImpl() };
        }

        return ret;
    }

    public void setSymbols(Symbol[] symbols) {
        this.symbols.clear();

        if (symbols != null) {
            for (int i = 0; i < symbols.length; i++) {
                addSymbol(symbols[i]);
            }
        }

        fireChanged();
    }

    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);

        if (symbol instanceof ExternalGraphic) {
            addExternalGraphic((ExternalGraphic) symbol);
        }

        if (symbol instanceof Mark) {
            addMark((Mark) symbol);
        }

        return;
    }

    /**
     * This specifies the level of translucency to use when rendering the graphic.<br>
     * The value is encoded as a floating-point value between 0.0 and 1.0 with
     * 0.0 representing totally transparent and 1.0 representing totally
     * opaque, with a linear scale of translucency for intermediate values.<br>
     * For example, "0.65" would represent 65% opacity. The default value is
     * 1.0 (opaque).
     *
     * @return The opacity of the Graphic, where 0.0 is completely transparent
     *         and 1.0 is completely opaque.
     */
    public Expression getOpacity() {
        return opacity;
    }

    /**
     * This parameter defines the rotation of a graphic in the clockwise
     * direction about its centre point in decimal degrees. The value encoded
     * as a floating point number.
     *
     * @return The angle of rotation in decimal degrees. Negative values
     *         represent counter-clockwise rotation.  The default is 0.0 (no
     *         rotation).
     */
    public Expression getRotation() {
        return rotation;
    }

    /**
     * This paramteter gives the absolute size of the graphic in pixels encoded
     * as a floating point number.
     * 
     * <p>
     * The default size of an image format (such as GIFD) is the inherent size
     * of the image.  The default size of a format without an inherent size
     * (such as SVG) is defined to be 16 pixels in height and the
     * corresponding aspect in width.  If a size is specified, the height of
     * the graphic will be scaled to that size and the corresponding aspect
     * will be used for the width.
     * </p>
     *
     * @return The size of the graphic, the default is context specific.
     *         Negative values are not possible.
     */
    public Expression getSize() {
        return size;
    }

    public Displacement getDisplacement() {
        return displacement;
    }

    public void setDisplacement(Displacement offset) {
        Displacement old = this.displacement;
        this.displacement = offset;
        fireChildChanged("offset", offset, old);
    }

    /**
     * Setter for property opacity.
     *
     * @param opacity New value of property opacity.
     */
    public void setOpacity(Expression opacity) {
        Expression old = opacity;
        this.opacity = opacity;
        fireChildChanged("opacity", opacity, old);
    }

    public void setOpacity(double opacity) {
        setOpacity(filterFactory.literal(opacity));
    }

    /**
     * Setter for property rotation.
     *
     * @param rotation New value of property rotation.
     */
    public void setRotation(Expression rotation) {
        Expression old = this.rotation;
        this.rotation = rotation;

        java.util.Iterator iter = marks.iterator();

        while (iter.hasNext()) {
            ((MarkImpl) iter.next()).setRotation(rotation);
        }

        fireChildChanged("rotation", rotation, old);
    }

    public void setRotation(double rotation) {
        setRotation(filterFactory.literal(rotation));
    }

    /**
     * Setter for property size.
     *
     * @param size New value of property size.
     */
    public void setSize(Expression size) {
        Expression old = this.size;
        this.size = size;

        java.util.Iterator iter = marks.iterator();

        while (iter.hasNext()) {
            ((MarkImpl) iter.next()).setSize(size);
        }

        fireChildChanged("size", size, old);
    }

    public void setSize(int size) {
        setSize(filterFactory.literal(size));
    }

    public void setGeometryPropertyName(String name) {
        geometryPropertyName = name;
        fireChanged();
    }

    /**
     * Getter for property geometryPropertyName.
     *
     * @return Value of property geometryPropertyName.
     */
    public java.lang.String getGeometryPropertyName() {
        return geometryPropertyName;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Creates a deep copy clone.
     *
     * @return The deep copy clone.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public Object clone() {
        GraphicImpl clone;

        try {
            clone = (GraphicImpl) super.clone();
            clone.marks = new ArrayList();
            clone.externalGraphics = new ArrayList();
            clone.symbols = new ArrayList();

            // Because ExternalGraphics and Marks are stored twice
            // and we only want to clone them once, we should use
            // the setter methods to place them in the proper lists
            for (Iterator iter = externalGraphics.iterator(); iter.hasNext();) {
                ExternalGraphic exGraphic = (ExternalGraphic) iter.next();
                clone.addExternalGraphic((ExternalGraphic) ((Cloneable) exGraphic)
                    .clone());
            }

            for (Iterator iter = marks.iterator(); iter.hasNext();) {
                Mark mark = (Mark) iter.next();
                clone.addMark((Mark) ((Cloneable) mark).clone());
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen.
        }

        return clone;
    }

    /**
     * Override of hashcode
     *
     * @return The hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (geometryPropertyName != null) {
            result = (PRIME * result) + geometryPropertyName.hashCode();
        }

        if (symbols != null) {
            result = (PRIME * result) + symbols.hashCode();
        }

        if (rotation != null) {
            result = (PRIME * result) + rotation.hashCode();
        }

        if (size != null) {
            result = (PRIME * result) + size.hashCode();
        }

        if (opacity != null) {
            result = (PRIME * result) + opacity.hashCode();
        }

        return result;
    }

    /**
     * Compares this GraphicImpl with another for equality.
     * 
     * <p>
     * Two graphics are equal if and only if they both have the same geometry
     * property name and the same list of symbols and the same rotation, size
     * and opacity.
     * </p>
     *
     * @param oth The other GraphicsImpl to compare with.
     *
     * @return True if this is equal to oth according to the above conditions.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof GraphicImpl) {
            GraphicImpl other = (GraphicImpl) oth;

            return Utilities.equals(this.geometryPropertyName,
                other.geometryPropertyName)
            && Utilities.equals(this.size, other.size)
            && Utilities.equals(this.rotation, other.rotation)
            && Utilities.equals(this.opacity, other.opacity)
            && Utilities.equals(this.getMarks(), other.getMarks() )
            && Utilities.equals( this.getExternalGraphics(), other.getExternalGraphics() )
            && Utilities.equals( this.getSymbols(), other.getSymbols() );                    
        }

        return false;
    }
}
