/*
 * Created on Jul 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.vpf;


class ColumnPair
{
	public VPFColumn column1;
	public VPFColumn column2;
	public ColumnPair(VPFColumn c1, VPFColumn c2)
	{
		column1 = c1;
		column2 = c2;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// Done because of the equals method being overwritten
		// Not that I have any idea what I am doing...
		int result = 0;
		result += column1.hashCode() + (column1.hashCode()<<1);
		result += column2.hashCode() + (column2.hashCode()<<2);
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		boolean result = false;
		if(arg0 == this)
		{
			result = true;
		}
		else {
			ColumnPair columnPair = (ColumnPair)arg0;
			if((columnPair != null) && columnPair.column1.equals(this.column1) && columnPair.column2.equals(this.column2))
			{
				result = true;
			}
		}
		return result;
	}
}