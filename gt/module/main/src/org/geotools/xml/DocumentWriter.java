
package org.geotools.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;

import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Schema;
import org.geotools.xml.schema.Type;
import org.xml.sax.Attributes;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class DocumentWriter {
    public static final Logger logger = Logger.getLogger("net.refractions.xml.write");
    public static final String WRITE_SCHEMA = "DocumentWriter_WRITE_SCHEMA";
    
    public static void writeDocument(Object value, Schema schema, File f, Map hints) throws OperationNotSupportedException, IOException{
        if(f==null || (!f.canWrite()))
            throw new IOException("Cannot write to "+f);
        if(hints.containsKey(WRITE_SCHEMA)){
            Map hints2 = new HashMap(hints);
            hints2.remove(WRITE_SCHEMA);
            File f2 = new File(f.getParentFile(),f.getName().substring(0,f.getName().indexOf("."))+".xsd");
            if(!writeSchema(schema,f2,hints2))
                throw new IOException("Schema Cannot be written to "+f2);
        }
        
        Writer wf = new FileWriter(f);
        WriterContentHandler wch = new WriterContentHandler(schema,wf); // should deal with xmlns declarations
        Element[] elems = schema.getElements();
        if(elems == null)
            throw new IOException("Cannot write for Schema "+schema.getTargetNamespace());
        wch.startDocument();
        for(int i=0;i<elems.length;i++){
            if(elems[i] !=null && elems[i].getType()!=null){
                Type t = elems[i].getType();
                if(t.canEncode(elems[i],value,hints)){
                    t.encode(elems[i],value,wch,hints);
            	}
            }
        }
        wch.endDocument();
    }
    
    private static class WriterContentHandler implements PrintHandler{
        private boolean firstElement = true; // needed for NS declarations
        private Writer writer;
        private Map prefixMappings; // when the value is null it has not been included yet into the document ...
        private Schema schema;
        
        public WriterContentHandler(Schema schema, Writer writer){
            this.writer = writer;
            this.schema = schema;
            prefixMappings = new HashMap();
            prefixMappings.put(schema.getTargetNamespace(),"");
            Schema[] imports = schema.getImports();
            if(imports!=null)
                for(int i=0;i<imports.length;i++)
                    prefixMappings.put(imports[i].getTargetNamespace(), imports[i].getPrefix());
        }
        
        private void printXMLNSDecs() throws IOException{
            writer.write(" xmlns=\""+schema.getTargetNamespace()+"\"");
            Schema[] imports = schema.getImports();
            String s = "";
            if(imports!=null)
                for(int i=0;i<imports.length;i++){
                    writer.write(" xmlns:"+imports[i].getPrefix()+"=\""+imports[i].getTargetNamespace()+"\"");
                    if(imports[i].getURI()!= null && imports[i].getURI().isAbsolute())
                        s += " "+imports[i].getTargetNamespace()+" "+imports[i].getURI();
                }
            s = s.trim();
            if(!"".equals(s)){
                writer.write(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
                writer.write(" xsi:schemaLocation=\""+s+"\"");
            }
        }

        public void startElement(String namespaceURI, String localName, Attributes attributes) throws IOException {
                String prefix = (String)prefixMappings.get(namespaceURI);
                if(prefix!=null && !prefix.equals(""))
                    prefix = prefix+":";
                
                writer.write("<");
                writer.write(prefix+localName);

                if(firstElement){
                    printXMLNSDecs();
                    firstElement = false;
                }
                
                if(attributes!=null)
                for(int i=0;i<attributes.getLength();i++){
                    String name = attributes.getLocalName(i);
                    String value = attributes.getValue(i);
                    writer.write("");
                    writer.write(name);
                    writer.write("=\"");
                    writer.write(value);
                    writer.write("\"");
                }
                
                writer.write(">");
        }
        
        public void element(String namespaceURI, String localName, Attributes attributes) throws IOException {
                String prefix = (String)prefixMappings.get(namespaceURI);
                if(prefix!=null && !prefix.equals(""))
                    prefix = prefix+":";
                
                writer.write("</");
                writer.write(prefix+localName);

                if(firstElement){
                    printXMLNSDecs();
                    firstElement = false;
                }
                
                if(attributes!=null)
                for(int i=0;i<attributes.getLength();i++){
                    String name = attributes.getLocalName(i);
                    String value = attributes.getValue(i);
                    writer.write("");
                    writer.write(name);
                    writer.write("=\"");
                    writer.write(value);
                    writer.write("\"");
                }
                
                writer.write(">");
        }

        public void endElement(String namespaceURI, String localName) throws IOException {
                String prefix = (String)prefixMappings.get(namespaceURI);
                if(prefix!=null && !prefix.equals(""))
                    prefix = prefix+":";
                
                writer.write("</");
                writer.write(prefix+localName);
                writer.write(">");
        }

        public void characters(char[] arg0, int arg1, int arg2) throws IOException {
                writer.write(arg0,arg1,arg2);
        }

        public void characters(String s) throws IOException {
                writer.write(s);
        }

        /**
         * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
         */
        public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws IOException {
                writer.write(arg0,arg1,arg2);
        }

        public void startDocument() throws IOException{
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        }

        public void endDocument() throws IOException {
                writer.close();
        }
    }
    
    public static boolean writeSchema(Schema schema, File f, Map hints){
        // TODO fill me in
        return true;
    }
}
