package org.litesoft.json.shared;

import org.litesoft.commonfoundation.problems.*;
import org.litesoft.commonfoundation.typeutils.*;

import java.util.*;

public abstract class AbstractJsonObject implements JsonObject {

    protected final Double mVersion;
    protected final ProblemCollector mProblemCollector;

    public AbstractJsonObject( Double pVersion, ProblemCollector pProblemCollector ) {
        mVersion = pVersion;
        mProblemCollector = pProblemCollector;
    }

    @Override
    public final Double getVersion() {
        return mVersion;
    }

    @Override
    public final List<JsonObject> getObjectArray( String name ) {
        ArrayList<JsonObject> zObjects = Lists.newArrayList();
        populateObjectArray( zObjects, name );
        return zObjects;
    }

    @Override
    public final List<String> getStringArray( String name ) {
        ArrayList<String> zStrings = Lists.newArrayList();
        populateStringArray( zStrings, name );
        return zStrings;
    }

    protected abstract void populateObjectArray( List<JsonObject> pObjects, String pName );

    protected abstract void populateStringArray( List<String> pStrings, String pName );

    @Override
    public void addProblem( Problem pProblem ) {
        mProblemCollector.add( pProblem );
    }
}
