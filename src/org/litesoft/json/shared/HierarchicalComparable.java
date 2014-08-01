package org.litesoft.json.shared;

import org.litesoft.commonfoundation.base.*;

public abstract class HierarchicalComparable<T extends HierarchicalComparable> extends Hierarchical implements Comparable<T> {
    @Override
    public boolean equals( Object obj ) {
        return (this == obj) || ((obj instanceof HierarchicalComparable) && equals( (HierarchicalComparable) obj ));
    }

    @Override
    public int hashCode() {
        NameValuePair[] zAttributes = getAttributes();
        HashCode.Builder zBuilder = HashCode.from( zAttributes[0] );
        for ( int i = 1; i < zAttributes.length; i++ ) {
            zBuilder = zBuilder.and( zAttributes[i] );
        }
        return zBuilder.toHashCode();
    }

    @Override
    public int compareTo( T them ) {
        Compare.verifySameClass( this, them );
        NameValuePair[] zThisAttributes = this.getAttributes();
        NameValuePair[] zThemAttributes = them.getAttributes();
        Compare zCompare = Compare.first( zThisAttributes[0], zThemAttributes[0] );
        for ( int i = 1; i < zThisAttributes.length; i++ ) {
            zCompare = zCompare.then( zThisAttributes[i], zThemAttributes[i] );
        }
        return zCompare.result();
    }

    public boolean equals( HierarchicalComparable them ) {
        if ( this == them ) {
            return true;
        }
        if ( them != null ) {
            NameValuePair[] zThisAttributes = this.getAttributes();
            NameValuePair[] zThemAttributes = them.getAttributes();
            if ( zThisAttributes.length == zThemAttributes.length ) {
                for ( int i = 0; i < zThisAttributes.length; i++ ) {
                    if ( !zThisAttributes[i].equals( zThemAttributes[i] ) ) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
