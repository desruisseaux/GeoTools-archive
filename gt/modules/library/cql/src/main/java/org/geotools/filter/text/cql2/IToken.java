package org.geotools.filter.text.cql2;

/**
 * Interface must be implemented by the specific compiler.
 * This will be used to send the token to the {@link FilterBuilder}.
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
public interface IToken {

    public String toString();

    public boolean hasNext();

    public IToken next();

    public int beginColumn();

    public int endColumn();

    public Object getAdapted();

}
