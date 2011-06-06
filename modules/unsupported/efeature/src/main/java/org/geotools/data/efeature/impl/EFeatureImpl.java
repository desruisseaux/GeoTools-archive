/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.geotools.data.efeature.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.geotools.data.efeature.EFeature;
import org.geotools.data.efeature.EFeatureContext;
import org.geotools.data.efeature.EFeatureInfo;
import org.geotools.data.efeature.EFeaturePackage;
import org.geotools.data.efeature.EFeatureReader;
import org.geotools.data.efeature.internal.EFeatureContextHelper;
import org.geotools.data.efeature.internal.EFeatureInternal;
import org.geotools.data.efeature.util.EFeatureAttributeList;
import org.geotools.data.efeature.util.EFeatureGeometryList;
import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * <!-- begin-user-doc -->
 * This is the default and recommended implementation of {@link EFeature}.
 * <p>
 * Any alternative implementation of {@link EFeature} should internally 
 * delegate to {@link EFeatureInternal}. If not, unexpected behavior may occur.
 * </p> 
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.geotools.data.efeature.impl.EFeatureImpl#getID <em>ID</em>}</li>
 *   <li>{@link org.geotools.data.efeature.impl.EFeatureImpl#getSRID <em>SRID</em>}</li>
 *   <li>{@link org.geotools.data.efeature.impl.EFeatureImpl#getData <em>Data</em>}</li>
 *   <li>{@link org.geotools.data.efeature.impl.EFeatureImpl#isSimple <em>Simple</em>}</li>
 *   <li>{@link org.geotools.data.efeature.impl.EFeatureImpl#getDefault <em>Default</em>}</li>
 *   <li>{@link org.geotools.data.efeature.impl.EFeatureImpl#getStructure <em>Structure</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class EFeatureImpl extends EObjectImpl implements EFeature {
    /**
     * The default value of the '{@link #getID() <em>ID</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getID()
     * @generated NOT
     * @ordered
     */
    protected static final String ID_EDEFAULT = "";

    /**
     * The default value of the '{@link #getSRID() <em>SRID</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getSRID()
     * @generated NOT 
     * @ordered
     */
    protected static final String SRID_EDEFAULT = "EPSG:4326";

    /**
     * The default value of the '{@link #getData() <em>Data</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getData()
     * @generated NOT
     * @ordered
     */
    protected static final Feature DATA_EDEFAULT = null;

    /**
     * The default value of the '{@link #isSimple() <em>Simple</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSimple()
     * @generated NOT
     * @ordered
     */
    protected static final boolean SIMPLE_EDEFAULT = true;

    /**
     * The default value of the '{@link #getDefault() <em>Default</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDefault()
     * @generated NOT
     * @ordered
     */
    protected static final String DEFAULT_EDEFAULT = "geom";

    /**
     * The default value of the '{@link #getStructure() <em>Structure</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getStructure()
     * @generated NOT
     * @ordered
     */
    protected static final EFeatureInfo STRUCTURE_EDEFAULT = null;

    /**
     * Exposes literal {@link #SRID_EDEFAULT} generated by EMF model generator.
     */
    public static final String DEFAULT_SRID = SRID_EDEFAULT;

    /**
     * Exposes literal {@link #SIMPLE_EDEFAULT} generated by EMF model generator.
     */
    public static final boolean DEFAULT_IS_SIMPLE = SIMPLE_EDEFAULT;

    /**
     * Exposes literal {@link #DATA_EDEFAULT} generated by EMF model generator.
     */
    public static final Feature DEFAULT_DATA = DATA_EDEFAULT;

    /**
     * Exposes literal {@link #DEFAULT_EDEFAULT} generated by EMF model generator.
     */
    public static final String DEFAULT_GEOMETRY_NAME = DEFAULT_EDEFAULT;

    /**
     * Exposes literal {@link #STRUCTURE_EDEFAULT} generated by EMF model generator.
     */
    public static final EFeatureInfo DEFAULT_FEATURE_STRUCTURE = STRUCTURE_EDEFAULT;

    /**
     * Cached internal implementation of {@link EFeature}.
     */
    protected EFeatureInternal eInternal;

    // ----------------------------------------------------- 
    //  Constructors
    // -----------------------------------------------------

    /**
     * <!-- begin-user-doc -->
     * Context-unaware constructor.
     * <p>
     * Use this constructor when the {@link EFeatureContext} is unknown.
     * <p>
     * {@link EFeatureContext Context} and {@link EFeatureInfo structure} must 
     * be set before it can be read by {@link EFeatureReader}. 
     * </p> 
     * @see {@link EFeatureContextHelper} - read more about the context startup problem.
     * @see {@link #setStructure(EFeatureInfo)} - set {@link EFeatureInfo#eContext() context}
     * and {@link EFeatureInfo structure}.
     * <!-- end-user-doc -->
     * @generated NOT
     */
    protected EFeatureImpl() {
        //
        // Construct EObject implementation
        //
        super();
        //
        // Construct the internal EFeature implementation
        //
        eInternal = new EFeatureInternal(this);
    }    

    // ----------------------------------------------------- 
    //  EFeatureImpl methods
    // -----------------------------------------------------

    public EFeatureInternal eImpl() {
        return eInternal;
    }

    // ----------------------------------------------------- 
    //  EFeature implementation
    // -----------------------------------------------------

    /**
     * @generated NOT
     */
    public String getID() {
        return eInternal.getID();
    }

    /**
     * @generated NOT
     */
    public void setID(String newID) {
        eInternal.setID(newID);
    }

    /**
     * @generated NOT
     */
    public String getSRID() {
        return eInternal.getSRID();
    }

    /**
     * @generated NOT
     */
    public void setSRID(String newSRID) {
        eInternal.setSRID(newSRID);
    }

    /**
     * @generated NOT
     */
    public Feature getData() {
        return eInternal.getData();
    }

    /**
     * @generated NOT
     */
    public void setData(Feature newData) {
        eInternal.setData(newData);
    }

    /**
     * @generated NOT
     */
    public boolean isSimple() {
        return eInternal.isSimple();
    }

    /**
     * @generated NOT
     */
    public String getDefault() {
        return getStructure().eGetDefaultGeometryName();
    }

    /**
     * @generated NOT
     */
    public void setDefault(String newDefault) {
        eInternal.setDefault(newDefault);
    }

    /**
     * @generated NOT
     */
    public EFeatureInfo getStructure() {
        return eInternal.getStructure();
    }

    /**
     * @generated NOT
     */
    public void setStructure(EFeatureInfo eStructure) {
        eInternal.setStructure(eStructure);
    }

    /**
     * @generated NOT
     */
    public <V> EFeatureAttributeList<V> getAttributeList(Class<V> valueType) {
        return eInternal.getAttributeList(valueType);
    }

    /**
     * @generated NOT
     */
    public <V extends Geometry> EFeatureGeometryList<V> getGeometryList(Class<V> valueType) {
        return eInternal.getGeometryList(valueType);
    }

    // ----------------------------------------------------- 
    //  EObjectImpl implementation
    // -----------------------------------------------------

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return EFeaturePackage.Literals.EFEATURE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected int eStaticFeatureCount() {
        return 0;
    }

    /**
     * @generated NOT
     */
    @Override
    public String toString() {
        if (eIsProxy())
            return super.toString();

        return eInternal.toString();
    }

} // EFeatureImpl
