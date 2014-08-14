package org.litesoft.json.shared;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.problems.*;

import java.util.*;

public abstract class JsonParserRootObjectFactory {
    private static JsonParserRootObjectFactory sFactory;

    public static synchronized JsonParserRootObjectFactory getInstance() {
        if ( sFactory == null ) {
            throw new IllegalStateException( "JSON Factory NOT Initialized!" );
        }
        return sFactory;
    }

    private final SupportingVersion mVersions;

    protected JsonParserRootObjectFactory( SupportingVersion pVersions ) {
        sFactory = this;
        mVersions = Confirm.isNotNull( "SupportingVersion", pVersions );
    }

    public Double getCurrentVersion() {
        return mVersions.getCurrentVersion();
    }

    abstract public RootJsonObject createRootObject( String pJson, String pJsonVersionAttributeName, ProblemCollector pProblemCollector )
            throws JsonVersionException;

    public boolean populate( JsonRootable pRootable, String pJson, String pJsonVersionAttributeName, ProblemCollector pProblemCollector )
            throws JsonVersionException {
        JsonObject zJsonObject = createRootObject( pJson, pJsonVersionAttributeName, pProblemCollector );
        if ( zJsonObject == null ) {
            return false;
        }
        pRootable.fromJsonAsNamedAttributes( zJsonObject );
        Double zVersion = zJsonObject.getVersion();
        FromJsonPreCompletableManager.preComplete( zVersion );
        pRootable.fromJsonComplete( zVersion );
        return true;
    }

    protected double verifyVersion( Double pVersion ) {
        if ( null == (pVersion = ConstrainTo.firstNonNull( pVersion, mVersions.getDefaultVersion() )) ) {
            throw new JsonVersionNotNumericException();
        }
        for ( double zSupportedVersion : mVersions.getSupportedVersions() ) {
            if ( zSupportedVersion == pVersion ) {
                return zSupportedVersion;
            }
        }
        throw new JsonVersionUnsupportedException( pVersion );
    }

    protected static abstract class ArrayElementHandler<T, JsonBase> {
        private String mWhat;

        protected ArrayElementHandler( String pWhat ) {
            mWhat = pWhat;
        }

        protected <JsonType> JsonType assertNotNull( JsonType pToCheck, int pIndex, JsonBase pNonNullArrayEntry ) {
            if ( pToCheck == null ) {
                throw new IllegalArgumentException( "JSON Array Element[" + pIndex + "] not a " + mWhat + ", but a: " + pNonNullArrayEntry.getClass().getName() );
            }
            return pToCheck;
        }

        abstract public void add( List<T> pCollector, ProblemCollector pProblemCollector, int pIndex, JsonBase pNonNullArrayEntry, Double pVersion );
    }
}
