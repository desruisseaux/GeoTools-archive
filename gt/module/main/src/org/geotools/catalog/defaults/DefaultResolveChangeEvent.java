package org.geotools.catalog.defaults;

import org.geotools.catalog.Resolve;
import org.geotools.catalog.ResolveChangeEvent;
import org.geotools.catalog.ResolveDelta;


/**
 * Everything change change change ...
 * 
 * @author jgarnett
 * @since 0.6.0
 */
public class DefaultResolveChangeEvent implements ResolveChangeEvent {
    private Object source;
    private Type type;
    private ResolveDelta delta; // may be null for some events
    private Resolve handle; // may be null for some events

    /**
     * Construct <code>CatalogChangeEvent</code>.
     * 
     * @param source Source of event, in case you care
     * @param type Type constant from ICatalogChangeEvent
     * @param delta Describes the change
     */
    public DefaultResolveChangeEvent( Object source, Type type, ResolveDelta delta ) {
        this.source = source;
        this.type = type;
        this.delta = delta;
        if (source instanceof Resolve) {
            handle = (Resolve) source;
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ResolveChangeEvent ("); //$NON-NLS-1$
        buffer.append(type);

        if (delta != null) {
            buffer.append(","); //$NON-NLS-1$
            buffer.append(delta);

        }
        if (handle != null) {
            buffer.append(","); //$NON-NLS-1$
            buffer.append(handle.getIdentifier());
        }

        return buffer.toString();
    }

    /**
     * @see net.refractions.udig.catalog.ICatalogChangeEvent#getDelta()
     */
    public ResolveDelta getDelta() {
        return delta;
    }

    /**
     * @see net.refractions.udig.catalog.ICatalogChangeEvent#getResource()
     */
    public Resolve getResolve() {
        return handle;
    }

    /**
     * @see net.refractions.udig.catalog.ICatalogChangeEvent#getSource()
     */
    public Object getSource() {
        return source;
    }

    /**
     * @see net.refractions.udig.catalog.ICatalogChangeEvent#getType()
     */
    public Type getType() {
        return type;
    }
}