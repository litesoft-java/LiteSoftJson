package org.litesoft.json.shared;

import java.util.*;

public abstract class JsonParserRootObjectFactory {
    private static JsonParserRootObjectFactory sFactory;

    public static synchronized JsonParserRootObjectFactory getInstance() {
        if ( sFactory == null ) {
            throw new IllegalStateException( "JSON Factory NOT Initialized!" );
        }
        return sFactory;
    }

    private final double mCurrentVersion;
    private final double[] mSupportedVersions;

    protected JsonParserRootObjectFactory( double pCurrentVersion, Double pNextVersion, double... pOlderVersions ) {
        sFactory = this;
        mCurrentVersion = pCurrentVersion;
        int zOlderStartsAt;
        if ( pNextVersion == null ) {
            mSupportedVersions = new double[(zOlderStartsAt = 1) + pOlderVersions.length];
            mSupportedVersions[0] = pCurrentVersion;
        } else {
            mSupportedVersions = new double[(zOlderStartsAt = 2) + pOlderVersions.length];
            mSupportedVersions[0] = pCurrentVersion;
            mSupportedVersions[1] = pNextVersion;
        }
        if ( pOlderVersions.length != 0 ) {
            System.arraycopy( pOlderVersions, 0, mSupportedVersions, zOlderStartsAt, pOlderVersions.length );
        }
    }

    public Double getCurrentVersion() {
        return mCurrentVersion;
    }

    abstract public RootJsonObject createRootObject( String pJson, String pJsonVersionAttributeName, Set<String> pIssueCollector )
            throws JsonVersionException;

    public boolean populate( JsonRootable pRootable, String pJson, String pJsonVersionAttributeName, Set<String> pIssueCollector )
            throws JsonVersionException {
        JsonObject zJsonObject = createRootObject( pJson, pJsonVersionAttributeName, pIssueCollector );
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
        if ( pVersion == null ) {
            throw new JsonVersionNotNumericException();
        }
        for ( double zSupportedVersion : mSupportedVersions ) {
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

        abstract public void add( List<T> pCollector, Set<String> pIssueCollector, int pIndex, JsonBase pNonNullArrayEntry, Double pVersion );
    }
}
