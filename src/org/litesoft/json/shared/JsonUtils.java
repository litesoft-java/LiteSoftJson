package org.litesoft.json.shared;

import org.litesoft.commonfoundation.base.*;

public class JsonUtils {
    /**
     * Wrap (and escape) a String as appropriate.
     *
     * @param pValue !null
     */
    public static String safeStringValue( String pValue ) {
        if ( null == (pValue = ConstrainTo.significantOrNull( pValue )) ) {
            return null;
        }
        StringBuilder sb = new StringBuilder().append( '"' );
        for ( int i = 0; i < pValue.length(); i++ ) {
            char c = pValue.charAt( i );
            if ( (c == '\\') || (c == '"') ) {
                sb.append( '\\' );
            }
            if ( (' ' <= c) && (c < 127) ) { // printable Ascii
                sb.append( c );
            } else if ( c == '\n' ) {
                sb.append( "\\n" );
            } else {
                sb.append( "\\u" );
                char[] chars = Hex.to4Chars( c );
                for ( char aChar : chars ) {
                    sb.append( aChar );
                }
            }
        }
        return sb.append( '"' ).toString();
    }
}
