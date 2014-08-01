package org.litesoft.json.shared;

public class JsonTypedValue {
    private final double mValue;
    private final String mType;

    private JsonTypedValue( double pValue, String pType ) {
        mValue = pValue;
        mType = pType;
    }

    public String getType() {
        return mType;
    }

    public double getValue() {
        return mValue;
    }

    public static JsonTypedValue from( JsonObject pObject, String pName ) {
        if ( pObject != null ) {
            String zType = pObject.getString( pName + "Type" );
            if ( zType != null ) {
                Double zValue = pObject.getDouble( pName + "Value" );
                if ( zValue != null ) {
                    return new JsonTypedValue( zValue, zType );
                }
            }
        }
        return null;
    }
}
