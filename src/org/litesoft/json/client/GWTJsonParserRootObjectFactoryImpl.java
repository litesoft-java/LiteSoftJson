package org.litesoft.json.client;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.exceptions.*;
import org.litesoft.commonfoundation.typeutils.gregorian.*;
import org.litesoft.json.shared.*;

import com.google.gwt.json.client.*;

import java.sql.*;
import java.util.*;

public class GWTJsonParserRootObjectFactoryImpl extends JsonParserRootObjectFactory {
    public GWTJsonParserRootObjectFactoryImpl( double pCurrentVersion, Double pNextVersion, double... pOlderVersions ) {
        super( pCurrentVersion, pNextVersion, pOlderVersions );
    }

    @Override
    public RootJsonObject createRootObject( String pJson, String pJsonVersionAttributeName, Set<String> pIssueCollector )
            throws JsonVersionException {
        JSONObject zJsonObject = asObject( JSONParser.parseLenient( pJson ) );
        if ( zJsonObject != null ) {
            return new GWTRootJsonObject( getVersion( zJsonObject, ConstrainTo.significantOrNull( pJsonVersionAttributeName ) ), pIssueCollector, zJsonObject );
        }
        return null;
    }

    private Double getVersion( JSONObject pJsonObject, String pJsonVersionAttributeName ) {
        return (pJsonVersionAttributeName == null) ? null
                                                   : verifyVersion( asDouble( pJsonObject.get( pJsonVersionAttributeName ) ) );
    }

    private static Double asDouble( JSONValue pJsonValue ) {
        if ( pJsonValue != null ) {
            JSONNumber zNumber = pJsonValue.isNumber();
            if ( zNumber != null ) {
                return zNumber.doubleValue();
            }
        }
        return null;
    }

    private static String asString( JSONValue pJsonValue ) {
        if ( pJsonValue != null ) {
            JSONString jsonString = pJsonValue.isString();
            if ( jsonString != null ) {
                return jsonString.stringValue();
            }
        }
        return null;
    }

    private static Timestamp asTimestamp( JSONValue pJsonValue ) {
        if ( pJsonValue != null ) {
            JSONString jsonString = pJsonValue.isString();
            if ( jsonString != null ) {
                try {
                    return Timestamps.fromISO8601ZuluSimple( jsonString.stringValue() );
                }
                catch ( TimestampFormatException e ) {
                    // Fall Thru...
                }
            }
        }
        return null;
    }

    private static Boolean asBoolean( JSONValue pJsonValue ) {
        if ( pJsonValue != null ) {
            JSONBoolean jsonBoolean = pJsonValue.isBoolean();
            if ( jsonBoolean != null ) {
                return jsonBoolean.booleanValue();
            }
        }
        return null;
    }

    private static JSONObject asObject( JSONValue pJsonValue ) {
        return (pJsonValue != null) ? pJsonValue.isObject() : null;
    }

    private static final ArrayElementHandler<JsonObject, JSONValue> OBJECT_HANDLER = new ArrayElementHandler<JsonObject, JSONValue>( "object" ) {
        @Override
        public void add( List<JsonObject> pCollector, Set<String> pIssueCollector, int pIndex, JSONValue pNonNullArrayEntry, Double pVersion ) {
            pCollector.add( new GWTJsonObject( pVersion, pIssueCollector, assertNotNull( asObject( pNonNullArrayEntry ), pIndex, pNonNullArrayEntry ) ) );
        }
    };

    private static final ArrayElementHandler<String, JSONValue> STRING_HANDLER = new ArrayElementHandler<String, JSONValue>( "String" ) {
        @Override
        public void add( List<String> pCollector, Set<String> pIssueCollector, int pIndex, JSONValue pNonNullArrayEntry, Double pVersion ) {
            pCollector.add( assertNotNull( asString( pNonNullArrayEntry ), pIndex, pNonNullArrayEntry ) );
        }
    };

    private static class GWTJsonObject extends AbstractJsonObject {
        private final JSONObject mJsonObject;

        public GWTJsonObject( Double pVersion, Set<String> pIssueCollector, JSONObject pJsonObject ) {
            super( pVersion, pIssueCollector );
            mJsonObject = pJsonObject;
        }

        public Set<String> keySet() {
            return mJsonObject.keySet();
        }

        @Override
        public String getString( String name ) {
            return asString( mJsonObject.get( name ) );
        }

        @Override
        public Boolean getBoolean( String name ) {
            return asBoolean( mJsonObject.get( name ) );
        }

        @Override
        public Timestamp getTimestamp( String name ) {
            return asTimestamp( mJsonObject.get( name ) );
        }

        @Override
        public Double getDouble( String name ) {
            return asDouble( mJsonObject.get( name ) );
        }

        @Override
        public JsonObject getObject( String name ) {
            JSONObject obj = asObject( mJsonObject.get( name ) );
            return (obj != null) ? new GWTJsonObject( mVersion, mIssueCollector, obj ) : null;
        }

        @Override
        protected void populateObjectArray( List<JsonObject> pObjects, String pName ) {
            populateArray( pObjects, pName, OBJECT_HANDLER );
        }

        @Override
        protected void populateStringArray( List<String> pStrings, String pName ) {
            populateArray( pStrings, pName, STRING_HANDLER );
        }

        private <T> void populateArray( List<T> pCollector, String pName, ArrayElementHandler<T, JSONValue> pHandler ) {
            JSONValue jsonValue = mJsonObject.get( pName );
            if ( jsonValue != null ) {
                JSONArray jsonArray = jsonValue.isArray();
                if ( jsonArray != null ) {
                    for ( int i = 0; i < jsonArray.size(); i++ ) {
                        JSONValue arrayEntry = jsonArray.get( i );
                        if ( arrayEntry != null ) {
                            pHandler.add( pCollector, mIssueCollector, i, arrayEntry, mVersion );
                        }
                    }
                }
            }
        }
    }

    private static class GWTRootJsonObject extends GWTJsonObject implements RootJsonObject {
        public GWTRootJsonObject( Double pVersion, Set<String> pIssueCollector, JSONObject pJsonObject ) {
            super( pVersion, pIssueCollector, pJsonObject );
        }
    }
}