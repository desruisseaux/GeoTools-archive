package org.geotools.ows.v1_1;

import java.util.Map;

import net.opengis.ows.v1_1_0.Ows11Factory;

import org.eclipse.emf.ecore.EFactory;
import org.geotools.xlink.XLINKConfiguration;
import org.geotools.xml.ComplexEMFBinding;
import org.geotools.xml.Configuration;
import org.geotools.xml.XMLConfiguration;
import org.picocontainer.MutablePicoContainer;

/**
 * Parser configuration for the http://www.opengis.net/ows/1.1 schema.
 * 
 * @generated
 */
public class OWSConfiguration extends Configuration {

    /**
     * Creates a new configuration.
     * 
     * @generated
     */
    public OWSConfiguration() {
        super(OWS.getInstance());

        addDependency(new XMLConfiguration());
        addDependency(new XLINKConfiguration());
    }

    /**
     * Registers the bindings for the configuration.
     * 
     * @generated
     */
    protected final void registerBindings(MutablePicoContainer container) {
        
    }
    
    protected void registerBindings(Map bindings) {
        bindings.put(OWS.AcceptVersionsType,new ComplexEMFBinding(Ows11Factory.eINSTANCE, OWS.AcceptVersionsType));        
        bindings.put(OWS.GetCapabilitiesType,new ComplexEMFBinding(Ows11Factory.eINSTANCE, OWS.GetCapabilitiesType));
        bindings.put(OWS.SectionsType ,new ComplexEMFBinding(Ows11Factory.eINSTANCE, OWS.SectionsType));
        bindings.put(OWS.AcceptFormatsType,new ComplexEMFBinding(Ows11Factory.eINSTANCE, OWS.AcceptFormatsType));
        bindings.put(OWS.BoundingBoxType,new ComplexEMFBinding(Ows11Factory.eINSTANCE, OWS.BoundingBoxType));
        bindings.put(OWS.CodeType,new ComplexEMFBinding(Ows11Factory.eINSTANCE, OWS.CodeType));
        bindings.put(OWS._ExceptionReport,new ComplexEMFBinding(Ows11Factory.eINSTANCE, OWS._ExceptionReport));
        bindings.put(OWS.ExceptionType,new ComplexEMFBinding(Ows11Factory.eINSTANCE, OWS.ExceptionType));
    }
}