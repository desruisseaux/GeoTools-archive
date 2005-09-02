package test.crs;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;

import javax.swing.*;

import org.geowidgets.crs.widgets.swing.*;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.EllipsoidalCS;


/** Test_CRSWidgets
 * 
 * @author Matthias Basler
 */
public class Test_SwingCRSWidgets {
    public static void main(String[] args) throws Exception{
        System.out.println("Test 1: Creating a CRS assembly widget.");
        
        final JFrame frame = new JFrame("CRS assembly test.");
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
                
        String what = "PCRS";
        final _AssemblyPanel panel ;
        
        if (what.equals("PCRS")){
            final JProjectedCRSAssemblyPanel ePanel =
                new JProjectedCRSAssemblyPanel(null, 2);
            
            ePanel.setButtonsVisible(true);
            ePanel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource().equals(ePanel.b_OK)){
                        try{
                        ProjectedCRS crs = ePanel.getCRS();
                        JOptionPane.showMessageDialog(null, "You selected: " + crs.getName());
                        ePanel.setCRS(crs);
                        } catch (FactoryException e){e.printStackTrace();}
                    }
                    frame.dispose();
                    System.exit(0);
                }
            });
            panel = ePanel;}
        
        else if (what.equals("GCRS")){            
            final JGeographicCRSAssemblyPanel ePanel =
                new JGeographicCRSAssemblyPanel(null, 2);
            
            ePanel.setButtonsVisible(true);
            ePanel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource().equals(ePanel.b_OK)){
                        try{
                            GeographicCRS crs = ePanel.getCRS();
                            JOptionPane.showMessageDialog(null, "You selected: " + crs.getName());
                        } catch (FactoryException e){e.printStackTrace();}
                    }
                    frame.dispose();
                    System.exit(0);
                }
            });
            panel = ePanel;
            
        } else if (what.equals("GD")){
            JGeodeticDatumAssemblyPanel ePanel = new JGeodeticDatumAssemblyPanel(null);
            panel = ePanel;
            
        } else if (what.equals("CS")){
            JCSAssemblyPanel ePanel = new JCSAssemblyPanel(null, EllipsoidalCS.class, 2);
            panel = ePanel;
            
        } else panel = null;
        
        /*panel.setResizable(true);
        frame.add(panel);*/
        frame.add(panel, BorderLayout.NORTH);
        JPanel fillPanel = new JPanel();
        fillPanel.setPreferredSize(new Dimension(0,0));
        frame.add(fillPanel);
        panel.addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {}
            public void componentShown(ComponentEvent e) {}
            public void componentMoved(ComponentEvent e) {}
            public void componentResized(ComponentEvent e) {
                frame.pack();
            }
        
        });
        frame.pack();
        frame.setVisible(true);
        /*
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            final JFrame frame = new JFrame("CRS assembly test.");
            frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
            final _AssemblyPanel panel ;
            
            // Set the EPSG ODBC database name.
            GWFactoryFinder.setEPSGConnection(
                    GWFactoryFinder.createODBCConnection("EPSG"));
            

        } catch (Exception e){e.printStackTrace();}
        */
    }
}
