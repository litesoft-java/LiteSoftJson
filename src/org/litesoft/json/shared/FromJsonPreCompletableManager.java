package org.litesoft.json.shared;

import org.litesoft.commonfoundation.typeutils.*;

import java.util.*;

public class FromJsonPreCompletableManager {
    private static final List<FromJsonPreCompletable> PRE_COMPLETABLES = Lists.newArrayList();

    public static void addFromJSONpreCompletable( FromJsonPreCompletable pCompletable ) {
        if ( pCompletable != null ) {
            PRE_COMPLETABLES.add( pCompletable );
        }
    }

    public static void preComplete( Double pVersion ) {
        int zFrom = 0;
        for ( FromJsonPreCompletable[] zCompletables; zFrom < (zCompletables = getCompletables()).length; ) {
            while ( zFrom < zCompletables.length ) {
                zCompletables[zFrom++].fromJsonPreComplete( pVersion );
            }
        }
    }

    private static FromJsonPreCompletable[] getCompletables() {
        return PRE_COMPLETABLES.toArray( new FromJsonPreCompletable[PRE_COMPLETABLES.size()] );
    }
}
