/*
 * Units - Temporary implementation for Geotools 2
 * Copyright (C) 1998 University Corporation for Atmospheric Research (Unidata)
 *               1998 Bill Hibbard & al. (VisAD)
 *               1999 P�ches et Oc�ans Canada
 *               2000 Institut de Recherche pour le D�veloppement
 *               2002 Centre for Computational Geography
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Library General Public
 *    License as published by the Free Software Foundation; either
 *    version 2 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Library General Public License for more details (http://www.gnu.org/).
 *
 *
 *    This package is inspired from the units package of VisAD.
 *    Unidata and Visad's work is fully acknowledged here.
 *
 *                   THIS IS A TEMPORARY CLASS
 *
 *    This is a placeholder for future <code>Unit</code> class.
 *    This skeleton will be removed when the real classes from
 *    JSR-108: Units specification will be publicly available.
 */
package org.geotools.units;

// Entr�s/sorties


/**
 * Une transformation d'unit�s qui repr�sente la
 * combinaison de deux autres transformations.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by the {@link javax.units.Unit} framework.
 */
final class CompoundTransform extends UnitTransform {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6193871497550654092L;
    
    /**
     * Premi�re transformation �
     * appliquer lors des conversions.
     */
    private final UnitTransform transform1;
    
    /**
     * Deuxi�me transformation �
     * appliquer lors des conversions.
     */
    private final UnitTransform transform2;
    
    /**
     * Construit une transformation qui repr�sente la combinaison de <code>transform1</code> avec <code>transform2</code>.
     * Les unit�s seront converties de <code>transform1.fromUnit</code> vers <code>transform2.toUnit</code> en appliquant
     * d'abord la transformation <code>transform1</code>, puis la transformation code>transform2</code>.
     */
    private CompoundTransform(final UnitTransform transform1, final UnitTransform transform2) {
        super(transform1.fromUnit, transform2.toUnit);
        this.transform1 = transform1;
        this.transform2 = transform2;
    }
    
    /**
     * Retourne une transformation qui repr�sente la combinaison de <code>transform1</code> avec <code>transform2</code>.
     * Les unit�s seront converties de <code>transform1.fromUnit</code> vers <code>transform2.toUnit</code> en appliquant
     * d'abord la transformation <code>transform1</code>, puis la transformation code>transform2</code>.
     *
     * @param  transform1 Premi�re transformation � appliquer.
     * @param  transform2 Deuxi�me transformation � appliquer.
     * @return Une combinaison des deux transformations sp�cifi�es. Si un des arguments
     *         est une transformation identit�e, l'autre transformation sera retourn�e.
     */
    public static UnitTransform getInstance(final UnitTransform transform1, final UnitTransform transform2) {
        if (transform1.isIdentity()) return transform2;
        if (transform2.isIdentity()) return transform1;
        return new CompoundTransform(transform1, transform2).intern();
    }
    
    /**
     * Effectue la conversion d'unit�s d'une valeur.
     * @param value Valeur exprim�e selon les unit�s {@link #fromUnit}.
     * @return Valeur exprim�e selon les unit�s {@link #toUnit}.
     */
    public double convert(final double value) {
        return transform2.convert(transform1.convert(value));
    }
    
    /**
     * Effectue la conversion d'unit�s d'un tableaux de valeurs.
     * @param values Valeurs exprim�es selon les unit�s {@link #fromUnit}.
     *        Elles seront converties sur place en valeurs exprim�es selon
     *        les unit�s {@link #toUnit}.
     */
    public void convert(final double[] values) {
        transform1.convert(values);
        transform2.convert(values);
    }
    
    /**
     * Effectue la conversion d'unit�s d'un tableaux de valeurs.
     * @param values Valeurs exprim�es selon les unit�s {@link #fromUnit}.
     *        Elles seront converties sur place en valeurs exprim�es selon
     *        les unit�s {@link #toUnit}.
     */
    public void convert(final float[] values) {
        transform1.convert(values);
        transform2.convert(values);
    }
    
    /**
     * Effectue la conversion inverse d'unit�s d'une valeur.
     * @param value Valeur exprim�e selon les unit�s {@link #toUnit}.
     * @return Valeur exprim�e selon les unit�s {@link #fromUnit}.
     */
    public double inverseConvert(final double value) {
        return transform1.inverseConvert(transform2.inverseConvert(value));
    }
    
    /**
     * Effectue la conversion inverse d'unit�s d'un tableaux de valeurs.
     * @param values Valeurs exprim�es selon les unit�s {@link #toUnit}.
     *        Elles seront converties sur place en valeurs exprim�es selon
     *        les unit�s {@link #fromUnits}.
     */
    public void inverseConvert(final double[] values) {
        transform2.inverseConvert(values);
        transform1.inverseConvert(values);
    }
    
    /**
     * Effectue la conversion inverse d'unit�s d'un tableaux de valeurs.
     * @param values Valeurs exprim�es selon les unit�s {@link #toUnit}.
     *        Elles seront converties sur place en valeurs exprim�es selon
     *        les unit�s {@link #fromUnit}.
     */
    public void inverseConvert(final float[] values) {
        transform2.inverseConvert(values);
        transform1.inverseConvert(values);
    }
    
    /**
     * Indique si cette transformation est
     * identique � la transformation sp�cifi�e.
     */
    public boolean equals(final Object o) {
        if (super.equals(o)) {
            final CompoundTransform t=(CompoundTransform) o;
            return transform1.equals(t.transform1) && transform2.equals(t.transform2);
        }
        return false;
    }
}
