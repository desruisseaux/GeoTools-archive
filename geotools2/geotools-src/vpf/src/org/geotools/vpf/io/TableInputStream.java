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

package org.geotools.vpf.io;

import java.io.ByteArrayInputStream;
import java.io.PushbackInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.geotools.vpf.TableColumnDef;
import org.geotools.vpf.TableHeader;
import org.geotools.vpf.TableRow;
import org.geotools.vpf.RowField;
import org.geotools.vpf.VPFDate;
import org.geotools.vpf.exc.VPFHeaderFormatException;
import org.geotools.vpf.exc.VPFRowDataException;
import org.geotools.vpf.ifc.DataTypesDefinition;
import org.geotools.vpf.ifc.FileConstants;

/**
 * TableInputStream.java
 *
 *
 * Created: Thu Jan 02 22:32:27 2003
 *
 * @author <a href="mailto:kobit@users.sf.net">Artur Hefczyc</a>
 * @version 1.0
 */
public class TableInputStream extends InputStream
  implements FileConstants, DataTypesDefinition
{
  public static final int AHEAD_BUFFER_SIZE = 0;
  
  protected PushbackInputStream input = null;
  protected TableHeader header = null;
  protected List rowsReadAhead = new LinkedList();
  
  public TableInputStream(File file)
    throws IOException
  {
    input = new PushbackInputStream(new FileInputStream(file));
    readHeader();
  }

  public TableInputStream(FileDescriptor fdObj)
    throws IOException
  {
    input = new PushbackInputStream(new FileInputStream(fdObj));
    readHeader();
  }

  public TableInputStream(String file)
    throws IOException
  {
    input = new PushbackInputStream(new FileInputStream(file));
    readHeader();
  }

  public TableHeader getHeader()
  {
    return header;
  }

  protected void readHeader()
    throws IOException
  {
    byte[] fourBytes = new byte[4];
    int res = input.read(fourBytes);
    char order = readChar();
    if (order == LITTLE_ENDIAN_ORDER)
    {
      fourBytes = toBigEndian(fourBytes);
    } // end of if (order == LITTLE_ENDIAN_ORDER)
    int length = decodeInt(fourBytes);
    char ctrl = readChar();
    if (ctrl != VPF_RECORD_SEPARATOR)
    {
      throw new VPFHeaderFormatException("Header format does not fit VPF"+
                                         " file definition.");
    } // end of if (ctrl != VPF_RECORD_SEPARATOR)
    String description = readString(""+VPF_RECORD_SEPARATOR);
    String narrativeTable = readString(""+VPF_RECORD_SEPARATOR);
    LinkedList colDefs = new LinkedList();
    TableColumnDef colDef = readColumnDef();
    while (colDef != null)
    {
	  //	  System.out.println(colDef.toString());
      colDefs.add(colDef);
      ctrl = readChar();
      if (ctrl != VPF_FIELD_SEPARATOR)
      {
        throw new VPFHeaderFormatException("Header format does not fit VPF"+
                                           " file definition.");
      } // end of if (ctrl != VPF_RECORD_SEPARATOR)
      colDef = readColumnDef();
    } // end of while (colDef != null)
    if (colDefs.size() == 0)
    {
      colDefs = null;
    } // end of if (colDefs.size() == 0)
    header = new TableHeader(length, order, description,
                             narrativeTable, colDefs);
  }

  public TableColumnDef readColumnDef()
    throws IOException, NumberFormatException
  {
    char ctrl = readChar();
    if (ctrl == VPF_RECORD_SEPARATOR)
    {
      return null;
    } // end of if (ctrl == VPF_RECORD_SEPARATOR)
    String name = ctrl + readString("=");
    char type = readChar();
    ctrl = readChar();
    if (ctrl != VPF_ELEMENT_SEPARATOR)
    {
      throw new VPFHeaderFormatException("Header format does not fit VPF"+
                                         " file definition.");
    } // end of if (ctrl != VPF_RECORD_SEPARATOR)
    int elements = Integer.parseInt(readString(""+VPF_ELEMENT_SEPARATOR));
    char key = readChar();
    ctrl = readChar();
    if (ctrl != VPF_ELEMENT_SEPARATOR)
    {
      throw new VPFHeaderFormatException("Header format does not fit VPF"+
                                         " file definition.");
    } // end of if (ctrl != VPF_RECORD_SEPARATOR)
    String colDesc =
	  readString(""+VPF_ELEMENT_SEPARATOR+VPF_FIELD_SEPARATOR);
    String descTableName =
	  readString(""+VPF_ELEMENT_SEPARATOR+VPF_FIELD_SEPARATOR);
    String indexFile =
	  readString(""+VPF_ELEMENT_SEPARATOR+VPF_FIELD_SEPARATOR);
    String narrTable =
	  readString(""+VPF_ELEMENT_SEPARATOR+VPF_FIELD_SEPARATOR);
    return new TableColumnDef(name, type, elements, key, colDesc,
                              descTableName, indexFile, narrTable);
  }

  public TableRow readRow()
    throws IOException
  {
	List rowsDef = header.getColumnDefs();
    RowField[] fieldsArr = new RowField[rowsDef.size()];
    HashMap fieldsMap = new HashMap();
	for (int i = 0; i < rowsDef.size(); i++) {
	  TableColumnDef tcd = (TableColumnDef)rowsDef.get(i);
	  byte[] bytes = new byte[tcd.getColumnSize()];
	  int size = input.read(bytes);
      if (size <= 0)
      {
        return null;
      } // end of if (size = 0)
	  if (size != tcd.getColumnSize())
	  {
		throw new VPFRowDataException("Insuffitient data in stream: is "+size+
									  " should be: "+tcd.getColumnSize());
	  } // end of if (size != tcd.getColumnSize())
	  if (tcd.isNumeric() && header.getByteOrder() == LITTLE_ENDIAN_ORDER)
	  {
		bytes = toBigEndian(bytes);
	  } // end of if (tcd.isNumeric() &&
	    //header.getByteOrder() == LITTLE_ENDIAN_ORDER)
	  Object value = decodeData(bytes, tcd.getType());
      RowField field = new RowField(value, tcd.getType());
      fieldsArr[i] = field;
      fieldsMap.put(tcd.getName(), field);
	} // end of for (int i = 0; i < rowsDefs.size(); i++)
    return new TableRow(fieldsArr, fieldsMap);
  }

  public int readRows(TableRow[] rows)
  {
    return 0;
  }

  public int read()
  {
    return -1;
  }

  public int availableRows()
  {
    return rowsReadAhead.size();
  }

  protected char readChar()
    throws IOException
  {
    return (char)input.read();
  }

  protected String readString(String terminators)
    throws IOException
  {
    StringBuffer text = new StringBuffer();
    char ctrl = readChar();
	if (terminators.indexOf(ctrl) != -1)
	{
	  if (ctrl == VPF_FIELD_SEPARATOR) {
		input.unread(ctrl);
	  } // end of if (ctrl == VPF_RECORD_SEPARATOR)
	  return null;
	}
    while (terminators.indexOf(ctrl) == -1)
    {
      text.append(ctrl);
      ctrl = readChar();
    } // end of while (terminators.indexOf(ctrl) != -1)
    if (text.toString().equals(STRING_NULL_VALUE))
    {
      return null;
    } // end of if (text.equals("null"))
    else
    {
      return text.toString();
    } // end of if (text.equals("null")) else
  }

  public static byte[] toBigEndian(byte[] source)
  {
	byte[] result = new byte[source.length];
	for (int i = 0; i < source.length; i++)
	{
	  result[i] = source[source.length - (i+1)];
	} // end of for (int i = 0; i < source.length; i++)
	return result;
  }

  public Object decodeData(byte[] bytes, char type)
    throws IOException
  {
	Object result = null;
	switch (type) {
	  case DATA_TEXT:
	  case DATA_LEVEL1_TEXT:
	  case DATA_LEVEL2_TEXT:
	  case DATA_LEVEL3_TEXT:
		StringBuffer sb = new StringBuffer(bytes.length);
		for (int i = 0; i < bytes.length; i++) {
		  sb.append((char)bytes[i]);
		} // end of for (int i = 0; i < bytes.length; i++)
        boolean isNull = false;
        for (int i = 0; i < STRING_NULL_VALUES.length; i++)
        {
          isNull |=
			sb.toString().trim().equalsIgnoreCase(STRING_NULL_VALUES[i]);
        } // end of for (int i = 0; i < STRING_NULL_VALUES.length; i++)
        if (isNull)
        {
          result = null;
        } // end of if (isNull)
        else
        {
          result = sb.toString();
        } // end of else
		break;
	  case DATA_SHORT_FLOAT:
		result = new Float(decodeFloat(bytes));
		break;
	  case DATA_LONG_FLOAT:
		result = new Double(decodeDouble(bytes));
		break;
	  case DATA_SHORT_INTEGER:
		result = new Short(decodeShort(bytes));
		break;
	  case DATA_LONG_INTEGER:
		result = new Integer(decodeInt(bytes));
		break;
	  case DATA_2_COORD_F:

		break;
	  case DATA_2_COORD_R:

		break;
	  case DATA_3_COORD_F:

		break;
	  case DATA_3_COORD_R:

		break;
	  case DATA_DATE_TIME:
		result = new VPFDate(bytes);
		break;
	  case DATA_NULL_FIELD:

		break;
	  case DATA_TRIPLED_ID:

	  default:
		break;
	} // end of switch (tcd.getType())
	return result;
  }

  public static short decodeShort(byte[] bytes)
    throws IOException
  {
	DataInputStream dis =
	  new DataInputStream(new ByteArrayInputStream(bytes));
	return dis.readShort();
  }

  public static int decodeInt(byte[] bytes)
    throws IOException
  {
	DataInputStream dis =
	  new DataInputStream(new ByteArrayInputStream(bytes));
	return dis.readInt();
  }
  
  public static float decodeFloat(byte[] bytes)
    throws IOException
  {
	DataInputStream dis =
	  new DataInputStream(new ByteArrayInputStream(bytes));
	return dis.readFloat();
  }
  
  public static double decodeDouble(byte[] bytes)
    throws IOException
  {
	DataInputStream dis =
	  new DataInputStream(new ByteArrayInputStream(bytes));
	return dis.readDouble();
  }
  
  //   public static int littleEndianToInt(byte[] fourBytes)
  //   {
  //     int res = 0;
  //     int limit = Math.min(fourBytes.length, 4);
  //     for (int i = 0; i < limit-1; i++)
  //     {
  //       res += unsigByteToInt(fourBytes[i]) << (i*8);
  //     } // end of for (int i = 0; i < limit-1; i++)
  // 	res += (int)fourBytes[i] << (i*8);
  //     return res;
  //   }

  //   public static int bigEndianToInt(byte[] fourBytes)
  //   {
  //     int res = 0;
  //     int limit = Math.min(fourBytes.length, 4);
  // 	res += (int)fourBytes[0] << ((limit-1)*8);
  //     for (int i = 1; i < limit; i++)
  //     {
  //       res += unsigByteToInt(fourBytes[i]) << ((limit-(i+1))*8);
  //     } // end of for (int i = 0; i < limit-1; i++)
  //     return res;
  //   }

  //   public static int unsigByteToInt(byte b)
  //   {
  //     return (int) b & 0xFF;
  //   }

  public static void main(String[] args)
    throws IOException
  {
    if (args.length != 1)
    {
      System.out.println("Put valid file name as parameter.");
      System.exit(1);
    } // end of if (args.length <> 1)
    TableInputStream testInput = new TableInputStream(args[0]);
    TableHeader testHeader = testInput.getHeader();
    System.out.println(testHeader.toString());
	List fieldDefs = testHeader.getColumnDefs();
    TableRow row = testInput.readRow();
    int counter = 0;
    while (row != null)
    {
	  for (int i = 0; i < fieldDefs.size(); i++) {
		TableColumnDef tcd = (TableColumnDef)fieldDefs.get(i);
		System.out.println(tcd.getName()+"="+row.get(i).toString());
	  } // end of for (int i = 0; i < fieldDefs.size(); i++)
      row = testInput.readRow();
    } // end of while (row != null)
  } // end of main()
  
} // TableInputStream
