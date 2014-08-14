package org.litesoft.json.server;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.server.dynamicload.*;
import org.litesoft.server.util.*;

import java8.util.function.*;

public abstract class GsonPersister<T> implements Supplier<T>,
                                                  TypePersister<T> {
    private final Class<T> mType;
    protected final String mFileName;

    public GsonPersister( Class<T> pType, String pFileNameWithoutJSON ) {
        mType = Confirm.isNotNull( "Type", pType );
        mFileName = Confirm.significant( "FileName", pFileNameWithoutJSON ) + ".json";
    }

    @Override
    public T get() {
        if ( !fileExists() ) {
            return ClassForName.newInstance( mType, mType.getName() );
        }
        String zJSON = loadJson();
        return GsonRoot.fromJson( zJSON, mType );
    }

    @Override
    public void save( T pInstance ) {
        String zJSON = GsonRoot.toJson( Confirm.isNotNull( "Instance", pInstance ), mType );
        saveJson( zJSON );
    }

    protected abstract boolean fileExists();

    protected abstract String loadJson();

    protected abstract void saveJson( String pJSON );
}
