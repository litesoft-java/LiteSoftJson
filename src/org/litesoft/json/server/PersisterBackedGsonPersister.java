package org.litesoft.json.server;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;
import org.litesoft.server.util.*;

public class PersisterBackedGsonPersister<T> extends GsonPersister<T> {
    private Persister mPersister;
    private final String mRelativeFilePath;

    public PersisterBackedGsonPersister( Class<T> pType, Persister pPersister, String pRelativePathFromPersister, String pFileNameWithoutJSON ) {
        super( pType, pFileNameWithoutJSON );
        mPersister = Confirm.isNotNull( "Persister", pPersister );
        mRelativeFilePath = Paths.forwardSlashCombine( pRelativePathFromPersister, mFileName );
    }

    @Override
    protected boolean fileExists() {
        return mPersister.fileExists( mRelativeFilePath );
    }

    @Override
    protected String loadJson() {
        String[] zLines = mPersister.getTextFile( mRelativeFilePath );
        return Strings.combineAsLines( zLines );
    }

    @Override
    protected void saveJson( String pJSON ) {
        String[] zLines = Strings.toLines( pJSON );
        mPersister.putTextFile( mRelativeFilePath, zLines );
    }
}
