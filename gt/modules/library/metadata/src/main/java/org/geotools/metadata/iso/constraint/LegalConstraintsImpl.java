/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.constraint;

import java.util.Collection;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.constraint.LegalConstraints;
import org.opengis.util.InternationalString;


/**
 * Restrictions and legal prerequisites for accessing and using the resource.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(name = "MD_LegalConstraints", propOrder={
    "accessConstraints", "useConstraints", "otherConstraints"
})
@XmlRootElement(name = "MD_LegalConstraints")
public class LegalConstraintsImpl extends ConstraintsImpl implements LegalConstraints {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2891061818279024901L;

    /**
     * Access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    private Collection<Restriction> accessConstraints;

    /**
     * Constraints applied to assure the protection of privacy or intellectual property, and any
     * special restrictions or limitations or warnings on using the resource.
     */
    private Collection<Restriction> useConstraints;

    /**
     * Other restrictions and legal prerequisites for accessing and using the resource.
     * This method should returns a non-empty value only if {@linkplain #getAccessConstraints
     * access constraints} or {@linkplain #getUseConstraints use constraints} declares
     * {@linkplain Restriction#OTHER_RESTRICTIONS other restrictions}.
     */
    private Collection<InternationalString> otherConstraints;

    /**
     * Constructs an initially empty constraints.
     */
    public LegalConstraintsImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public LegalConstraintsImpl(final LegalConstraints source) {
        super(source);
    }

    /**
     * Returns the access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    @XmlElement(name = "accessConstraints", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<Restriction> getAccessConstraints() {
        return xmlOptional(accessConstraints = nonNullCollection(accessConstraints, Restriction.class));
    }

    /**
     * Set the access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    public synchronized void setAccessConstraints(
            final Collection<? extends Restriction> newValues)
    {
        accessConstraints = copyCollection(newValues, accessConstraints, Restriction.class);
    }

    /**
     * Returns the constraints applied to assure the protection of privacy or intellectual property, and any
     * special restrictions or limitations or warnings on using the resource.
     */
    @XmlElement(name = "useConstraints", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<Restriction> getUseConstraints() {
        return xmlOptional(useConstraints = nonNullCollection(useConstraints, Restriction.class));
    }

    /**
     * Set the constraints applied to assure the protection of privacy or intellectual property, and any
     * special restrictions or limitations or warnings on using the resource.
     */
    public synchronized void setUseConstraints(
            final Collection<? extends Restriction> newValues)
    {
        useConstraints = copyCollection(newValues, useConstraints, Restriction.class);
    }

    /**
     * Returns the other restrictions and legal prerequisites for accessing and using the resource.
     * This method should returns a non-empty value only if {@linkplain #getAccessConstraints
     * access constraints} or {@linkplain #getUseConstraints use constraints} declares
     * {@linkplain Restriction#OTHER_RESTRICTIONS other restrictions}.
     */
    @XmlElement(name = "otherConstraints", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<InternationalString> getOtherConstraints() {
        return xmlOptional(otherConstraints = nonNullCollection(otherConstraints, InternationalString.class));
    }

    /**
     * Set the other restrictions and legal prerequisites for accessing and using the resource.
     */
    public synchronized void setOtherConstraints(
            final Collection<? extends InternationalString> newValues)
    {
        otherConstraints = copyCollection(newValues, otherConstraints, InternationalString.class);
    }

    /**
     * Sets the {@code isMarshalling} flag to {@code true}, since the marshalling
     * process is going to be done.
     * This method is automatically called by JAXB, when the marshalling begins.
     *
     * @param marshaller Not used in this implementation.
     */
///    private void beforeMarshal(Marshaller marshaller) {
///        isMarshalling(true);
///    }

    /**
     * Sets the {@code isMarshalling} flag to {@code false}, since the marshalling
     * process is finished.
     * This method is automatically called by JAXB, when the marshalling ends.
     *
     * @param marshaller Not used in this implementation
     */
///    private void afterMarshal(Marshaller marshaller) {
///        isMarshalling(false);
///    }
}
