package org.litesoft.json.shared;

import java.sql.*;
import java.util.*;

public interface JsonObject {
    public Double getVersion();

    public void addIssue( String pIssue );

    public Set<String> keySet();

    public JsonObject getObject( String name );

    public List<JsonObject> getObjectArray( String name );

    public List<String> getStringArray( String name );

    public String getString( String name );

    public Boolean getBoolean( String name );

    public Timestamp getTimestamp( String name );

    public Double getDouble( String name );
}
