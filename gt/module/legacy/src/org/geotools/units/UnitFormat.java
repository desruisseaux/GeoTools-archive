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

// Formattage de textes
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geotools.resources.Utilities;
import org.geotools.resources.XMath;
import org.geotools.resources.rsc.ResourceKeys;
import org.geotools.resources.rsc.Resources;
import org.geotools.util.WeakHashSet;


/**
 * Classe charg�e du formattage ou de l'interpr�tation des symboles d'une unit�.
 * <strong>Cette classe n'est qu'un premier jet</strong>. Elle ne contient pas encore d'API qui
 * permettrait de contr�ler la fa�on d'�crire les unit�s. Plus important, le contrat g�n�ral voulant
 * que tout objet produit par {@link #format(Object)} soit lissible par {@link #parse(String)} <u>n'est
 * pas garanti</u> dans la version actuelle.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by the {@link javax.units.Unit} framework.
 */
final class UnitFormat {
    /**
     * Format par d�faut � utiliser pour construire et interpr�ter les symboles des unit�s.
     * Ce format sera utilis� par les constructeurs des diff�rentes classes {@link Unit}
     * pour cr�er des symboles, ainsi que par la m�thode {@link Unit#getUnit} pour interpr�ter
     * un symbole.
     */
    static final UnitFormat DEFAULT=new UnitFormat();
    
    /**
     * Banque des objets qui ont �t� pr�c�demment cr��s et
     * enregistr�s par un appel � la m�thode {@link #intern}.
     */
    private static final WeakHashSet pool=Prefix.pool;
    
    /**
     * Inverse d'une petite valeur servant � �viter des erreurs d'arrondissements.
     * Cette valeur est d�finie arbitrairement � 2^24, soit environ 1.678E+7.
     */
    private static final double INV_EPS = 16777216;
    
    /**
     * Ordre de pr�f�rences des classes d'unit�s. Cette ordre sera pris en
     * compte si plusieurs unit�s ont �t� trouv�s pour un symbole donn�.
     */
    private static final Class[] PRIORITIES=new Class[] {
        BaseUnit.class,
        DerivedUnit.class,
        ScaledUnit.class,
        OffsetUnit.class
    };
    
    /**
     * Symbole repr�sentant la multiplication d'une unit� par un facteur.
     */
    private static final char SCALE_SYMBOL = '\u00D7';
    
    /**
     * Symbole repr�sentant la multiplication de deux unit�s.
     */
    private static final char DOT_SYMBOL = '\u22C5'; // TODO: on devrait plut�t utiliser '\u22C5', mais ce symbole n'est pas affich� correctement.
    
    /**
     * Symbole repr�sentant la division de deux unit�s.
     */
    private static final char SLASH_SYMBOL = '/';
    
    /**
     * Symbole de l'op�rateur exposant.
     */
    private static final char EXPONENT_SYMBOL='^';
    
    /**
     * Symbole repr�sentant l'ouverture d'une parenth�se.
     */
    private static final char OPEN_SYMBOL = '(';
    
    /**
     * Symbole repr�sentant la fermeture d'une parenth�se.
     */
    private static final char CLOSE_SYMBOL = ')';
    
    /**
     * Construit un objet qui lira et �crira
     * des unit�s avec les param�tres par d�faut.
     */
    public UnitFormat()
    {}
    
    /**
     * Construit un symbole � partir du facteur sp�cifi�. Ce sera le symbole
     * de l'unit� de base avec son exposant. Par exemple "m", "m�" ou "kg^-1".
     */
    final StringBuffer format(final Factor factor, final StringBuffer buffer) {
        if (factor.power!=0) {
            buffer.append(factor.baseUnit.symbol);
            if (factor.power!=1) {
                final String power = String.valueOf(factor.power);
                final int   length = power.length();
                final int  initPos = buffer.length();
                for (int i=0; i<length; i++) {
                    final char c=power.charAt(i);
                    final char s=Utilities.toSuperScript(c);
                    if (s==c) {
                        buffer.setLength(initPos);
                        buffer.append(EXPONENT_SYMBOL);
                        buffer.append(power);
                        return buffer;
                    }
                    buffer.append(s);
                }
            }
        } else {
            buffer.append('1');
        }
        return buffer;
    }
    
    /**
     * Construit un symbole � partir du tableau de facteurs sp�cifi�s. L'impl�mentation par d�faut place
     * au d�but les unit�s qui ont une puissance positive, puis � la droite d'un signe "/" les unit�s qui
     * ont une puissance n�gative. Le r�sultat ressemblera par exemple � "m/s". Si n�cessaire, des parenth�ses
     * seront utilis�es.
     */
    final StringBuffer format(final Factor[] factors, StringBuffer buffer) {
        /*
         * Ajoute au buffer tous les termes qui se trouvent comme num�rateurs
         * (puissance positive).  Au passage, on comptera le nombre de termes
         * qui se trouvent comme d�nominateurs (puissance n�gative).
         */
        int numeratorCount   = 0;
        int denominatorCount = 0;
        for (int i=0; i<factors.length; i++) {
            final Factor factor=factors[i];
            if (factor.power>0) {
                if (numeratorCount!=0) buffer.append(DOT_SYMBOL);
                buffer=format(factor, buffer);
                numeratorCount++;
            } else if (factor.power<0) {
                denominatorCount++;
            }
        }
        /*
         * Ajoute au buffer tous les termes qui se trouvent comme d�nominateurs
         * (puissance n�gative), s'il y en a.
         */
        if (denominatorCount!=0) {
            if (numeratorCount==0)   buffer.append('1');
            buffer.append(SLASH_SYMBOL);
            if (denominatorCount!=1) buffer.append(OPEN_SYMBOL);
            denominatorCount=0;
            for (int i=0; i<factors.length; i++) {
                final Factor factor=factors[i];
                if (factor.power<0) {
                    if (denominatorCount!=0) buffer.append(DOT_SYMBOL);
                    buffer=format(factor.inverse(), buffer);
                    denominatorCount++;
                }
            }
            if (denominatorCount!=1) {
                buffer.append(CLOSE_SYMBOL);
            }
        }
        return buffer;
    }
    
    /**
     * Renvoie une cha�ne de caract�res repr�sentant la multiplication d'un symbole par un facteur.
     * Par exemple cette repr�sentation pourrait �tre de la forme <code>"\u00D70,9144\u00A0m"</code>
     * pour repr�senter un yard. Si le symbole <code>unit</code> autorise l'utilisation de pr�fix, un
     * pr�fix pourra �tre plac� devant le symbole plut�t que d'�crire le facteur. C'est le cas par
     * exemple des centim�tres qui peuvent �tre �crits comme "cm".
     */
    final StringBuffer formatScaled(double amount, final SimpleUnit unit, StringBuffer buffer) {
        String          symbol = unit.symbol;
        final int       length = symbol.length();
        final int      initPos = buffer.length();
        final PrefixSet prefix = unit.prefix;
        if (prefix!=null) {
            /*
             * Commence par v�rifier si le symbole de <code>unit</code> commen�ait
             * d�j� par un des pr�fix. Si oui, on supprimera cet ancien pr�fix.
             */
            final String unprefixedSymbol = unit.getUnprefixedSymbol();
            if (symbol.endsWith(unprefixedSymbol)) // Test only to make sure...
            {
                final Prefix p=prefix.getPrefix(symbol.substring(0, symbol.length()-unprefixedSymbol.length()));
                if (p!=null) {
                    symbol=unprefixedSymbol;
                    amount *= p.amount;
                }
            }
            /*
             * Essaie de placer un nouveau pr�fix devant
             * le symbole, en fonction de l'�chelle.
             */
            final Prefix p=prefix.getPrefix(amount);
            if (p!=null) {
                symbol = p.symbol+symbol;
                amount /= p.amount;
            }
        }
        /*
         * Si <code>amount</code> est presqu'une puissance de 10, arrondi
         * � la puissance de 10 la plus proche. Cette �tape vise � r�duire
         * certaines erreurs d'arrondissement.
         */
        final double power = Math.rint(XMath.log10(amount)*INV_EPS)/INV_EPS;
        if (power==Math.rint(power)) amount=XMath.pow10(power);
        /*
         * Si on n'a pas pu placer un pr�fix devant les unit�s,
         * alors on �crira un symbole de multiplication.
         */
        if (amount!=1) {
            final NumberFormat format=NumberFormat.getNumberInstance();
            buffer.append(SCALE_SYMBOL);
            format.format(amount, buffer, new FieldPosition(0));
            if (length!=0) {
                buffer.append('\u00A0'); // No-break space
            }
        }
        buffer.append(symbol);
        return buffer;
    }
    
    /**
     * Renvoie une cha�ne de caract�res repr�sentant le d�calage d'une unit�. Par exemple cette repr�sentation
     * pourrait �tre de la forme <code>"+273.15\u00A0K</code> pour repr�senter des degr�s Celsius.
     */
    final StringBuffer formatOffset(final double offset, final Unit unit, StringBuffer buffer) {
        final String       symbol = unit.toString();
        final int          length = symbol.length();
        final NumberFormat format = NumberFormat.getNumberInstance();
        if (format instanceof DecimalFormat) {
            final DecimalFormat cast=(DecimalFormat) format;
            if (cast.getPositivePrefix().trim().length()==0) {
                cast.setPositivePrefix("+");
            }
        }
        format.format(offset, buffer, new FieldPosition(0));
        if (length!=0) {
            buffer.append('\u00A0'); // No-break space
            buffer.append(symbol);
        }
        return buffer;
    }
    
    /**
     * Retourne les unit�s qui correspondent au symbole sp�cifi�. Si plus d'une
     * unit� correspond au symbole sp�cifi�, une unit� arbitraire sera choisie.
     *
     * @param  symbol Symbole des unit�s recherch�es. Cet argument ne doit pas �tre nul.
     * @return Si les unit�s ont �t� trouv�s, l'objet {@link Unit} qui les repr�sentent.
     *         Sinon, un objet {@link String} contenant la portion de cha�ne qui n'a pas
     *         �t� reconnue.
     * @throws IllegalArgumentException si les parenth�ses ne sont pas �quilibr�es.
     */
    final Object parse(final String symbol) throws IllegalArgumentException {
        final Set set=new HashSet(11);
        final String unrecognized=parse(symbol.replace('*', DOT_SYMBOL), set);
        final Unit[] units = (Unit[]) set.toArray(new Unit[set.size()]);
        switch (units.length) {
            case 0:  return unrecognized;
            case 1:  return units[0];
            default: return selectUnit(units);
        }
    }
    
    /**
     * Recherche des unit�s qui correspondent au symbole sp�cifi�. Les unit�s trouv�s seront ajout�s
     * dans l'ensemble <code>set</code>. Si aucune unit� n'a �t� trouv�e, la taille de <code>set</code>
     * n'aura pas augment�e. Dans ce cas, cette m�thode retourne les caract�res qui n'ont pas �t� reconnus.
     *
     * @param  symbol Symbole des unit�s recherch�es.
     * @param  set Ensemble dans lequel placer les unit�s trouv�es.
     * @return <code>null</code> si des unit�s ont �t� trouv�es, ou sinon la portion
     *         de la cha�ne <code>symbol</code> qui n'a pas �t� reconnue.
     * @throws IllegalArgumentException si les parenth�ses ne sont pas �quilibr�es.
     */
    private String parse(String symbol, final Set set) throws IllegalArgumentException {
        symbol=symbol.trim();
        final int initialSize=set.size();
        /*
         * Ignore les parenth�ses qui se trouvent au d�but ou � la fin des unit�s. Les �ventuelles
         * parenth�ses qui se trouverait au milieu ne sont pas pris en compte maintenant. Elles le
         * seront plus tard.   On ignore toujours le m�me nombre de parenth�ses ouvrantes au d�but
         * que de parenth�ses fermantes � la fin.
         */
        if (true) {
            int lower=0;
            int upper=0;
            int level=0;
            int index=0;
            final int length=symbol.length();
            while (index<length) {
                switch (symbol.charAt(index)) {
                    case  OPEN_SYMBOL: level++; index++; break;
                    case CLOSE_SYMBOL: level--; index++; break;
                    default: {
                        if (upper==0) lower=index;
                        upper=++index;
                        break;
                    }
                }
                if (level==0) break;
            }
            if (level!=0) {
                throw new IllegalArgumentException(Resources.format(
                                ResourceKeys.ERROR_NON_EQUILIBRATED_PARENTHESIS_$2,
                                symbol, String.valueOf(level>=0 ? CLOSE_SYMBOL : OPEN_SYMBOL)));
            }
            if (index==length) {
                upper=length-upper;
                if (lower>upper) lower=upper;
                if (upper>lower) upper=lower;
                upper=length-upper;
                symbol=symbol.substring(lower, upper);
            }
        }
        /*
         * Recherche les symboles de divisions ou de multiplications. Si un tel symbole est trouv�, on lira s�parament
         * les unit�s qui pr�c�dent et qui suivent ce symbole. On ne prend en compte que les symboles qui ne se trouvent
         * pas dans une parenth�se. Si un symbole se trouve dans une parenth�se, il sera pris en compte plus tard.
         */
        if (true) {
            int level=0;
            String unrecognized=null;
            for (int i=symbol.length(); --i>=0 && level<=0;) {
                final int power;
                switch (symbol.charAt(i)) {
                    case  OPEN_SYMBOL: level++; continue;
                    case CLOSE_SYMBOL: level--; continue;
                    case   DOT_SYMBOL: power=+1; break;
                    case SLASH_SYMBOL: power=-1; break;
                    default : continue;
                }
                if (level!=0) continue;
                /*
                 * Un signe de multiplication ou d'addition a �t� trouv�.
                 * Lit d'abord les unit�s avant ce signe, puis apr�s ce signe.
                 */
                String tmp;
                final Set unitsA=new HashSet(11);
                final Set unitsB=new HashSet(11);
                tmp=parse(symbol.substring(0,i), unitsA); if (unrecognized==null) unrecognized=tmp;
                tmp=parse(symbol.substring(i+1), unitsB); if (unrecognized==null) unrecognized=tmp;
                for (final Iterator itA=unitsA.iterator(); itA.hasNext();) {
                    final Unit unitA = (Unit) itA.next();
                    for (final Iterator itB=unitsB.iterator(); itB.hasNext();) {
                        final Unit unitB = (Unit) itB.next();
                        try {
                            final Unit unit;
                            switch (power) {
                                case -1: unit=unitA.divide  (unitB);            break;
                                case +1: unit=unitA.multiply(unitB);            break;
                                default: unit=unitA.multiply(unitB.pow(power)); break;
                            }
                            set.add(unit);
                        } catch (UnitException exception) {
                            // ignore incompatible units.
                        }
                    }
                }
                return (set.size()==initialSize) ? unrecognized : null;
            }
            if (level!=0) {
                throw new IllegalArgumentException(Resources.format(
                                ResourceKeys.ERROR_NON_EQUILIBRATED_PARENTHESIS_$2,
                                symbol, String.valueOf(level>=0 ? CLOSE_SYMBOL : OPEN_SYMBOL)));
            }
        }
        /*
         * Parvenu � ce stade, on n'a d�tect� aucun symbole de multiplication ou de division
         * et aucune parenth�ses. Il ne devrait rester que le symbole de l'unit�, �ventuellement
         * avec son pr�fix et un exposant. On tente maintenant d'interpr�ter ce symbole.
         */
        int           power = 1;
        boolean powerParsed = false;
        /*
         * La boucle suivante sera ex�cut�e deux fois. La premi�re fois, on n'aura pas tent� de prendre en compte
         * une �ventuelle puissance apr�s le symbole (par exemple le '2' dans "m�"), parce que le symbole avec sa
         * puissance a peut-�tre �t� d�j� explicitement d�finie.   Si cette tentative a �chou�, alors le deuxi�me
         * passage de la boucle prendra en compte un �ventuel exposant.
         */
        while (true) {
            final int length=symbol.length();
            for (int lower=0; lower<length; lower++) {
                Unit unit=Unit.getCached(symbol.substring(lower));
                if (unit!=null) {
                    /*
                     * Parvenu � ce stade, nous avons trouv� une unit� qui correspond au symbole <code>symbol</code>.
                     * S'il a fallu sauter des caract�res pour trouver cette unit�, alors les caract�res ignor�s doivent
                     * �tre un pr�fix. On tentera d'identifier le pr�fix en interrogeant la liste des pr�fix autoris�s
                     * pour cette unit�.
                     */
                    if (lower!=0) {
                        if (unit.prefix==null) continue;
                        final Prefix prefix=unit.prefix.getPrefix(symbol.substring(0, lower));
                        if (prefix==null) continue;
                        unit=unit.scale(prefix.amount);
                    }
                    /*
                     * Tente maintenant d'�lever les unit�s � une puissance,
                     * s'ils sont suivit d'une puissance.
                     */
                    try {
                        set.add(unit.pow(power));
                    } catch (UnitException exception) {
                        continue;
                    }
                }
            }
            /*
             * Si c'est le second passage de la boucle, la puissance a d�j� �t�
             * prise en compte. On terminera alors cette m�thode maintenant.
             */
            if (powerParsed) {
                return (set.size()==initialSize) ? symbol : null;
            }
            powerParsed=true;
            /*
             * Si aucune unit� n'a �t� trouv�e lors du premier passage de la boucle, tente maintenant de prendre en compte
             * une �ventuelle puissance qui aurait �t� sp�cifi�e apr�s les unit�s (comme par exemple le '2' dans "m�"). On
             * supposera que la puissance commence soit apr�s le dernier caract�re qui n'est pas un exposant, ou soit apr�s
             * le symbole '^'.
             */
            int expStart;
            int symbolEnd=symbol.lastIndexOf(EXPONENT_SYMBOL);
            if (symbolEnd>=0) {
                // Positionne 'expStart' apr�s le symbole '^'.
                expStart = symbolEnd+1;
            } else {
                for (symbolEnd=length; --symbolEnd>=0;) {
                    if (!Utilities.isSuperScript(symbol.charAt(symbolEnd))) {
                        symbolEnd++;
                        break;
                    }
                }
                // Il n'y a pas de symbole '^' � sauter pour 'expStart'.
                expStart = symbolEnd;
            }
            /*
             * Maintenant qu'on a s�par� le symbole de l'exposant, tente d'interpr�ter l'exposant. Si l'interpr�tation
             * �choue, ou s'il n'y a pas d'exposant ou de symbole, alors ce n'est pas la peine de faire le deuxi�me
             * passage de la boucle; on fera donc un "break".
             */
            if (symbolEnd>=1 && expStart<length) {
                final String powerText; {
                    final StringBuffer tmp=new StringBuffer(symbol.substring(expStart));
                    for (int i=tmp.length(); --i>=0;) tmp.setCharAt(i, Utilities.toNormalScript(tmp.charAt(i)));
                    powerText=tmp.toString();
                }
                symbol=symbol.substring(0, symbolEnd);
                try {
                    power=Integer.parseInt(powerText);
                } catch (NumberFormatException exception) {
                    // TODO: le message d'erreur de 'Unit.getUnit(String)' n'est pas
                    //       vraiment appropri� lorsqu'on retourne 'powerText'.
                    return (set.size()==initialSize) ? powerText : null;
                }
            } else {
                return (set.size()==initialSize) ? symbol : null;
            }
        }
    }
    
    /**
     * S�lectionne une unit�. Cette m�thode est appel�e automatiquement par la m�thode
     * {@link #parse} si elle a trouv� plusieurs unit�s qui utilisent le m�me symbole.
     * L'impl�mentation par d�faut tentera de retourner de pr�f�rence une unit� de la
     * classe {@link BaseUnit} ou {@link DerivedUnit}.
     * Les classes d�riv�es peuvent red�finir cette m�thode pour s�lectionner une unit�
     * selon d'autres crit�res, par exemple en demandant � l'utilisateur de choisir.
     *
     * @param  units Liste d'unit�s parmi lesquelles il faut faire un choix.
     *         La longueur de ce tableau sera d'au moins 2.
     * @return Unit� choisie. Il n'est pas obligatoire que cette unit� fasse
     *         partie du tableau <code>units</code> original.
     */
    protected Unit selectUnit(final Unit[] units) {
        for (int i=0; i<PRIORITIES.length; i++) {
            final Class c=PRIORITIES[i];
            for (int j=0; j<units.length; j++) {
                final Unit u=units[j];
                if (c.isAssignableFrom(u.getClass())) {
                    return u;
                }
            }
        }
        return units[0];
    }
}
