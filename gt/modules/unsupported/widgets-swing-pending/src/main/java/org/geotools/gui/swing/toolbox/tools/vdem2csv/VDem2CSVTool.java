/*
 * MNTVisualDem.java
 *
 * Created on 18 avril 2007, 19:09
 *
 * AlterSIG-Convert is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * AlterSIG-Convert is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.geotools.gui.swing.toolbox.tools.vdem2csv;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.geotools.gui.swing.misc.filtre.mnt.FiltreVDem;
import org.geotools.gui.swing.misc.filtre.txt.FiltreCSV;
import org.geotools.gui.swing.toolbox.AbstractWidgetTool;

/**
 *
 * @author  Johann
 */
public class VDem2CSVTool extends AbstractWidgetTool {
    
    private PLMNTVisualDem plvdem = new PLMNTVisualDem();
    
    /** Creates new form MNTVisualDem */
    public VDem2CSVTool() {
        initComponents();
    }
    
    
    private void testName(){
        
        
        String nom_sortie = jtf_sortie.getText();
        String nom_entree = jtf_entree.getText();
        String extension_fichier = "";
        String extension_liste = ".csv";
        String nouveaunom = "";
        int index = -1;
        
        // on essai de recuperer le georeferencement
        int[] rec = plvdem.recoverGeoref(nom_entree);
        jtf_refx.setText(String.valueOf(rec[0]));
        jtf_refy.setText(String.valueOf(rec[1]));
        
        //recuperation de l'extension dans la liste
        
        
        //si le texte en sortie est vide on le complete avec celui d'entree
        if(nom_sortie.length() == 0){
            nom_sortie = nom_entree;
        }
        
        //recuperation de l'extension du fichier en sortie
        index = nom_sortie.lastIndexOf(".");
        if (index != -1){
            extension_fichier = nom_sortie.substring(index,nom_sortie.length());
        }
        
        
        // on corrige l'extension
        if(!nom_sortie.endsWith(extension_liste)){
            
            if(nom_sortie.endsWith(File.separator)){
                nom_sortie += "sortie.csv";
            }
            
            if( !nom_sortie.equals("")){
                if ( index == -1 ) {
                    
                    nouveaunom =  nom_sortie + extension_liste;
                }else{
                    nouveaunom = nom_sortie.substring(0,index) + extension_liste;
                }
            }
            
        }else{
            nouveaunom = nom_sortie;
        }
        
        jtf_sortie.setText(nouveaunom);
        
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jtf_entree = new javax.swing.JTextField();
        but_chercher_entree = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jtf_sortie = new javax.swing.JTextField();
        but_chercher_sortie = new javax.swing.JButton();
        jbu_convertir = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jtf_refx = new javax.swing.JTextField();
        jtf_refy = new javax.swing.JTextField();
        chk_cover = new javax.swing.JCheckBox();
        jpb_attente = new javax.swing.JProgressBar();

        jLabel3.setFont(new java.awt.Font("Georgia", 3, 18));
        jLabel3.setText("VDEM to CSV"); // NOI18N
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jLabel3.setIconTextGap(40);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Fichier", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11)));

        jLabel1.setText("Fichier en entrée : ");

        jtf_entree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtf_entreeActionPerformed(evt);
            }
        });
        jtf_entree.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jtf_entreeFocusLost(evt);
            }
        });

        but_chercher_entree.setText("null");
        but_chercher_entree.setPreferredSize(new java.awt.Dimension(45, 20));
        but_chercher_entree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_chercher_entreechercherEntree(evt);
            }
        });

        jLabel2.setText("Fichier en sortie :"); // NOI18N

        jtf_sortie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtf_sortiesortieAction(evt);
            }
        });
        jtf_sortie.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jtf_sortiesortieLost(evt);
            }
        });

        but_chercher_sortie.setText("null");
        but_chercher_sortie.setPreferredSize(new java.awt.Dimension(45, 20));
        but_chercher_sortie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_chercher_sortiechercherSortie(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jtf_entree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(but_chercher_entree, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jtf_sortie, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(but_chercher_sortie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                        .add(99, 99, 99)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(but_chercher_entree, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jtf_entree, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(but_chercher_sortie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jtf_sortie, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jbu_convertir.setText("convertir"); // NOI18N
        jbu_convertir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbu_convertirActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Paramètres", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11)));

        jLabel4.setText("Géoréférencement en X :");

        jLabel5.setText("Géoréférencement en Y :");

        jtf_refx.setText("0");

        jtf_refy.setText("0");

        chk_cover.setText("Garder recouvrement"); // NOI18N
        chk_cover.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chk_cover.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtf_refx, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtf_refy, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE))
                    .add(chk_cover))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jtf_refx, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jtf_refy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(19, 19, 19)
                .add(chk_cover)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jpb_attente, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jbu_convertir)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jpb_attente, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jbu_convertir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jbu_convertirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbu_convertirActionPerformed
        jbu_convertir.setEnabled(false);
        jpb_attente.setIndeterminate(true);
        boolean b = plvdem.convert(jtf_entree.getText(),jtf_sortie.getText(),chk_cover.isSelected(),Integer.valueOf(jtf_refx.getText()).intValue(), Integer.valueOf(jtf_refy.getText()).intValue()) ;
        if( !b ){
            JOptionPane.showMessageDialog(this,"Echec conversion","Erreur",JOptionPane.ERROR_MESSAGE);
        }
        else{
            JOptionPane.showMessageDialog(this,"Conversion reussie","",JOptionPane.INFORMATION_MESSAGE);
        }
        jpb_attente.setIndeterminate(false); 
        jbu_convertir.setEnabled(true);
    }//GEN-LAST:event_jbu_convertirActionPerformed

    private void jtf_entreeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtf_entreeFocusLost
        testName();
    }//GEN-LAST:event_jtf_entreeFocusLost

    private void jtf_entreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtf_entreeActionPerformed
        testName();
    }//GEN-LAST:event_jtf_entreeActionPerformed

    private void but_chercher_sortiechercherSortie(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_chercher_sortiechercherSortie
        JFileChooser jfc = new JFileChooser();
        jfc.addChoosableFileFilter(new FiltreCSV());
        
        jfc.setFileFilter(jfc.getChoosableFileFilters()[1]);
        
        
        int val = jfc.showSaveDialog(this);
        
        if(val == JFileChooser.APPROVE_OPTION){
            File f = jfc.getSelectedFile();
            jtf_sortie.setText( f.getPath() );
            testName();
        }
    }//GEN-LAST:event_but_chercher_sortiechercherSortie

    private void jtf_sortiesortieLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jtf_sortiesortieLost
        testName();
    }//GEN-LAST:event_jtf_sortiesortieLost

    private void jtf_sortiesortieAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtf_sortiesortieAction
        testName();
    }//GEN-LAST:event_jtf_sortiesortieAction

    private void but_chercher_entreechercherEntree(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_chercher_entreechercherEntree
        JFileChooser jfc = new JFileChooser();
        jfc.addChoosableFileFilter(new FiltreVDem());
        
        jfc.setFileFilter(jfc.getChoosableFileFilters()[1]);
                
        int val = jfc.showOpenDialog(this);
        
        if(val == JFileChooser.APPROVE_OPTION){
            File f = jfc.getSelectedFile();
            jtf_entree.setText( f.getPath() );
            testName();
        }
    }//GEN-LAST:event_but_chercher_entreechercherEntree
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_chercher_entree;
    private javax.swing.JButton but_chercher_sortie;
    private javax.swing.JCheckBox chk_cover;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jbu_convertir;
    private javax.swing.JProgressBar jpb_attente;
    private javax.swing.JTextField jtf_entree;
    private javax.swing.JTextField jtf_refx;
    private javax.swing.JTextField jtf_refy;
    private javax.swing.JTextField jtf_sortie;
    // End of variables declaration//GEN-END:variables
 
    
    
    
}
