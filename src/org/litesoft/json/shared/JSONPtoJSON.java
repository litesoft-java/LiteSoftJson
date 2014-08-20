package org.litesoft.json.shared;

import java8.util.function.*;

public class JSONPtoJSON implements UnaryOperator<String> {
    private static final String JSONP_FRONT = "callback(";
    private static final String JSONP_END = ");";
    private static final String JSONP_END2 = ")";

    @Override
    public String apply(String pPossibleJSONP) {
        return removeCommentsAndJSONP( pPossibleJSONP );
    }

    protected String removeCommentsAndJSONP( String pJSON ) {
        pJSON = pJSON.trim();
        while ( !pJSON.startsWith( "{" ) || !pJSON.endsWith( "}" ) ) {
            int zJsonStartsAt = pJSON.indexOf( "{" );
            int zJsonEndsAt = pJSON.lastIndexOf( "}" );
            if ( null == (pJSON = cleanJSON( pJSON, zJsonStartsAt, zJsonEndsAt )) ) {
                return null;
            }
        }
        return pJSON;
    }

    protected String cleanJSON( String pJSON, int pStartsAt, int pEndsAt ) {
        if ( (pEndsAt < pStartsAt) || (pStartsAt == -1) ) {
            return null;
        }
        int zAt = pJSON.substring( 0, pStartsAt ).indexOf( JSONP_FRONT );
        if ( zAt == -1 ) {
            return null;
        }
        if ( !isEmptyOrComments( pJSON.substring( 0, zAt ).trim() ) ) {
            return null;
        }
        if ( pJSON.endsWith( JSONP_END ) ) {
            return pJSON.substring( pStartsAt, pJSON.length() - JSONP_END.length() ).trim();
        }
        if ( pJSON.endsWith( JSONP_END2 ) ) {
            return pJSON.substring( pStartsAt, pJSON.length() - JSONP_END2.length() ).trim();
        }
        return null;
    }

    protected boolean isEmptyOrComments( String pPreJSONP ) {
        return (pPreJSONP.length() == 0) || (pPreJSONP.startsWith( "/*" ) && pPreJSONP.endsWith( "*/" ));
    }
}
