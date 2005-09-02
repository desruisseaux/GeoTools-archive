package test.crs;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.geowidgets.crs.widgets.swing.JUnitComboBox;
import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.units.model.EPSG_UnitModel;
import org.geowidgets.units.model.IUnitModel;

/** Test_SwingUnitCombo
 * 
 * @author Matthias Basler
 */
public class Test_SwingUnitCombo {

    public static void main(String[] args) {
        final JLabel label = new JLabel();
        
        boolean sorted = true;
        
        JFrame frame = new JFrame("Unit dropdown test.");
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridLayout(9,1));//Size for four elements
        
        frame.add(new JLabel("Units implementation via GeoTools - linear units."));
        final JUnitComboBox ucb1 = new JUnitComboBox(IUnitModel.UNIT_LINEAR, sorted);
        ucb1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                label.setText("Symbol: " + ucb1.getSelectedUnit().toString());
            }
        });
        frame.add(ucb1);
        
        frame.add(new JLabel("Units implementation via GeoTools - angular units."));
        final JUnitComboBox ucb2 = new JUnitComboBox(IUnitModel.UNIT_ANGULAR, sorted); 
        ucb2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                label.setText("Symbol: " + ucb2.getSelectedUnit().toString());
            }
        });
        frame.add(ucb2);
        frame.add(label);
        
        try{
            GWFactoryFinder.setUnitModel(EPSG_UnitModel.getDefault());
            
            frame.add(new JLabel("EPSG implementation via GeoTools - linear units."));
            final JUnitComboBox ucb3 = new JUnitComboBox(IUnitModel.UNIT_LINEAR, sorted);
            ucb3.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    label.setText("Symbol: " + ucb3.getSelectedUnit().toString());
                }
            });
            frame.add(ucb3);
            
            frame.add(new JLabel("EPSG implementation via GeoTools - angular units."));
            final JUnitComboBox ucb4 = new JUnitComboBox(IUnitModel.UNIT_ANGULAR, sorted); 
            ucb4.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    label.setText("Symbol: " + ucb4.getSelectedUnit().toString());
                }
            });
            frame.add(ucb4);
            frame.add(label);
        } catch (Exception e){e.printStackTrace();}
        
        
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
