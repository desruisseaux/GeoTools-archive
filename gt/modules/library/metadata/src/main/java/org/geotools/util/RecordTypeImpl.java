package org.geotools.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.util.MemberName;
import org.opengis.util.Record;
import org.opengis.util.RecordSchema;
import org.opengis.util.RecordType;
import org.opengis.util.TypeName;

/**
 * Used to provde a record of data (in a manner similar to a strongly typed Map).
 * <p>
 * Please note that a record is *strongly* typed (and may be better thought
 * of as a mathmatical tuple). The "keys" are strictly controlled "MemberNames"
 * and are usual defined in the context of a schema.
 * </p>
 * <p>
 * What do you get for all this fun (beyond a Java Map)?
 * <ul>
 * <li>
 * </ul>
 * @author Jody Garnett
 *
 */
public class RecordTypeImpl implements RecordType {
    private TypeName typeName;
    private Map attributeTypes;
    private RecordSchema parent;
    
    /**
     * Direct constructor (all attributes are specified).
     * @param typeName
     * @param members List<MemberName>
     */
    public RecordTypeImpl( RecordSchema parent, TypeName typeName, List members ){
        this.parent = parent;
        this.typeName = typeName;
        Map attributeTypes = new HashMap();
        for( Iterator i = members.iterator(); i.hasNext(); ){
            MemberName member = (MemberName) i.next();
            attributeTypes.put( member, member.getAttributeType() );
        }
        this.attributeTypes = Collections.unmodifiableMap( attributeTypes );
    }
    /**
     * Direct constructor (all attributes are specified).
     * @param typeName
     * @param attributeTypes
     */
    public RecordTypeImpl( RecordSchema parent, TypeName typeName, Map attributeTypes ){
        this.parent = parent;
        this.typeName = typeName;
        this.attributeTypes = Collections.unmodifiableMap( attributeTypes );
    }
    /**
     * Dictionary of name / type pairs for this record type.
     * <p>
     * @return Map<MemberName,TypeName>
     */
    public Map getAttributeTypes() {
        return attributeTypes;
    }
    /**
     * Returns parent schema that defines this type.
     * <p>
     * Please note that getContainer().getNamespace() should contain
     * our value for getTypeName().
     * </p>
     */
    public RecordSchema getContainer() {
        return parent;
    }

    /**
     * Set up MemberName valid for getAttributeTypes.
     */
    public Set getMembers() {
        return getAttributeTypes().keySet();
    }
    /**
     * Gets the typeName that identified this record type.
     * <p>
     * A couple of consequences for you to think about:
     * <ul>
     * <li>The typeName is defined in a RecordSchema
     * <li>The typeName is valid in the namespace for the RecordSchema
     * </ul>
     * Even though these things are "names" they are also the real object
     * (think of them as "named" things if that helps).
     */
    public TypeName getTypeName() {
        return typeName;
    }
    /**
     * Check if this record can work with us.
     * <p>
     * The following should hold true:
     * <ul>
     * <li>getMembers().equals( record.getAttributes().keySet() )
     * <li>you can add more conditions in a subclass
     * </p>
     * I kind of wish that the record just had all the members present
     * but what can you do (perhaps the spec has a reason).
     * @return true of record attributes line up with getMembers
     */
    public boolean isInstance( Record record ) {
        return getMembers().equals( record.getAttributes().keySet() );
    }
    /**
     * Looks up attribute name and returns associated TypeName.
     * <p>
     * This is good a look up mechanism ...but it makes no sense
     * as memberName.getAttributeType() has the exact same
     * information?
     */
    public TypeName locate( MemberName memberName ) {
        return (TypeName) getAttributeTypes().get( memberName );
    }

}
