package org.litesoft.json.shared;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.problems.*;
import org.litesoft.commonfoundation.typeutils.*;

public class JsonHelper {
    public static IllegalStateException notAsNamedAttributes( ToJson pCaller ) {
        return new IllegalStateException( ClassName.simple( pCaller ) + " does NOT support being JsonAsNamedAttributes!" );
    }

    public static IllegalStateException notAsListEntryValue( ToJson pCaller ) {
        return new IllegalStateException( ClassName.simple( pCaller ) + " does NOT support being JsonAsListEntryValue!" );
    }

    public static void toJson( JsonBuilder pBuilder, ToJson pField ) {
        if ( pField != null ) {
            pField.toJsonAsNamedAttributes( pBuilder );
        }
    }

    public static void fromJson( JsonObject pObject, FromJson pField ) {
        if ( pField != null ) {
            pField.fromJsonAsNamedAttributes( pObject );
        }
    }

    public static void toJson( JsonBuilder pBuilder, ToJson... pFields ) {
        if ( pFields != null ) {
            for ( ToJson zField : pFields ) {
                toJson( pBuilder, zField );
            }
        }
    }

    public static boolean fromJson( JsonObject pObject, FromJson... pFields ) {
        if ( pObject == null ) {
            return false;
        }
        if ( pFields != null ) {
            for ( FromJson zField : pFields ) {
                fromJson( pObject, zField );
            }
        }
        return true;
    }

    public static void fromJsonComplete( Double pVersion, FromJson pField ) {
        if ( pField != null ) {
            pField.fromJsonComplete( pVersion );
        }
    }

    public static void fromJsonComplete( Double pVersion, FromJson... pFields ) {
        if ( pFields != null ) {
            for ( FromJson zField : pFields ) {
                fromJsonComplete( pVersion, zField );
            }
        }
    }

    public static void addIssue( JsonObject pObject, Enum<?> pCodeSupplier, Object... pIndexParameters ) {
        pObject.addProblem( new Problem( pCodeSupplier, Strings.toArray( pIndexParameters ) ) );
    }
}
