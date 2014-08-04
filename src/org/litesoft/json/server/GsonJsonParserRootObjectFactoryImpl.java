package org.litesoft.json.server;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.exceptions.*;
import org.litesoft.commonfoundation.problems.*;
import org.litesoft.commonfoundation.typeutils.*;
import org.litesoft.commonfoundation.typeutils.gregorian.*;
import org.litesoft.json.shared.*;
import org.litesoft.json.shared.JsonObject;

import com.google.gson.*;

import java.sql.*;
import java.util.*;

public class GsonJsonParserRootObjectFactoryImpl extends JsonParserRootObjectFactory {
    public GsonJsonParserRootObjectFactoryImpl( double pCurrentVersion, Double pNextVersion, double... pOlderVersions ) {
        super( pCurrentVersion, pNextVersion, pOlderVersions );
    }

    @Override
    public RootJsonObject createRootObject( String pJson, String pJsonVersionAttributeName, ProblemCollector pProblemCollector )
            throws JsonVersionException {
        com.google.gson.JsonObject zJsonObject = asObject( new JsonParser().parse( pJson ) );
        if ( zJsonObject != null ) {
            return new GSONRootJsonObject( getVersion( zJsonObject, ConstrainTo.significantOrNull( pJsonVersionAttributeName ) ), pProblemCollector,
                                           zJsonObject );
        }
        return null;
    }

    private Double getVersion( com.google.gson.JsonObject pJsonObject, String pJsonVersionAttributeName ) {
        return (pJsonVersionAttributeName == null) ? null
                                                   : verifyVersion( asDouble( pJsonObject.get( pJsonVersionAttributeName ) ) );
    }

    private static Double asDouble( JsonElement pJsonValue ) {
        if ( (pJsonValue != null) && pJsonValue.isJsonPrimitive() ) {
            JsonPrimitive zPrimitive = (JsonPrimitive) pJsonValue;
            if ( zPrimitive.isNumber() ) {
                return zPrimitive.getAsDouble();
            }
        }
        return null;
    }

    private static String asString( JsonElement pJsonValue ) {
        if ( (pJsonValue != null) && pJsonValue.isJsonPrimitive() ) {
            JsonPrimitive zPrimitive = (JsonPrimitive) pJsonValue;
            if ( zPrimitive.isString() ) {
                return zPrimitive.getAsString();
            }
        }
        return null;
    }

    private static Timestamp asTimestamp( JsonElement pJsonValue ) {
        if ( (pJsonValue != null) && pJsonValue.isJsonPrimitive() ) {
            JsonPrimitive zPrimitive = (JsonPrimitive) pJsonValue;
            if ( zPrimitive.isString() ) {
                try {
                    return Timestamps.fromISO8601ZuluSimple( zPrimitive.getAsString() );
                }
                catch ( TimestampFormatException e ) {
                    // Fall Thru...
                }
            }
        }
        return null;
    }

    private static Boolean asBoolean( JsonElement pJsonValue ) {
        if ( (pJsonValue != null) && pJsonValue.isJsonPrimitive() ) {
            JsonPrimitive zPrimitive = (JsonPrimitive) pJsonValue;
            if ( zPrimitive.isBoolean() ) {
                return zPrimitive.getAsBoolean();
            }
        }
        return null;
    }

    private static com.google.gson.JsonObject asObject( JsonElement pJsonValue ) {
        return ((pJsonValue != null) && pJsonValue.isJsonObject()) ? (com.google.gson.JsonObject) pJsonValue : null;
    }

    private static final ArrayElementHandler<JsonObject, JsonElement> OBJECT_HANDLER = new ArrayElementHandler<JsonObject, JsonElement>( "object" ) {
        @Override
        public void add( List<JsonObject> pCollector, ProblemCollector pProblemCollector, int pIndex, JsonElement pNonNullArrayEntry, Double pVersion ) {
            pCollector.add( new GSONJsonObject( pVersion, pProblemCollector, assertNotNull( asObject( pNonNullArrayEntry ), pIndex, pNonNullArrayEntry ) ) );
        }
    };

    private static final ArrayElementHandler<String, JsonElement> STRING_HANDLER = new ArrayElementHandler<String, JsonElement>( "String" ) {
        @Override
        public void add( List<String> pCollector, ProblemCollector pProblemCollector, int pIndex, JsonElement pNonNullArrayEntry, Double pVersion ) {
            pCollector.add( assertNotNull( asString( pNonNullArrayEntry ), pIndex, pNonNullArrayEntry ) );
        }
    };

    private static class GSONJsonObject extends AbstractJsonObject {
        private final com.google.gson.JsonObject mJsonObject;

        public GSONJsonObject( Double pVersion, ProblemCollector pProblemCollector, com.google.gson.JsonObject pJsonObject ) {
            super( pVersion, pProblemCollector );
            mJsonObject = pJsonObject;
        }

        @Override
        public Set<String> keySet() {
            LinkedHashSet<String> zKeys = Sets.newLinkedHashSet();
            for ( Map.Entry<String, JsonElement> zEntry : mJsonObject.entrySet() ) {
                zKeys.add( zEntry.getKey() );
            }
            return zKeys;
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
            com.google.gson.JsonObject obj = asObject( mJsonObject.get( name ) );
            return (obj != null) ? new GSONJsonObject( mVersion, mProblemCollector, obj ) : null;
        }

        @Override
        protected void populateObjectArray( List<JsonObject> pObjects, String pName ) {
            populateArray( pObjects, pName, OBJECT_HANDLER );
        }

        @Override
        protected void populateStringArray( List<String> pStrings, String pName ) {
            populateArray( pStrings, pName, STRING_HANDLER );
        }

        private <T> void populateArray( List<T> pCollector, String pName, ArrayElementHandler<T, JsonElement> pHandler ) {
            JsonElement jsonValue = mJsonObject.get( pName );
            if ( jsonValue != null ) {
                if ( jsonValue.isJsonArray() ) {
                    JsonArray jsonArray = (JsonArray) jsonValue;
                    for ( int i = 0; i < jsonArray.size(); i++ ) {
                        JsonElement arrayEntry = jsonArray.get( i );
                        if ( arrayEntry != null ) {
                            pHandler.add( pCollector, mProblemCollector, i, arrayEntry, mVersion );
                        }
                    }
                }
            }
        }
    }

    private static class GSONRootJsonObject extends GSONJsonObject implements RootJsonObject {
        public GSONRootJsonObject( Double pVersion, ProblemCollector pProblemCollector, com.google.gson.JsonObject pJsonObject ) {
            super( pVersion, pProblemCollector, pJsonObject );
        }
    }
}
