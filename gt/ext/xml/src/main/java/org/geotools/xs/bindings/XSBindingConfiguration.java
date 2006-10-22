/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.xs.bindings;

import org.geotools.xml.BindingConfiguration;
import org.picocontainer.MutablePicoContainer;


/**
 * Binding configuration for the http://www.w3.org/2001/XMLSchema schema.
 *
 * @generated
 */
public final class XSBindingConfiguration implements BindingConfiguration {
    /**
     * @generated modifiable
     */
    public void configure(MutablePicoContainer container) {
        container.registerComponentImplementation(XS.ALL, XSAllBinding.class);
        container.registerComponentImplementation(XS.ALLNNI,
            XSAllNNIBinding.class);
        container.registerComponentImplementation(XS.ANNOTATED,
            XSAnnotatedBinding.class);
        container.registerComponentImplementation(XS.ANYSIMPLETYPE,
            XSAnySimpleTypeBinding.class);
        container.registerComponentImplementation(XS.ANYTYPE,
            XSAnyTypeBinding.class);
        container.registerComponentImplementation(XS.ANYURI,
            XSAnyURIBinding.class);
        container.registerComponentImplementation(XS.ATTRIBUTE,
            XSAttributeBinding.class);
        container.registerComponentImplementation(XS.ATTRIBUTEGROUP,
            XSAttributeGroupBinding.class);
        container.registerComponentImplementation(XS.ATTRIBUTEGROUPREF,
            XSAttributeGroupRefBinding.class);
        container.registerComponentImplementation(XS.BASE64BINARY,
            XSBase64BinaryBinding.class);
        container.registerComponentImplementation(XS.BLOCKSET,
            XSBlockSetBinding.class);
        container.registerComponentImplementation(XS.BOOLEAN,
            XSBooleanBinding.class);
        container.registerComponentImplementation(XS.BYTE, XSByteBinding.class);
        container.registerComponentImplementation(XS.COMPLEXRESTRICTIONTYPE,
            XSComplexRestrictionTypeBinding.class);
        container.registerComponentImplementation(XS.COMPLEXTYPE,
            XSComplexTypeBinding.class);
        container.registerComponentImplementation(XS.DATE, XSDateBinding.class);
        container.registerComponentImplementation(XS.DATETIME,
            XSDateTimeBinding.class);
        container.registerComponentImplementation(XS.DECIMAL,
            XSDecimalBinding.class);
        container.registerComponentImplementation(XS.DERIVATIONCONTROL,
            XSDerivationControlBinding.class);
        container.registerComponentImplementation(XS.DERIVATIONSET,
            XSDerivationSetBinding.class);
        container.registerComponentImplementation(XS.DOUBLE,
            XSDoubleBinding.class);
        container.registerComponentImplementation(XS.DURATION,
            XSDurationBinding.class);
        container.registerComponentImplementation(XS.ELEMENT,
            XSElementBinding.class);
        container.registerComponentImplementation(XS.ENTITIES,
            XSENTITIESBinding.class);
        container.registerComponentImplementation(XS.ENTITY,
            XSENTITYBinding.class);
        container.registerComponentImplementation(XS.EXPLICITGROUP,
            XSExplicitGroupBinding.class);
        container.registerComponentImplementation(XS.EXTENSIONTYPE,
            XSExtensionTypeBinding.class);
        container.registerComponentImplementation(XS.FACET, XSFacetBinding.class);
        container.registerComponentImplementation(XS.FLOAT, XSFloatBinding.class);
        container.registerComponentImplementation(XS.FORMCHOICE,
            XSFormChoiceBinding.class);
        container.registerComponentImplementation(XS.FULLDERIVATIONSET,
            XSFullDerivationSetBinding.class);
        container.registerComponentImplementation(XS.GDAY, XSGDayBinding.class);
        container.registerComponentImplementation(XS.GMONTH,
            XSGMonthBinding.class);
        container.registerComponentImplementation(XS.GMONTHDAY,
            XSGMonthDayBinding.class);
        container.registerComponentImplementation(XS.GROUP, XSGroupBinding.class);
        container.registerComponentImplementation(XS.GROUPREF,
            XSGroupRefBinding.class);
        container.registerComponentImplementation(XS.GYEAR, XSGYearBinding.class);
        container.registerComponentImplementation(XS.GYEARMONTH,
            XSGYearMonthBinding.class);
        container.registerComponentImplementation(XS.HEXBINARY,
            XSHexBinaryBinding.class);
        container.registerComponentImplementation(XS.ID, XSIDBinding.class);
        container.registerComponentImplementation(XS.IDREF, XSIDREFBinding.class);
        container.registerComponentImplementation(XS.IDREFS,
            XSIDREFSBinding.class);
        container.registerComponentImplementation(XS.INT, XSIntBinding.class);
        container.registerComponentImplementation(XS.INTEGER,
            XSIntegerBinding.class);
        container.registerComponentImplementation(XS.KEYBASE,
            XSKeybaseBinding.class);
        container.registerComponentImplementation(XS.LANGUAGE,
            XSLanguageBinding.class);
        container.registerComponentImplementation(XS.LOCALCOMPLEXTYPE,
            XSLocalComplexTypeBinding.class);
        container.registerComponentImplementation(XS.LOCALELEMENT,
            XSLocalElementBinding.class);
        container.registerComponentImplementation(XS.LOCALSIMPLETYPE,
            XSLocalSimpleTypeBinding.class);
        container.registerComponentImplementation(XS.LONG, XSLongBinding.class);
        container.registerComponentImplementation(XS.NAME, XSNameBinding.class);
        container.registerComponentImplementation(XS.NAMEDATTRIBUTEGROUP,
            XSNamedAttributeGroupBinding.class);
        container.registerComponentImplementation(XS.NAMEDGROUP,
            XSNamedGroupBinding.class);
        container.registerComponentImplementation(XS.NAMESPACELIST,
            XSNamespaceListBinding.class);
        container.registerComponentImplementation(XS.NARROWMAXMIN,
            XSNarrowMaxMinBinding.class);
        container.registerComponentImplementation(XS.NCNAME,
            XSNCNameBinding.class);
        container.registerComponentImplementation(XS.NEGATIVEINTEGER,
            XSNegativeIntegerBinding.class);
        container.registerComponentImplementation(XS.NMTOKEN,
            XSNMTOKENBinding.class);
        container.registerComponentImplementation(XS.NMTOKENS,
            XSNMTOKENSBinding.class);
        container.registerComponentImplementation(XS.NOFIXEDFACET,
            XSNoFixedFacetBinding.class);
        container.registerComponentImplementation(XS.NONNEGATIVEINTEGER,
            XSNonNegativeIntegerBinding.class);
        container.registerComponentImplementation(XS.NONPOSITIVEINTEGER,
            XSNonPositiveIntegerBinding.class);
        container.registerComponentImplementation(XS.NORMALIZEDSTRING,
            XSNormalizedStringBinding.class);
        container.registerComponentImplementation(XS.NOTATION,
            XSNOTATIONBinding.class);
        container.registerComponentImplementation(XS.NUMFACET,
            XSNumFacetBinding.class);
        container.registerComponentImplementation(XS.OPENATTRS,
            XSOpenAttrsBinding.class);
        container.registerComponentImplementation(XS.POSITIVEINTEGER,
            XSPositiveIntegerBinding.class);
        container.registerComponentImplementation(XS.PUBLIC,
            XSPublicBinding.class);
        container.registerComponentImplementation(XS.QNAME, XSQNameBinding.class);
        container.registerComponentImplementation(XS.REALGROUP,
            XSRealGroupBinding.class);
        container.registerComponentImplementation(XS.REDUCEDDERIVATIONCONTROL,
            XSReducedDerivationControlBinding.class);
        container.registerComponentImplementation(XS.RESTRICTIONTYPE,
            XSRestrictionTypeBinding.class);
        container.registerComponentImplementation(XS.SHORT, XSShortBinding.class);
        container.registerComponentImplementation(XS.SIMPLEDERIVATIONSET,
            XSSimpleDerivationSetBinding.class);
        container.registerComponentImplementation(XS.SIMPLEEXPLICITGROUP,
            XSSimpleExplicitGroupBinding.class);
        container.registerComponentImplementation(XS.SIMPLEEXTENSIONTYPE,
            XSSimpleExtensionTypeBinding.class);
        container.registerComponentImplementation(XS.SIMPLERESTRICTIONTYPE,
            XSSimpleRestrictionTypeBinding.class);
        container.registerComponentImplementation(XS.SIMPLETYPE,
            XSSimpleTypeBinding.class);
        container.registerComponentImplementation(XS.STRING,
            XSStringBinding.class);
        container.registerComponentImplementation(XS.TIME, XSTimeBinding.class);
        container.registerComponentImplementation(XS.TOKEN, XSTokenBinding.class);
        container.registerComponentImplementation(XS.TOPLEVELATTRIBUTE,
            XSTopLevelAttributeBinding.class);
        container.registerComponentImplementation(XS.TOPLEVELCOMPLEXTYPE,
            XSTopLevelComplexTypeBinding.class);
        container.registerComponentImplementation(XS.TOPLEVELELEMENT,
            XSTopLevelElementBinding.class);
        container.registerComponentImplementation(XS.TOPLEVELSIMPLETYPE,
            XSTopLevelSimpleTypeBinding.class);
        container.registerComponentImplementation(XS.TYPEDERIVATIONCONTROL,
            XSTypeDerivationControlBinding.class);
        container.registerComponentImplementation(XS.UNSIGNEDBYTE,
            XSUnsignedByteBinding.class);
        container.registerComponentImplementation(XS.UNSIGNEDINT,
            XSUnsignedIntBinding.class);
        container.registerComponentImplementation(XS.UNSIGNEDLONG,
            XSUnsignedLongBinding.class);
        container.registerComponentImplementation(XS.UNSIGNEDSHORT,
            XSUnsignedShortBinding.class);
        container.registerComponentImplementation(XS.WILDCARD,
            XSWildcardBinding.class);
    }
}
