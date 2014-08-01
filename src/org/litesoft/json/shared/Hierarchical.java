package org.litesoft.json.shared;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.iterators.*;
import org.litesoft.commonfoundation.typeutils.*;
import org.litesoft.commonfoundation.typeutils.Objects;

import java.util.*;

public abstract class Hierarchical {
    protected static class NameValuePair implements Comparable<NameValuePair> {
        private final String mName;
        private final Object mValue;

        public NameValuePair( String pName, Object pValue ) {
            mName = pName;
            mValue = pValue;
        }

        public String getName() {
            return mName;
        }

        public Object getValue() {
            return mValue;
        }

        @Override
        public int hashCode() {
            return HashCode.from( mName ).and( mValue ).toHashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            return (this == obj) || ((obj instanceof NameValuePair) && equals( (NameValuePair) obj ));
        }

        public boolean equals( NameValuePair them ) {
            return (this == them) || ((them != null)
                                      && Objects.areNonArraysEqual( this.mName, them.mName )
                                      && Objects.areNonArraysEqual( this.mValue, them.mValue )
            );
        }

        @Override
        public int compareTo( NameValuePair them ) {
            int zResult = Compare.first( this.mName, them.mName ).result();
            if ( zResult != 0 ) {
                return zResult;
            }
            Object zThis = this.mValue;
            Object zThem = them.mValue;
            if ( !Objects.areNonArraysEqual( zThis, zThem ) ) {
                // Not Both Null
                if ( zThis == null ) {
                    return -1; // Nulls Up
                }
                if ( zThem == null ) {
                    return 1; // Nulls Up
                }
                // Neither Null
                Compare.verifySameClass( zThis, zThem );
                if ( zThis instanceof Comparable ) {
                    Comparable zComparableThis = Cast.it( zThis );
                    Comparable zComparableThem = Cast.it( zThem );
                    return Compare.first( zComparableThis, zComparableThem ).result();
                }
            }
            return 0;
        }
    }

    public final <T> T get( String... pPath ) {
        return get( new ArrayIterator<String>( pPath ) );
    }

    public final <T> T get( Iterator<String> pPath ) {
        Object zValue = getAttributeValue( pPath.next() );
        while ( (zValue != null) && pPath.hasNext() ) {
            if ( zValue instanceof Hierarchical ) {
                return ((Hierarchical) zValue).get( pPath );
            }
            String zNextReference = pPath.next();
            if ( !(zValue instanceof List) ) {
                throw new IllegalArgumentException(
                        "Can not descend into a '" + ClassName.simple( zValue ) + "' with attribute reference: " + zNextReference );
            }
            zValue = getListMember( (List) zValue, zNextReference );
        }
        return Cast.it( zValue );
    }

    private Object getListMember( List pList, String pReference ) {
        if ( (pReference.length() > 2) && pReference.startsWith( "[" ) && pReference.endsWith( "]" ) ) {
            int zIndex = -1;
            try {
                zIndex = Integer.parseInt( pReference.substring( 1, pReference.length() - 1 ) );
            }
            catch ( NumberFormatException e ) {
                // Whatever
            }
            if ( 0 <= zIndex ) {
                return (zIndex < pList.size()) ? pList.get( zIndex ) : null;
            }
        }
        throw new IllegalArgumentException( "Not a List Reference: " + pReference );
    }

    private Object getAttributeValue( String pName ) {
        for ( NameValuePair zAttribute : getAttributes() ) {
            if ( zAttribute.getName().equalsIgnoreCase( pName ) ) {
                return zAttribute.getValue();
            }
        }
        List<String> zNames = Lists.newArrayList();
        for ( NameValuePair zAttribute : getAttributes() ) {
            zNames.add( zAttribute.getName() );
        }
        throw new IllegalArgumentException( "Name '" + pName + "' not found, must be one of: " + zNames );
    }

    abstract protected NameValuePair[] getAttributes();

    @Override
    public final String toString() {
        return appendTo( new StringBuilder(), 0 ).toString();
    }

    private StringBuilder appendTo( StringBuilder pSB, int pDepth ) {
        StringBuilder sb = new StringBuilder();
        int zAdded = appendAttributes( pDepth + 1, sb, 0 );
        if ( zAdded > 0 ) {
            pSB.append( '{' );
            if ( zAdded == 1 ) {
                pSB.append( sb );
            } else {
                appendNewLine( pSB, pDepth + 1 );
                pSB.append( sb );
                appendNewLine( pSB, pDepth );
            }
            pSB.append( '}' );
        }
        return pSB;
    }

    protected int appendAttributes( int pDepth, StringBuilder pSB, int pAdded ) {
        for ( NameValuePair zAttribute : getAttributes() ) {
            if ( appendTo( pSB, pDepth, zAttribute.getName(), zAttribute.getValue() ) ) {
                pAdded++;
            }
        }
        return pAdded;
    }

    protected void appendNewLine( StringBuilder pSB, int pDepth ) {
        pSB.append( '\n' );
        while ( pDepth-- > 0 ) {
            pSB.append( "  " );
        }
    }

    protected boolean appendTo( StringBuilder pSB, int pDepth, String pName, Object pValue ) {
        if ( pValue == null ) {
            return false;
        }
        if ( pValue instanceof List ) {
            return appendList( pSB, pDepth, pName, (List) pValue );
        }
        if ( pValue instanceof String ) {
            return appendString( pSB, pDepth, pName, (String) pValue );
        }
        if ( pValue instanceof Number ) {
            return appendNumber( pSB, pDepth, pName, (Number) pValue );
        }
        if ( pValue instanceof Boolean ) {
            return appendBoolean( pSB, pDepth, pName, (Boolean) pValue );
        }
        if ( pValue instanceof Hierarchical ) {
            return appendJsonParent( pSB, pDepth, pName, (Hierarchical) pValue );
        }
        return appendCommon( pSB, pDepth, pName, pValue.toString() );
    }

    private boolean appendCommon( StringBuilder pSB, int pDepth, String pName, String pValue ) {
        pValue = ConstrainTo.significantOrNull( pValue );
        if ( (pValue == null) || "{}".equals( pValue ) ) {
            return false;
        }
        if ( pSB.length() != 0 ) {
            appendNewLine( pSB.append( ',' ), pDepth );
        }
        pSB.append( "'" ).append( pName ).append( "': " ).append( pValue );
        return true;
    }

    private boolean appendString( StringBuilder pSB, int pDepth, String pName, String pValue ) {
        return appendCommon( pSB, pDepth, pName, JsonUtils.safeStringValue( pValue ) );
    }

    private boolean appendNumber( StringBuilder pSB, int pDepth, String pName, Number pValue ) {
        return appendCommon( pSB, pDepth, pName, "" + pValue );
    }

    private boolean appendBoolean( StringBuilder pSB, int pDepth, String pName, Boolean pValue ) {
        return appendCommon( pSB, pDepth, pName, "" + pValue );
    }

    private boolean appendJsonParent( StringBuilder pSB, int pDepth, String pName, Hierarchical pValue ) {
        return appendCommon( pSB, pDepth, pName, pValue.appendTo( new StringBuilder(), pDepth ).toString() );
    }

    private boolean appendList( StringBuilder pSB, int pDepth, String pName, List pValue ) {
        StringBuilder sb = new StringBuilder();
        int zAdded = 0;
        for ( Object zValue : pValue ) {
            if ( appendTo( sb, pDepth + 1, zValue ) ) {
                zAdded++;
            }
        }
        if ( zAdded == 0 ) {
            return false;
        }
        appendCommon( pSB, pDepth, pName, "[" );
        if ( zAdded == 1 ) {
            pSB.append( sb );
        } else {
            appendNewLine( pSB, pDepth + 1 );
            pSB.append( sb );
            appendNewLine( pSB, pDepth );
        }
        pSB.append( ']' );
        return true;
    }

    private boolean appendTo( StringBuilder pSB, int pDepth, Object pValue ) {
        if ( pValue == null ) {
            return false;
        }
        if ( pValue instanceof String ) {
            return appendString( pSB, pDepth, (String) pValue );
        }
        if ( pValue instanceof Number ) {
            return appendNumber( pSB, pDepth, (Number) pValue );
        }
        if ( pValue instanceof Boolean ) {
            return appendBoolean( pSB, pDepth, (Boolean) pValue );
        }
        if ( pValue instanceof Hierarchical ) {
            return appendHierarchical( pSB, pDepth, (Hierarchical) pValue );
        }
        return appendCommon( pSB, pDepth, pValue.toString() );
    }

    private boolean appendCommon( StringBuilder pSB, int pDepth, String pValue ) {
        pValue = ConstrainTo.significantOrNull( pValue );
        if ( (pValue == null) || "{}".equals( pValue ) ) {
            return false;
        }
        if ( pSB.length() != 0 ) {
            appendNewLine( pSB.append( ',' ), pDepth );
        }
        pSB.append( pValue );
        return true;
    }

    private boolean appendString( StringBuilder pSB, int pDepth, String pValue ) {
        return appendCommon( pSB, pDepth, JsonUtils.safeStringValue( pValue ) );
    }

    private boolean appendNumber( StringBuilder pSB, int pDepth, Number pValue ) {
        return appendCommon( pSB, pDepth, "" + pValue );
    }

    private boolean appendBoolean( StringBuilder pSB, int pDepth, Boolean pValue ) {
        return appendCommon( pSB, pDepth, "" + pValue );
    }

    private boolean appendHierarchical( StringBuilder pSB, int pDepth, Hierarchical pValue ) {
        return appendCommon( pSB, pDepth, pValue.appendTo( new StringBuilder(), pDepth ).toString() );
    }
}
