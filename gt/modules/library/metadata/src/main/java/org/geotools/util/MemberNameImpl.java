package org.geotools.util;

import org.opengis.util.MemberName;
import org.opengis.util.TypeName;

/**
 * MemberName is used in a Record as part of a Map<MemberName,TypeName>.
 * <p>
 * It may be more simple to think of MemberName *as* a Map.Entry - since it
 * is both the "key" and the "value".
 * <ul>
 * <li>key: this
 * <li>value: associated TypeName
 * </ul>
 * This presents a bit of a conflict in that we are never quite sure
 * what comes first the record or the member during creation time.
 * 
 * @author Jody
 */
public class MemberNameImpl extends org.geotools.util.LocalName implements MemberName {
    private static final long serialVersionUID = 6188284973982058318L;
    private final TypeName typeName;

    public MemberNameImpl( String name, TypeName typeName ){
        super( name );
        this.typeName = typeName;        
    }
    public MemberNameImpl( CharSequence name, TypeName typeName ){
        super( name );
        this.typeName = typeName;        
    }
    public TypeName getAttributeType() {
        return typeName;
    }

}
