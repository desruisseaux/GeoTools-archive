/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package org.geotools.vpf.ifc;

/**
 * VPFHeader.java
 *
 *
 * Created: Mon Feb 24 22:51:07 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFHeader.java,v 1.3 2003/03/24 16:38:22 kobit Exp $
 */

public interface VPFHeader 
{

  /**
   * Returns particular <code>VPFHeader</code> length.
   *
   * @return an <code>int</code> value of header length.
   */
  int getLength();

  /**
   * Method <code><code>getRecordSize</code></code> is used to return
   * size in bytes of records stored in this table. If table keeps variable
   * length records <code>-1</code> should be returned.
   *
   * @return an <code><code>int</code></code> value
   */
  int getRecordSize();
  
}// VPFHeader
