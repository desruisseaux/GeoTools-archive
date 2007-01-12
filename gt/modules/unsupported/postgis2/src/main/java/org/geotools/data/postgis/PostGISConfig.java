package org.geotools.data.postgis;

import java.util.Map;

/**
 * This is a Java Bean used to collect connetion parameters.
 * <p>
 * A BeanInfo is available if you want to get nice
 * descriptions etc...
 * </p>
 * @author Jody Garnett, Refractions Research Inc.
 */
public class PostGISConfig {
    private String host;
    private int port;
    private String schema;
    private String database;
    private String user;
    private String password;
    private String namespace;

    PostGISConfig( Map params ){
        setHost( (String) params.get("host") );
        setPort( Integer.parseInt( (String) params.get("port") ));
        setSchema( (String) params.get("schema") );
        setDatabase( (String) params.get("database"));
        setUser( (String) params.get("user"));
        setPassword( (String) params.get("password"));
    }
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    public String getNamespace() {
        return namespace;
    }
}
