/*
 * NamedStyle.java
 *
 * Created on November 3, 2003, 12:40 PM
 */
package org.geotools.styling;

/**
 * A NamedStyle is used to refer to a style that has a name in a WMS.
 * <p>
 * A NamedStyle is a Style that has only Name, so all setters other than
 * setName will throw an <code>UnsupportedOperationException</code>
 * </p>
 * @author jamesm
 */
public class NamedStyle implements Style {
    /** DOCUMENT ME!  */
    private String name;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return this.name;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getTitle() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param title DOCUMENT ME!
     */
    public void setTitle(String title) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAbstract() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param abstractStr DOCUMENT ME!
     */
    public void setAbstract(String abstractStr) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isDefault() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param isDefault DOCUMENT ME!
     */
    public void setDefault(boolean isDefault) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FeatureTypeStyle[] getFeatureTypeStyles() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param types DOCUMENT ME!
     */
    public void setFeatureTypeStyles(FeatureTypeStyle[] types) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     */
    public void addFeatureTypeStyle(FeatureTypeStyle type) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @param visitor DOCUMENT ME!
     */
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
}
