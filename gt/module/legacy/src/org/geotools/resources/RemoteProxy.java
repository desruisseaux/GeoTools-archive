/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// J2SE dependencies
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Interface for remote object wrapping an implementation. This interface is used for
 * implementation of {@link org.opengis.cs.CS_CoordinateSystem} and its friends. Remote
 * methods are usually executed on the server side, not the client side. However, if a
 * user chooses to wrap a {@link org.opengis.cs.CS_CoordinateSystem} interface into a
 * {@link org.geotools.cs.CoordinateSystem} concrete class, then the {@link #getImplementation}
 * method may be used for faster and more accurate wrapping: we get directly the underlying
 * implementation instead of building it from many methods invocations.
 * <br><br>
 * This interface is used internally by Geotools implementations only, for optimization purpose.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated This interface was used by the legacy CTS packages, which are going to be removed.
 */
public interface RemoteProxy extends Remote {
    /**
     * Returns the underlying implementation used by this proxy.
     *
     * @return The underlying implementation.
     * @throws RemoteException if a remote call failed.
     */
    public abstract Serializable getImplementation() throws RemoteException;
}
