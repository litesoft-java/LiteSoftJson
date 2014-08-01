package org.litesoft.json.shared;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.gregorian.*;

import java.sql.*;
import java.util.*;

public class JsonBuilder {
    public static final String SINGLE_QUOTE = "'";
    public static final String ATTRIBUTE_VALUE_SEP = ":";

    private final JsonBehaviorHelper mBehaviorHelper;
    private final Stack<State> mStack = new Stack<State>();

    public JsonBuilder( JsonBehaviorHelper pBehaviorHelper ) {
        mBehaviorHelper = pBehaviorHelper;
        mStack.push( new BaseState() );
    }

    public JsonBuilder( JsonBehaviorHelper pBehaviorHelper, String pJsonVersionAttributeName ) {
        this( pBehaviorHelper );
        startObject();
        addAttribute( pJsonVersionAttributeName, JsonParserRootObjectFactory.getInstance().getCurrentVersion() );
    }

    public String toJSON( JsonRootable pToJson ) {
        pToJson.toJsonAsNamedAttributes( this );
        return toJSON();
    }

    public String toJSON() {
        State zTop;
        while ( !((zTop = getTop()) instanceof BaseState) ) {
            zTop.end();
        }
        return ((BaseState) zTop).toJSON();
    }

    public JsonBehaviorHelper getBehaviorHelper() {
        return mBehaviorHelper;
    }

    public boolean shouldRecord( ToJson pToJson ) {
        return mBehaviorHelper.shouldRecord( pToJson );
    }

    public void startObject() {
        getTop().startObject();
    }

    public boolean isObjectEmpty() {
        return getTop().isObjectEmpty();
    }

    public void addAttribute( String name, String value ) {
        getTop().addAttribute( name, value );
    }

    public void addAttribute( String name, Enum<?> value ) {
        getTop().addAttribute( name, value );
    }

    public void addAttribute( String name, Number value ) {
        getTop().addAttribute( name, value );
    }

    public void addAttribute( String name, Boolean value ) {
        getTop().addAttribute( name, value );
    }

    public void addAttribute( String name, Timestamp value ) {
        getTop().addAttribute( name, value );
    }

    public void addAttributeObject( String name ) {
        getTop().addAttributeObject( name );
    }

    public void addAttributeObjectArray( String name ) {
        getTop().addAttributeObjectArray( name );
    }

    public void addAttributeObjectArray( String name, List<? extends HasJson> children ) {
        addAttributeObjectArray( name );
        if ( children != null ) {
            for ( HasJson child : children ) {
                if ( child != null ) {
                    child.toJsonAsListEntryValue( this );
                }
            }
        }
        endObjectArray();
    }

    public void addAttributeStringArray( String name, List<String> children ) {
        getTop().addAttributeStringArray( name );
        if ( children != null ) {
            for ( String child : children ) {
                if ( child != null ) {
                    getTop().addToStringArray( child );
                }
            }
        }
        getTop().endStringArray();
    }

    public void endObjectArray() {
        getTop().endObjectArray();
    }

    public void endObject() {
        getTop().endObject();
    }

    private State getTop() {
        return mStack.peek();
    }

    private abstract class State {
        public void startObject() {
            throw new IllegalStateException();
        }

        public boolean isObjectEmpty() {
            throw new IllegalStateException();
        }

        public void addAttributeObject( String pName ) {
            throw new IllegalStateException();
        }

        public void addAttributeObjectArray( String pName ) {
            throw new IllegalStateException();
        }

        public void addAttributeStringArray( String pName ) {
            throw new IllegalStateException();
        }

        public void addAttribute( String pName, String pValue ) {
            throw new IllegalStateException();
        }

        public void addAttribute( String pName, Number pValue ) {
            throw new IllegalStateException();
        }

        public void addAttribute( String pName, Boolean pValue ) {
            throw new IllegalStateException();
        }

        public void addAttribute( String pName, Enum<?> pValue ) {
            throw new IllegalStateException();
        }

        public void addAttribute( String pName, Timestamp pValue ) {
            throw new IllegalStateException();
        }

        public void addToStringArray( String pValue ) {
            throw new IllegalStateException();
        }

        public void endObjectArray() {
            throw new IllegalStateException();
        }

        public void endStringArray() {
            throw new IllegalStateException();
        }

        public void endObject() {
            throw new IllegalStateException();
        }

        public void end() {
            throw new IllegalStateException();
        }

        protected void setJSON( String pJSON ) {
            throw new IllegalStateException();
        }

        protected void assertFalse( boolean pPending ) {
            if ( pPending ) {
                throw new IllegalStateException();
            }
        }

        protected void assertTrue( boolean pFlag ) {
            if ( !pFlag ) {
                throw new IllegalStateException();
            }
        }

        protected boolean startObject( boolean pObjectStarted ) {
            if ( pObjectStarted ) {
                throw new IllegalStateException();
            }
            mStack.push( new ObjectState( this ) );
            return true;
        }

        protected boolean endObject( boolean pendingObject ) {
            if ( !pendingObject ) {
                throw new IllegalStateException();
            }
            return false;
        }

        protected void dropUs() {
            State zTop = mStack.pop();
            if ( zTop != this ) {
                throw new IllegalStateException();
            }
        }
    }

    private class BaseState extends State {
        private boolean mObjectStarted;
        private String mJSON;

        @Override
        public void startObject() {
            mObjectStarted = startObject( mObjectStarted );
        }

        @Override
        protected void setJSON( String pJSON ) {
            mJSON = pJSON;
        }

        public String toJSON() {
            if ( mJSON == null ) {
                throw new IllegalStateException();
            }
            return mJSON;
        }
    }

    private class ChildState extends State {
        private final State mParent;
        private final StringBuilder mJSON = new StringBuilder();

        protected ChildState( State pParent ) {
            mParent = pParent;
        }

        protected boolean anyJson() {
            return (mJSON.length() != 0);
        }

        protected void sharedEnd( char pJsonStart, char pJsonEnd ) {
            dropUs();
            if ( anyJson() ) {
                mJSON.insert( 0, pJsonStart ).append( pJsonEnd );
            }
            mParent.setJSON( mJSON.toString() );
        }

        protected StringBuilder sharedSetJson( String pJSON ) {
            if ( (pJSON != null) && (pJSON.length() != 0) ) {
                if ( anyJson() ) {
                    mJSON.append( ',' );
                }
                mJSON.append( pJSON );
            }
            return mJSON;
        }
    }

    private class ObjectArrayState extends ChildState {
        private boolean mPendingObject;

        private ObjectArrayState( State pParent ) {
            super( pParent );
        }

        @Override
        public void end() {
            endObjectArray();
        }

        @Override
        public void endObjectArray() {
            assertFalse( mPendingObject );
            sharedEnd( '[', ']' );
        }

        @Override
        protected void setJSON( String pJSON ) {
            mPendingObject = endObject( mPendingObject );
            sharedSetJson( pJSON );
        }

        @Override
        public void startObject() {
            mPendingObject = startObject( mPendingObject );
        }
    }

    private class StringArrayState extends ChildState {
        private StringArrayState( State pParent ) {
            super( pParent );
        }

        @Override
        public void end() {
            endStringArray();
        }

        @Override
        public void endStringArray() {
            sharedEnd( '[', ']' );
        }

        @Override
        public void addToStringArray( String pValue ) {
            sharedSetJson( JsonUtils.safeStringValue( pValue ) );
        }
    }

    private class ObjectState extends ChildState {
        private String mPendingChildStateAttributeName;

        private ObjectState( State pParent ) {
            super( pParent );
        }

        @Override
        public void end() {
            endObject();
        }

        @Override
        public void endObject() {
            assertNotCurrentlyInChildState();
            sharedEnd( '{', '}' );
        }

        @Override
        protected void setJSON( String pJSON ) {
            assertCurrentlyInChildState();
            if ( (pJSON != null) && (pJSON.length() != 0) ) {
                addCommonAttribute( mPendingChildStateAttributeName, pJSON );
            }
            mPendingChildStateAttributeName = null;
        }

        private void pushChild( String pName, ChildState pChild ) {
            Confirm.significant( "Name", mPendingChildStateAttributeName = initiateEntry( pName ) );
            mStack.push( pChild );
        }

        private void assertNotCurrentlyInChildState() {
            assertTrue( mPendingChildStateAttributeName == null );
        }

        private void assertCurrentlyInChildState() {
            assertTrue( mPendingChildStateAttributeName != null );
        }

        private String initiateEntry( String pName ) {
            assertNotCurrentlyInChildState();
            return validateName( pName );
        }

        private StringBuilder addNamedEntry( String pName ) {
            return sharedSetJson( SINGLE_QUOTE ).append( pName ).append( SINGLE_QUOTE ).append( ATTRIBUTE_VALUE_SEP );
        }

        private void addCommonAttribute( String pName, Object pValue ) {
            if ( (pName != null) && (pValue != null) ) {
                addNamedEntry( pName ).append( pValue );
            }
        }

        private void addCommonAttributeAsString( String pName, Object pValue ) {
            if ( (pName != null) && (pValue != null) ) {
                addNamedEntry( pName ).append( SINGLE_QUOTE ).append( pValue ).append( SINGLE_QUOTE );
            }
        }

        @Override
        public void addAttributeObject( String pName ) {
            pushChild( pName, new ObjectState( this ) );
        }

        @Override
        public void addAttributeObjectArray( String pName ) {
            pushChild( pName, new ObjectArrayState( this ) );
        }

        @Override
        public void addAttributeStringArray( String pName ) {
            pushChild( pName, new StringArrayState( this ) );
        }

        @Override
        public boolean isObjectEmpty() {
            return !anyJson();
        }

        @Override
        public void addAttribute( String pName, String pValue ) {
            addCommonAttribute( initiateEntry( pName ), JsonUtils.safeStringValue( pValue ) );
        }

        @Override
        public void addAttribute( String pName, Number pValue ) {
            addCommonAttribute( initiateEntry( pName ), pValue );
        }

        @Override
        public void addAttribute( String pName, Boolean pValue ) {
            addCommonAttribute( initiateEntry( pName ), pValue );
        }

        @Override
        public void addAttribute( String pName, Enum<?> pValue ) {
            addCommonAttributeAsString( initiateEntry( pName ), pValue );
        }

        @Override
        public void addAttribute( String pName, Timestamp pValue ) {
            addCommonAttributeAsString( initiateEntry( pName ), Timestamps.toISO8601ZuluSimple( pValue ) );
        }

        private String validateName( String pName ) {
            return ConstrainTo.significantOrNull( pName );
        }
    }
}
