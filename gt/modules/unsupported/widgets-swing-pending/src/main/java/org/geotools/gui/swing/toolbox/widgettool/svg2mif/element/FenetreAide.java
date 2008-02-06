package org.geotools.gui.swing.toolbox.widgettool.svg2mif.element;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FenetreAide extends JFrame {

	JTextArea jtaAide;
	JScrollPane jspAide;
	
	public FenetreAide() {
		int i;
		initComponents();
		jtaAide.setText(jtaAide.getText() + Messages.getString("Aide.2"));
		for (i = 3; i < 23; i++) {
			jtaAide.setText(jtaAide.getText() + "\n" + Messages.getString("Aide." + i));
		}
		jtaAide.setCaretPosition(0);
	}

	private void initComponents() {
		setTitle(Messages.getString("Aide.1"));
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

		this.setPreferredSize(new Dimension(550, 260));
		jtaAide = new JTextArea("", 10, 20);
		jtaAide.setWrapStyleWord(true);
		this.getContentPane().add(jtaAide);
		jspAide = new JScrollPane(jtaAide,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.getContentPane().add(jspAide);
		this.pack();
	}
}
