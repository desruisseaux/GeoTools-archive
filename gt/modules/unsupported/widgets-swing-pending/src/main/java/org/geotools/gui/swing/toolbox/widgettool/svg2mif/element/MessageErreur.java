package org.geotools.gui.swing.toolbox.widgettool.svg2mif.element;

import java.io.PrintStream;

import javax.swing.JOptionPane;

public class MessageErreur extends PrintStream {
	
	   public String MessageErreur;
	    
	    public MessageErreur(PrintStream ps) {
			super (ps);
	    }
				
		public void write(byte buf[], int off, int len) {
			try {
				if (len != 0) {
					MessageErreur += new String(buf);
				} else {
					JOptionPane.showMessageDialog(null, MessageErreur);
				}			
			} catch (Exception e) {
				e.printStackTrace();
				setError();
			}
			super.write(buf, off, len);
		}

}
