/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.propertysheet;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.*;
import org.geowidgets.crs.widgets.propertysheet.eclipse.PropertySheetViewer;
import org.geowidgets.crs.widgets.propertysheet.propertysources.*;
import org.geowidgets.framework.basewidgets.propertysheet.PropertySheetNotSorter;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.Conversion;

/** 
 *
 * @author Matthias Basler
 * @param <T> a CRS-based object type that serves as the top level entry for
 * this property view. For instance use <code>GeographiCRS</code> if the purpose
 * of this property viewer is to select or edit a geographic CRS.
 */
public class CRSElementPropertyViewer<T extends IdentifiedObject> extends Composite {
    protected PropertySheetViewer prop;
    protected T root;

    /** Creates a new property viewer with the initial content given by the
     * <code>content</code> parameter.
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style the style of widget to construct
     * @param content the inital contents of the type in which this property
     * viewer was defined.
     */
    public CRSElementPropertyViewer(Composite parent, int style, T content) {
        super(parent, style);
        initTree(content);
    }

    protected void initTree(T root) {
        setLayout(new FillLayout());

        PropertySheetEntry entry = new PropertySheetEntry();
        entry.setPropertySourceProvider(new CRSPropertySourceProvicer(root.getClass()));

        this.prop = new PropertySheetViewer(this);
        this.prop.setRootEntry(entry);
        //We don't want the properties to get sorted.
        this.prop.setSorter(new PropertySheetNotSorter());

        setContent(root);
    }

    /** Programatically change the object to be shown (and edited) with this
     * propert viewer.
     * @param root
     * @return <code>true</code> if the root object was indeed change,
     * <code>false</code> if the argument was <code>null</code> or equal
     * to the current root.
     */
    public boolean setContent(T root) {
        if (root == null) return false;
        this.root = root;
        this.prop.setInput(new Object[] { root });
        return true;
    }

    /** @return the current object in question with its current
     * properties as selected by the user.
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public T getContent(){
        return (T)this.prop.getInput();
    }
    
    /** A PropertySourceProvider that delivers <code>PropertySource</code> objects
     * for all usual CRS-related object types, such as GeographicCRS, Ellipsoid
     * and so on.
     *
     * @author Matthias Basler
     */
    public static class CRSPropertySourceProvicer implements IPropertySourceProvider {
        /* We need to know the top level class, because this PropertySource
         * will be the only on whose name field is directly shown. */
        Class topClass;

        /** Creates a new PropertySource provider for CRS-related objects.
         * @param topClass the class of the top level entry. For instance use
         * <code>GeographicCRS</code> if the purpose of this property viewer
         * is to select or edit a geographic CRS. Only the PropertySource for
         * objects of the <code>topClass</code> will have their name element
         * shown as the first row, for all other (child objects) it it assumed
         * that it is already show in the parent object's PropertySource.
         * Example for a GeographicCRS as top level element: <pre>
         * GeographicCRS:    | WGS84        <- top level entry: name shown
         * Code:             | 1234
         * Geodetic Datum:   | WGS84        <- parent contains name as attribute
         *     Code:         | 2345         <- name hidden, code is first line of child entry
         *     ...           | [someDetails]
         * Coordinate System:| EllipsoidalCS ... <- name again in parent entry
         *     Code:         | 3456         <- again child entry starts with code (2nd line)
         *     ...           | [someDetails]
         * Area:             | World
         * ...               | [someDetails]
         * </pre> (Note that the above is a simplyfied sketch.)
         */
        public CRSPropertySourceProvicer(Class topClass) {
            this.topClass = topClass;
        }

        public IPropertySource getPropertySource(Object object) {
            if (object instanceof Ellipsoid) {
                return new EL_PropertySource((Ellipsoid) object, Ellipsoid.class
                        .isAssignableFrom(this.topClass));
            } else if (object instanceof PrimeMeridian) {
                return new PM_PropertySource((PrimeMeridian) object, PrimeMeridian.class
                        .isAssignableFrom(this.topClass));
            } else if (object instanceof GeodeticDatum) {
                return new GD_PropertySource((GeodeticDatum) object, this,
                        GeodeticDatum.class.isAssignableFrom(this.topClass));
            } else if (object instanceof EllipsoidalCS) {
                return new CS_PropertySource<EllipsoidalCS>((EllipsoidalCS) object, this,
                        EllipsoidalCS.class.isAssignableFrom(this.topClass));
            } else if (object instanceof CartesianCS) {
                return new CS_PropertySource<CartesianCS>((CartesianCS) object, this,
                        CartesianCS.class.isAssignableFrom(this.topClass));
            } else if (object instanceof Conversion) {
                return new PROJ_PropertySource((Conversion) object, Conversion.class
                        .isAssignableFrom(this.topClass));
            } else if (object instanceof GeographicCRS) {
                return new GCRS_PropertySource((GeographicCRS) object, this,
                        GeographicCRS.class.isAssignableFrom(this.topClass));
            } else if (object instanceof ProjectedCRS) {
                return new PCRS_PropertySource((ProjectedCRS) object, this,
                        ProjectedCRS.class.isAssignableFrom(this.topClass));
            } else return null;
        }

    }
}
