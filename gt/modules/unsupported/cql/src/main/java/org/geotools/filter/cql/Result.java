package org.geotools.filter.cql;

/**
 * 
 * @author Mauricio Pazos - Axios Engineering
 * @author Gabriel Roldan - Axios Engineering
 * @version $Id$
 * @source $URL$
 */
final class Result {

    private int nodeType = 0;

    private Object built;

    private Token token;

    Result(Object built, Token token, int nodeType) {

        this.built = built;
        this.token = token;
        this.nodeType = nodeType;
    }

    public final Object getBuilt() {
        return this.built;
    }

    public final int getNodeType() {
        return this.nodeType;
    }

    public final Token getToken() {
        return this.token;
    }
}
