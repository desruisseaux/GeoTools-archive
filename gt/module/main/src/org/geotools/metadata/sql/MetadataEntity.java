/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.metadata.sql;

// J2SE dependencies
import java.util.Map;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;


/**
 * A metadata entity which implements (indirectly) metadata
 * interfaces like {@link org.opengis.metadata.MetaData},
 * {@link org.opengis.metadata.citation.Citation}, etc.
 *
 * Any call to a method in a metadata interface is redirected toward the
 * {@link #invoke} method. This method use reflection in order to find
 * the caller's method and class name. The class name is translated into
 * a table name, and the method name is translated into a column name.
 * Then the information is fetch in the underlying metadata database.
 *
 * @version $Id$
 * @author Toura�vane
 * @author Martin Desruisseaux
 */
final class MetadataEntity implements InvocationHandler {
    /**
     * The identifier used in order to locate the record for
     * this metadata entity in the database. This is usually
     * the primary key in the table which contains this entity.
     */
    private final int identifier;

    /**
     * The connection to the database. All metadata entities
     * created from a single database should share the same source.
     */
    private final MetadataSource source;
    
    /**
     * Creates a new metadata entity.
     *
     * @param identifier The identifier used in order to locate the record for
     *                   this metadata entity in the database. This is usually
     *                   the primary key in the table which contains this entity.
     * @param source     The connection to the table which contains this entity.
     */
    public MetadataEntity(final int identifier, final MetadataSource source) {
        this.identifier = identifier;
        this.source     = source;
    }

    /**
     * Invoked when any method from a metadata interface is invoked.
     *
     * @param proxy  The object on which the method is invoked.
     * @param method The method invoked.
     * @param args   The argument given to the method.
     */
    public Object invoke(final Object proxy,
                         final Method method,
                         final Object[] args)
    {
        final Class type = method.getDeclaringClass();
        if (type.getName().startsWith(source.metadataPackage)) {
            if (args!=null && args.length!=0) {
                // TODO: localize
                throw new MetadataException("Unexpected argument");
            }
            /*
             * The method invoked is a method from the metadata interface.
             * Consequently, the information should exists in the underlying
             * database.
             */
            try {
                return source.getValue(type, method, identifier);
            } catch (SQLException e) {
                throw new MetadataException(e);
            }
        } else {
            /*
             * The method invoked is a method inherit from a parent class,
             * like Object.toString() or Object.hashCode(). This information
             * is not expected to exists in the database. Forward the call
             * to the proxy object.
             */
            try {
                return method.invoke(proxy, args);
            } catch (IllegalAccessException e) {
                throw new MetadataException(e);
            } catch (InvocationTargetException e) {
                throw new MetadataException(e);
            }
        }
    }
}
