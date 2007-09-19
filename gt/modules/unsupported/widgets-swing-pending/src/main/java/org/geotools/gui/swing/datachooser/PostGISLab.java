/*
 * PostGISLab.java
 * 
 * Created on 19 sept. 2007, 22:14:48
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.datachooser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.FilterTransformer;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.gui.swing.ProgressWindow;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.filter.Filter;

public class PostGISLab {
	
	public static void main(String[] args) throws Exception {
		DataStore dataStore = getDatabase(args);
		String[] typeNames = dataStore.getTypeNames();
		if( typeNames == null ){
			JOptionPane.showConfirmDialog(null,"Could not conntect");
			System.exit(0);
		}
		String typeName = typeNames[0];

		System.out.println("Reading content " + typeName);
		FeatureSource featureSource = dataStore.getFeatureSource(typeName);

		FeatureType schema = featureSource.getSchema();
		System.out.println("Header: "+DataUtilities.spec( schema ));
		
		JQuery dialog = new JQuery( dataStore );
		dialog.show();
		dialog.dispose();
		System.exit(0);
	}

	private static DataStore getDatabase(String[] args) throws IOException {
		PostGISDialog dialog;
		
		if (args.length == 0){
			dialog = new PostGISDialog();
		}
		else {
			File file = new File( args[0] );
			if (!file.exists()){
				throw new FileNotFoundException( file.getAbsolutePath() );
			}
			Reader reader = new FileReader( file );
			Properties config = new Properties();			
			config.load(reader);
			
			dialog = new PostGISDialog( config );
		}		
		dialog.show();
		Map properties = dialog.getProperties();
		dialog.dispose();

		if( properties == null){
			System.exit(0);
		}
		return DataStoreFinder.getDataStore( properties );
	}
	
	static class JQuery extends JDialog {
		final DataStore dataStore;
		
		JTextArea query;
		JTextArea show;
		JButton selectButton;
		JButton closeButton;
		JComboBox typeNameSelect;
		JButton schemaButton;

		private JButton filterButton;
		
		JQuery( DataStore database ) throws IOException {
			this.dataStore = database;
			setTitle("Query");
			setModal( true );
			setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE );
			
			setLayout( new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
						
			Vector options = new Vector();
			String typeNames[] = dataStore.getTypeNames();
			for( int i=0; i < typeNames.length; i++ ){
				String typeName = typeNames[i];
				options.add( typeName );
			}
			typeNameSelect = new JComboBox( options );
			c.gridx=0;
			c.gridy=0;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			add( typeNameSelect, c );
						
			schemaButton = new JButton("Describe Schema");
			schemaButton.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {
						String typeName = (String) typeNameSelect.getSelectedItem();
						FeatureType schema = dataStore.getSchema( typeName );
						display( schema );
					} catch (Throwable t ){
						display( t );
					}
				}
			});
			c.gridx=GridBagConstraints.RELATIVE;
			add( schemaButton, c );
			
			query = new JTextArea(5,80);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 5;
			c.fill = GridBagConstraints.BOTH;
			JScrollPane scrollPane1 = new JScrollPane(query);
			scrollPane1.setPreferredSize( new Dimension(600,100));
			add( scrollPane1, c);
			
			selectButton = new JButton("Select Features");
			selectButton.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {
						String text = query.getText();
						FeatureCollection features = filter( text );	
						display( features );
					} catch (Throwable t ){
						display( t );
					}
				}
			});
			c.gridx=0;
			c.gridy=2;
			c.gridheight=1;
			c.gridwidth=1;			
			add( selectButton, c );
		
			filterButton = new JButton("CQL to Filter 1.0");
			filterButton.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {
						String text = query.getText();
						Filter filter = CQL.toFilter(text);
							
						display( filter );
					} catch (Throwable t ){
						display( t );
					}
				}
			});
			c.gridx=1;
			add( filterButton, c );
		
			closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}				
			});
			c.gridx=3;
			c.gridy=4;
			add( closeButton, c );

			show = new JTextArea(40,80);
			show.setTabSize(2);
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 3;			
			c.gridheight = 1;
			c.gridwidth = 4;
			c.fill = GridBagConstraints.BOTH;
			JScrollPane scrollPane2 = new JScrollPane(show);
			scrollPane2.setPreferredSize( new Dimension(600,400));
			add( scrollPane2, c);
			
			this.pack();
		}		
		
		protected void display(Filter filter) throws Exception {
			StringBuffer buf = new StringBuffer();
			FilterTransformer transform = new FilterTransformer();
			transform.setIndentation(2);
			String xml = transform.transform( filter );
			
			show.setText( xml );
		}

		public void display(FeatureType schema) {
			if( schema == null ){
				show.setText("");
				return;
			}
			StringBuffer buf = new StringBuffer();
			buf.append("typeName=");
			buf.append( schema.getTypeName() );
			
			buf.append(" namespace=");
			buf.append( schema.getNamespace() );
			buf.append("attributes = ([\n");
			
			for( int index=0; index<schema.getAttributeCount(); index++ ){
				AttributeType type = schema.getAttributeType( index );
				buf.append( type.getLocalName() );
				buf.append(" [\n");					
				
				buf.append("\t binding=");
				buf.append( type.getBinding() );
				buf.append("\n");
				
				buf.append("\t minOccurs=");
				buf.append( type.getMinOccurs() );
				buf.append(" maxOccurs=");
				buf.append( type.getMaxOccurs());
				buf.append(" nillable=");
				buf.append( type.isNillable() );				
				buf.append("\n");
				
				buf.append("\t restrictions=");
				buf.append( type.getRestriction() );
				buf.append("\n");
				
				if( type instanceof GeometryAttributeType ){
					GeometryAttributeType geomType = (GeometryAttributeType) type;
					buf.append("\t crs=");
					if( geomType.getCoordinateSystem() == null ){
						buf.append("null");						
					}
					else {
						buf.append( geomType.getCoordinateSystem().getName() );	
					}					
					buf.append("\n");						
				}
				buf.append("]\n");
			}	
			buf.append(")");
			show.setText( buf.toString() );
		}

		public FeatureCollection filter(String text ) throws Exception {
			Filter filter; 
			filter = CQL.toFilter( text );
			
			String typeName = (String) typeNameSelect.getSelectedItem();
			FeatureSource table = dataStore.getFeatureSource( typeName );
			return table.getFeatures(filter);
		}
		
		protected void display(FeatureCollection features) throws Exception {
			if( features == null ){
				show.setText("");
				return;
			}
			final StringBuffer buf = new StringBuffer();
			final FeatureType schema = features.getSchema();
			buf.append( DataUtilities.spec( schema ));
			buf.append("\n");
			features.accepts( new FeatureVisitor(){
				public void visit(Feature feature) {
					buf.append( feature.getID() );
					buf.append(" [\n");
					for( int index =0; index < schema.getAttributeCount(); index++ ){
						AttributeType type = schema.getAttributeType( index );
						String name = type.getLocalName();
						buf.append("\t");
						buf.append( name );
						buf.append( "=" );
						buf.append( feature.getAttribute( name ) );
					}
					buf.append("]");					
				}

                public void init(FeatureCollectionType arg0) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                public void visit(org.opengis.feature.Feature arg0) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
			}, new ProgressWindow( this ));
			
			show.setText( buf.toString() );
		}
		public void display(Throwable t ){
			show.setText( t.getLocalizedMessage() );
			show.setForeground( Color.RED );
		}
	}
}
