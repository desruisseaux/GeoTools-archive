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

// Divers
import org.geotools.resources.rsc.ResourceKeys;
import org.geotools.resources.rsc.Resources;


/**
 * Classe repr�sentant des unit�s d�cal�es par rapport � d'autres unit�s.
 * Les valeurs exprim�es selon les unit�s d'un objet <code>OffsetUnit</code>
 * peuvent �tre converties vers le syst�me d'unit�s {@link #unit} � l'aide
 * de l'�quation suivante:
 *
 * <blockquote><pre>
 * <var>x</var><sub>{@link #unit}</sub>&nbsp;=&nbsp;{@link #offset}&nbsp;+&nbsp;<var>x</var><sub><code>this</code></sub>
 * </pre></blockquote>
 *
 * Les objets <code>OffsetUnit</code> permettent de faire des conversions entre
 * diff�rentes unit�s, par exemple des degr�s Kelvin aux degr�s Celsius. Cette
 * classe n'ayant pas de constructeur publique, la seule fa�on d'obtenir des
 * unit�s d�cal�es est d'utiliser les m�thodes {@link #getInstance} ou {@link #offset}.
 *
 * <p><strong>NOTE: Des erreurs d'arrondissements importants peuvent survenir lors des
 * conversions d'unit�s utilisant des objets <code>OffsetUnit</code></strong>. La densit� Sigma-T,
 * utilis�e en oc�anographie, en est un bon exemple. Cette densit� Sigma-T est la densit� de l'eau
 * de mer auquel on a retranch�e 1000&nbsp;kg/m�. Par exemple une eau de mer de densit� 1024&nbsp;kg/m�
 * a une densit� Sigma-T de 24&nbsp;kg/m�. Dans un nombre r�el de type <code>float</code>, un nombre
 * de l'ordre de 24 peut �tre m�moris� avec environ 6 chiffres apr�s la virgules. Mais en revanche,
 * un nombre de l'ordre de 1024 ne peut �tre m�moris� qu'avec environ 3 ou 4 chiffres apr�s la virgule.
 * Les conversions des densit�s Sigma-T en densit� ordinaires (qui consistent � ajouter 1000 kg/m�)
 * risquent donc de se traduire par une perte de 2 ou 3 chiffres significatifs de cette densit�. Une fa�on
 * d'�viter ce probl�me est de convertir tous les tableaux de <code>float</code> en <code>double</code>
 * avant de convertir les unit�s.</p>
 *
 * @version 1.0
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by the {@link javax.units.Unit} framework.
 */
/*public*/
final class OffsetUnit extends Unit {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6259444767590765138L;
    
    /**
     * La constante � ajouter aux mesures exprim�es selon ces
     * unit�s pour obtenir des mesures exprim�es selon les unit�s
     * {@link #unit}. Cette constante sera par exemple 273.15 pour
     * passer des degr�s Celsius ou degr�s Kelvin.
     */
    public final double offset;
    
    /**
     * Les unit�s vers lesquelles
     * se feront les conversions.
     */
    public final Unit unit;
    
    /**
     * Construit une unit� d�cal�e par rapport
     * � l'unit� sp�cifi�e, avec le symbole
     * sp�cifi�.
     *
     * @param  offset La constante du d�calage.
     * @param  unit L'unit� associ�e. Pour des raisons
     *         d'efficacit�, cette unit� ne devrait pas
     *         �tre un objet <code>OffsetUnit</code>.
     * @param  symbol Le symbole associ� � cette unit�.
     * @param  prefix Liste des pr�fix pouvant �tre plac�s devant le symbole
     *         <code>symbol</code>, ou <code>null</code> s'il n'y en a pas.
     */
    private OffsetUnit(final double offset, final Unit unit, final String symbol, final PrefixSet prefix) {
        super(symbol, prefix);
        this.offset = offset;
        this.unit   = unit;
        
        if (unit==null) {
            throw new NullPointerException(Resources.format(ResourceKeys.ERROR_NO_UNIT));
        }
        if (Double.isNaN(offset) || Double.isInfinite(offset)) {
            throw new IllegalArgumentException(Resources.format(
                                               ResourceKeys.ERROR_NOT_A_NUMBER_$1,
                                               new Double(offset)));
        }
    }
    
    /**
     * Cr�e une nouvelle unit� de m�me dimension que l'unit� sp�cifi�e, mais dont les mesures
     * seront d�cal�es d'une constante. Les conversions d'unit�s se feront par l'�quation suivante:
     *
     * <center>
     *     <var>x</var><sub><code>unit</code></sub> =
     *     <var>x</var><sub><code>new</code></sub> + <code>offset</code>
     * </center>
     *
     * o� <var>x</var><sub><code>unit</code></sub> repr�sente une quantit� mesur�e
     * selon les unit�s <code>unit</code> et <var>x</var><sub><code>new</code></sub>
     * repr�sente une quantit� mesur�e selon les unit�s retourn�es par cette m�thode.
     *
     * Par exemple on pourrait construire des unit�s de
     * temp�rature en degr�s celsius avec le code suivant:
     *
     * <blockquote><pre>
     *     Unit CELSIUS=OffsetUnit.<strong>getInstance</strong>(273.15, KELVIN);
     * </blockquote></pre>
     *
     * Ce code signifie que 0�C correspond � 273.15�K.
     *
     * @param  offset Constante qu'il faudra additionner aux mesures
     *         exprim�es selon les nouvelles unit�s pour les convertir
     *         dans les unit�s <code>unit</code>.
     * @param  unit Unit�s par rapport � lesquelles
     *         d�caler les nouvelles unit�s.
     * @return Les unit�s cr��es, ou <code>unit</code> si le
     *         param�tre <code>offset</code> est �gal � 0.
     *
     * @see BaseUnit#getInstance
     * @see DerivedUnit#getInstance
     * @see ScaledUnit#getInstance
     */
    public static Unit getInstance(final double offset, final Unit unit) {
        return getInstance(offset, unit, null, null);
    }
    
    /**
     * Cr�e une nouvelle unit� de m�me dimension que l'unit� sp�cifi�e, mais dont les mesures
     * seront d�cal�es d'une constante. Les conversions d'unit�s se feront par l'�quation suivante:
     *
     * <center>
     *     <var>x</var><sub><code>unit</code></sub> =
     *     <var>x</var><sub><code>new</code></sub> + <code>offset</code>
     * </center>
     *
     * o� <var>x</var><sub><code>unit</code></sub> repr�sente une quantit� mesur�e
     * selon les unit�s <code>unit</code> et <var>x</var><sub><code>new</code></sub>
     * repr�sente une quantit� mesur�e selon les unit�s retourn�es par cette m�thode.
     *
     * Par exemple on pourrait construire des unit�s de
     * temp�rature en degr�s celsius avec le code suivant:
     *
     * <blockquote><pre>
     *     Unit CELSIUS=OffsetUnit.<strong>getInstance</strong>(273.15, KELVIN, "�C", null);
     * </blockquote></pre>
     *
     * Ce code signifie que 0�C correspond � 273.15�K.
     *
     * @param  offset Constante qu'il faudra additionner aux mesures
     *         exprim�es selon les nouvelles unit�s pour les convertir
     *         dans les unit�s <code>unit</code>.
     * @param  unit Unit�s par rapport � lesquelles
     *         d�caler les nouvelles unit�s.
     * @param  symbol Le symbole qui repr�sentera les unit�s cr��es. Si
     *         nul, alors un symbole par d�faut sera cr��. Dans l'exemple
     *         pr�c�dent, ce symbole par d�faut serait "+273.15&nbsp;K"
     * @param  prefix Liste des pr�fix pouvant �tre plac�s devant le symbole
     *         <code>symbol</code>, ou <code>null</code> s'il n'y en a pas.
     * @return Les unit�s cr��es, ou <code>unit</code> si le
     *         param�tre <code>offset</code> est �gal � 0.
     *
     * @see BaseUnit#getInstance
     * @see DerivedUnit#getInstance
     * @see ScaledUnit#getInstance
     */
    public static Unit getInstance(final double offset, final Unit unit, final String symbol, final PrefixSet prefix) {
        if (offset==0) {
            if (unit==null) {
                throw new NullPointerException(Resources.format(ResourceKeys.ERROR_NO_UNIT));
            }
            if (symbol!=null) {
                // TODO: Que faire si le symbole sp�cifi�
                //       n'est pas le symbole de 'unit'?
            }
            return unit;
        }
        if (symbol==null) {
            return new OffsetUnit(offset, unit, UnitFormat.DEFAULT.formatOffset(offset, unit, new StringBuffer()).toString(), null).internIgnoreSymbol();
        } else {
            return new OffsetUnit(offset, unit, symbol, prefix).intern();
        }
    }
    
    /**
     * Renvoie une unit� identique � celle-ci, mais
     * avec un nouveau symbole et de nouveaux pr�fix.
     *
     * @param  symbol Nouveau symbole repr�sentant cette unit�. Si ce
     *         param�tre est nul, un symbole par d�faut sera cr��.
     * @param  prefix Liste des pr�fix autoris�s pour le symbole.
     * @return La m�me unit�, mais avec le nouveau symbole. Peut �tre
     *         <code>this</code>, mais ne sera jamais <code>null</code>.
     */
    public Unit rename(final String symbol, final PrefixSet prefix) { // CAST
        return getInstance(offset, unit, symbol, prefix);
    }
    
    /**
     * Retourne la quantit� que repr�sente cette unit�. Les quantit�s sont des cha�nes de
     * caract�res qui d�crivent le param�tre physique mesur�, comme "mass" ou "speed". Si
     * aucune quantit� n'est d�finie pour cette unit�, retourne <code>null</code>.
     */
    public String getQuantityName() {
        return unit.getQuantityName();
    }
    
    /**
     * Cr�e une nouvelle unit� proportionnelle � cette unit�. Par exemple pour
     * convertir en millicelsius des temp�ratures exprim�es en degr�s Celsius,
     * il faut les multiplier par 1000. On peut exprimer cette relation par le
     * code <code>Unit&nbsp;mdegC=degC.scale(0.001)</code>.
     *
     * @param  amount Facteur par lequel il faudra diviser les valeurs
     *         exprim�es selon ces unit�s pour obtenir des valeurs
     *         exprim�es selon les nouvelles unit�s.
     * @return Les nouvelles unit�s.
     *
     * @see #shift
     */
    public Unit scale(final double amount) {
        return (amount==1) ? this : getInstance(offset/amount, unit.scale(amount));
    }
    
    /**
     * Cr�e une nouvelle unit� d�cal�e par rapport � cette unit�. Par exemple
     * pour convertir des degr�s Kelvin en degr�s Celsius, il faut soustraire
     * 273.15 aux degr�s Kelvin. On peut exprimer cette relation par le code
     * <code>Unit&nbsp;celsius=kelvin.shift(273.15)</code>.
     *
     * @param  offset Constante � soustraire aux valeurs exprim�es selon ces
     *         unit�s pour obtenir des valeurs exprim�es selon les nouvelles
     *         unit�s.
     * @return Les nouvelles unit�s.
     *
     * @see #scale
     */
    public Unit shift(final double offset) {
        return (offset==0) ? this : getInstance(this.offset+offset, unit);
    }
    
    /**
     * Indique si les unit�s <code>this</code> et <code>that</code> sont compatibles.
     * Si elles le sont, alors les m�thodes <code>convert</code> ne lanceront jamais
     * d'exception pour ces unit�s.
     *
     * @param that Autre unit�s avec laquelle on veut
     *        v�rifier si ces unit�s sont compatibles.
     * @return <code>true</code> Si l'on garantie que les m�thodes
     *         <code>convert</code> ne lanceront pas d'exceptions.
     */
    public boolean canConvert(final Unit that) {
        return unit.canConvert(that);
    }
    
    /**
     * Effectue la conversion d'une mesure exprim�e selon d'autres unit�s. Par
     * exemple <code>KELVIN.convert(15, CELSIUS)</code> retournera <code>288.15</code>.
     *
     * @param value    La valeur exprim�e selon les autres unit�s (<code>fromUnit</code>).
     * @param fromUnit Les autres unit�s.
     * @return         La valeur convertie selon ces unit�s (<code>this</code>).
     * @throws         UnitException Si les unit�s ne sont pas compatibles.
     */
    public double convert(final double value, final Unit fromUnit) throws UnitException {
        if (fromUnit==this) {
            return value; // sligh optimization
        }
        return unit.convert(value, fromUnit)-offset;
    }
    
    /**
     * Effectue sur-place la conversion de mesures exprim�es selon d'autres
     * unit�s. Les valeurs converties remplaceront les anciennes valeurs.
     *
     * @param  values En entr�, les valeurs exprim�es selon les autres unit�s
     *         (<code>fromUnit</code>). En sortie, les valeurs exprim�es selon ces
     *         unit�s (<code>this</code>).
     * @param  fromUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    public void convert(final double[] values, final Unit fromUnit) throws UnitException {
        if (!equalsIgnoreSymbol(fromUnit)) {
            unit.convert(values, fromUnit);
            for (int i=0; i<values.length; i++) {
                values[i] -= offset;
            }
        }
    }
    
    /**
     * Effectue sur-place la conversion de mesures exprim�es selon d'autres
     * unit�s. Les valeurs converties remplaceront les anciennes valeurs.
     * Notez que d'importantes erreurs d'arrondissement peuvent survenir
     * si <code>fromUnit</code> est de la classe {@link OffsetUnit}.
     *
     * @param  values En entr�, les valeurs exprim�es selon les autres
     *         unit�s (<code>fromUnit</code>). En sortie, les valeurs exprim�es
     *         selon ces unit�s (<code>this</code>).
     * @param  fromUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    public void convert(final float[] values, final Unit fromUnit) throws UnitException {
        if (!equalsIgnoreSymbol(fromUnit)) {
            unit.convert(values, fromUnit);
            for (int i=0; i<values.length; i++) {
                values[i] = (float) (values[i]-offset);
            }
        }
    }
    
    /**
     * Retourne un objet qui saura convertir selon ces unit�s les valeurs exprim�es
     * selon d'autres unit�s. Cette m�thode est avantageuse si on pr�voie fa�re
     * plusieurs conversions, car la transformation � utiliser est d�termin�e une
     * fois pour toute.
     *
     * @param  fromUnit Unit�s � partir de lesquel faire les conversions.
     * @return Une transformation des unit�s <code>fromUnit</code>
     *         vers les unit�s <code>this</code>. Cette m�thode ne
     *         retourne jamais <code>null</code>.
     * @throws UnitException Si les unit�s ne sont pas compatibles.
     */
    public UnitTransform getTransform(final Unit fromUnit) throws UnitException {
        if (!equalsIgnoreSymbol(fromUnit)) {
            final UnitTransform tr=unit.getTransform(fromUnit);
            if (tr instanceof OffsetTransform) {
                return OffsetTransform.getInstance(fromUnit, this, ((OffsetTransform) tr).offset+offset);
            } else {
                return CompoundTransform.getInstance(tr, OffsetTransform.getInstance(unit, this, offset));
            }
        } else {
            return IdentityTransform.getInstance(fromUnit, this);
        }
    }
    
    /**
     * Convertit une mesure vers d'autre unit�s.
     *
     * @param value  La valeur exprim�e selon ces unit�s (<code>this</code>).
     * @param toUnit Les autres unit�s.
     * @return       La valeur convertie selon les autres unit�s (<code>toUnit</code>).
     * @throws       UnitException Si les unit�s ne sont pas compatibles.
     */
    protected double inverseConvert(final double value, final Unit toUnit) throws UnitException {
        if (toUnit==this) {
            return value; // sligh optimization
        }
        return unit.inverseConvert(value+offset, toUnit);
    }
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unit�s.
     * Les valeurs converties remplaceront les anciennes valeurs. Cette
     * m�thode est l'inverse de la m�thode {@link #convert(double[],Unit)}.
     *
     * @param  values En entr�, les valeur exprim�es selon ces unit�s
     *         (<code>this</code>). En sortie, les valeurs exprim�es
     *         selon les autres unit�s (<code>toUnit</code>).
     * @param  toUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    protected void inverseConvert(final double[] values, final Unit toUnit) throws UnitException {
        if (!equalsIgnoreSymbol(toUnit)) {
            if (unit.canConvert(toUnit)) {
                for (int i=0; i<values.length; i++) {
                    values[i] += offset;
                }
                unit.inverseConvert(values, toUnit);
            } else {
                throw toUnit.incompatibleUnitsException(this);
            }
        }
    }
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unit�s.
     * Les valeurs converties remplaceront les anciennes valeurs. Cette
     * m�thode est l'inverse de la m�thode {@link #convert(float[],Unit)}.
     * @param  values En entr�, les valeur exprim�es selon ces unit�s
     *         (<code>this</code>). En sortie, les valeurs exprim�es
     *         selon les autres unit�s (<code>toUnit</code>).
     * @param  toUnit Les autres unit�s.
     * @throws UnitException Si les unit�s ne sont pas compatibles. Dans ce
     *         cas, aucun �l�ment de <code>values</code> n'aura �t� modifi�.
     */
    protected void inverseConvert(final float[] values, final Unit toUnit) throws UnitException {
        if (!equalsIgnoreSymbol(toUnit)) {
            if (unit.canConvert(toUnit)) {
                for (int i=0; i<values.length; i++) {
                    values[i] = (float) (values[i]+offset);
                }
                unit.inverseConvert(values, toUnit);
            } else {
                throw toUnit.incompatibleUnitsException(this);
            }
        }
    }
    
    /**
     * Retourne un objet qui saura convertir selon d'autres unit�s les
     * valeurs exprim�es selon ces unit�s. Cette m�thode est l'inverse
     * de {@link #getTransform}.
     *
     * @param  toUnit Unit�s vers lesquel faire les conversions.
     * @return Une transformation des unit�s <code>this</code>
     *         vers les unit�s <code>toUnit</code>. Cette m�thode
     *         ne retourne jamais <code>null</code>.
     * @throws UnitException Si les unit�s ne sont pas compatibles.
     */
    protected UnitTransform getInverseTransform(final Unit toUnit) throws UnitException {
        if (!equalsIgnoreSymbol(toUnit)) {
            final UnitTransform tr=unit.getInverseTransform(toUnit);
            if (tr instanceof OffsetTransform) {
                return ScaledTransform.getInstance(this, toUnit, ((OffsetTransform) tr).offset-offset);
            } else {
                return CompoundTransform.getInstance(OffsetTransform.getInstance(this, unit, -offset), tr);
            }
        } else {
            return IdentityTransform.getInstance(this, toUnit);
        }
    }
    
    /**
     * Indique si deux unit�s sont �gales, en ignorant leurs symboles. Le
     * champs {@link #symbol} de chacune des deux unit�s ne sera pas pris
     * en compte.
     */
    public boolean equalsIgnoreSymbol(final Unit unit) {
        if (unit instanceof OffsetUnit) {
            final OffsetUnit that = (OffsetUnit) unit;
            return Double.doubleToLongBits(this.offset)==Double.doubleToLongBits(that.offset) &&
                   this.unit.equalsIgnoreSymbol(that.unit);
        } else {
            return false;
        }
    }
    
    /**
     * Retourne un code
     * pour cette unit�.
     */
    public int hashCode() {
        final long code=Double.doubleToLongBits(offset);
        return (int) code ^ (int) (code >>> 32) ^ unit.hashCode();
    }
}
