/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.gui.swing.propertyedit.styleproperty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.transform.TransformerException;

import org.geotools.gui.swing.i18n.TextBundle;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.filtre.FiltreSLD;
import org.geotools.gui.swing.propertyedit.PropertyPanel;
import org.geotools.map.MapLayer;
import org.geotools.sld.SLDConfiguration;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;




/**
 *
 * @author  johann sorel
 */
public class JXMLStylePanel extends javax.swing.JPanel implements PropertyPanel {

    private MapLayer layer;

    /** Creates new form XMLStylePanel */
    public JXMLStylePanel() {
        initComponents();
        lbl_check.setText(TextBundle.getResource().getString("checkstyle"));
        but_check.setText(TextBundle.getResource().getString("check"));
        but_import.setText(TextBundle.getResource().getString("import"));
        but_export.setText(TextBundle.getResource().getString("export"));
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        but_check = new javax.swing.JButton();
        lbl_check = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        editpane = new javax.swing.JTextPane();
        but_export = new javax.swing.JButton();
        but_import = new javax.swing.JButton();

        but_check.setText("jButton1");
        but_check.setEnabled(false);
        but_check.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionCheck(evt);
            }
        });

        lbl_check.setText("jLabel1");

        jScrollPane1.setViewportView(editpane);

        but_export.setText("jButton1");
        but_export.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionExport(evt);
            }
        });

        but_import.setText("jButton2");
        but_import.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionImport(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lbl_check)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(but_check))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(but_import)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(but_export)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(but_export)
                    .add(but_import, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_check)
                    .add(but_check))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void actionCheck(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionCheck
    }//GEN-LAST:event_actionCheck

    private void actionImport(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionImport

        JFileChooser jfc = new JFileChooser();
        FiltreSLD fsld = new FiltreSLD();
        jfc.addChoosableFileFilter(fsld);
        jfc.setFileFilter(fsld);

        int ret = jfc.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            try {
                Configuration configuration = new SLDConfiguration();
                Parser parser = new Parser(configuration);

                InputStream xml = new ByteArrayInputStream(editpane.getText().getBytes());
                StyledLayerDescriptor sld = (StyledLayerDescriptor) parser.parse( xml );
                
                //layer.setStyle(sld);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, TextBundle.getResource().getString("sld_verification_error"), "", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_actionImport

    private void actionExport(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionExport
        JFileChooser jfc = new JFileChooser();
        FiltreSLD fsld = new FiltreSLD();
        jfc.addChoosableFileFilter(fsld);
        jfc.setFileFilter(fsld);

        int ret = jfc.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            
            File f = jfc.getSelectedFile();
            SLDTransformer st = new SLDTransformer();

            try {
                String xml = st.transform(layer.getStyle());
                ArrayList<String> str = new ArrayList<String>();
                str.add(xml);
                new FileUtilities().write(f.getPath(), str); 
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_actionExport

    public JComponent getPanel() {
        return this;
    }

    public void apply() {
        try {
            Configuration configuration = new SLDConfiguration();
            Parser parser = new Parser(configuration);

            //the xml instance document above
            InputStream xml = new ByteArrayInputStream(editpane.getText().getBytes());

            //parse
            StyledLayerDescriptor sld = (StyledLayerDescriptor) parser.parse( xml );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, TextBundle.getResource().getString("sld_verification_error"), "", JOptionPane.ERROR_MESSAGE);
        }
    }

    public ImageIcon getIcon() {
        return IconBundle.getResource().getIcon("CP16_mimetypes_source_s");
    }

    public String getTitle() {
        return TextBundle.getResource().getString("xml");
    }

    public void setTarget(Object layer) {
        
        if(layer instanceof MapLayer){
            this.layer = (MapLayer) layer;
            parse();
            
        }
    }
    
    private void parse(){
        SLDTransformer st = new SLDTransformer();

            try {
                String xml = st.transform(this.layer.getStyle());            
                editpane.setText(xml);
            } catch (TransformerException ex) {
                ex.printStackTrace();
            }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_check;
    private javax.swing.JButton but_export;
    private javax.swing.JButton but_import;
    private javax.swing.JTextPane editpane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_check;
    // End of variables declaration//GEN-END:variables


    public void reset() {
        parse();
    }

    public String getToolTip() {
        return "";
    }
}
/** 
 * Utility class for file operations. Made for
 * quick needs, this class is not optimised.
 * 
 * @author Johann Sorel
 */
class FileUtilities {
    
    
    public FileUtilities(){        
    }
    
    
    /** 
     * Doesn't read lines beginning with # or / and empty lines
     * 
     * @param adress : path to the file
     * @return List<String> with one String for each line
     * @throws FileNotFoundException if file doesn't exist
     * @throws IOException if an error happen while reading
     */
    public List<String> read(String adress) throws FileNotFoundException, IOException {
        List<String> str = new ArrayList<String>();

        InputStream ips = new FileInputStream(adress);
        InputStreamReader ipsr = new InputStreamReader(ips);
        BufferedReader br = new BufferedReader(ipsr);
        String l;
        char ch1 = '/';
        char ch2 = '#';

        while ((l = br.readLine()) != null) {
            if (l.length() > 0) {
                if (l.charAt(0) != ch1 && l.charAt(0) != ch2) {
                    str.add(l);
                }
            }
        }
        br.close();
        ips.close();

        return str;
    }

    /** 
     * Use to read a serialized object
     * 
     * @param adress : path to the file
     * @return Object : the serialized object
     * @throws IOException if an error happen while reading
     */
    public Object readObjet(String adress) throws IOException, ClassCastException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream(adress);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Object o = in.readObject();
        return o;
    }

    /** 
     * Same issue as java.util.Properties, faster for one value
     * but much slower when making many acces
     * 
     * @param adress : path to the file
     * @param nomparam : key
     * @return String : linked value
     * @throws IOException if an error happen while reading
     */
    public String readValue(String adress, String nomparam) throws IOException {
        String str = "";

        InputStream ips = new FileInputStream(adress);
        InputStreamReader ipsr = new InputStreamReader(ips);
        BufferedReader br = new BufferedReader(ipsr);
        String l;
        char ch1 = '/';
        char ch2 = '#';

        while ((l = br.readLine()) != null) {
            if (l.length() > 0) {
                if (l.charAt(0) != ch1 && l.charAt(0) != ch2 && l.subSequence(0, l.indexOf("=")).equals(nomparam)) {
                    str = l.substring(l.indexOf("=") + 1, l.length());
                }
            }
        }
        br.close();
        ips.close();

        return str;
    }

    /** 
     * Same issue as a propertie file, faster for one value
     * but much slower when making many acces
     * 
     * @param flux : stream
     * @param nomparam : key
     * @return String : linked value
     * @throws IOException if an error happen while reading
     */
    public String readValue(InputStream flux, String nomparam) throws IOException {
        String str = "";

        InputStreamReader ips = new InputStreamReader(flux);
        BufferedReader br = new BufferedReader(ips);
        String l;
        char ch1 = '/';
        char ch2 = '#';

        while ((l = br.readLine()) != null) {
            if (l.length() > 0) {
                if (l.charAt(0) != ch1 && l.charAt(0) != ch2 && l.subSequence(0, l.indexOf("=")).equals(nomparam)) {
                    str = l.substring(l.indexOf("=") + 1, l.length());
                }
            }
        }
        br.close();
        ips.close();

        return str;
    }

    /** 
     * Write a String List in a file
     * 
     * @param adress : path to the file
     * @param val : String List to write
     * @throws IOException if an error happen while writing
     */
    public void write(String adress, List<String> val) throws IOException {        
        FileWriter fw = new FileWriter(adress, false);
        BufferedWriter output = new BufferedWriter(fw);

        int size = val.size();
        for (int i = 0; i < size; i++) {
            output.write(val.get(i));
            output.flush();
        }

        fw.close();
        output.close();
    }

    /** 
     * Serialize an object in a file.
     * 
     * @param adress : path to the file
     * @param O : Object to serialize
     * @throws IOException if an error happen while writing
     */
    public void writeObject(String adress, Object O) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(adress);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);

        out.writeObject(O);
        out.close();
        fileOut.close();
        
    }
}