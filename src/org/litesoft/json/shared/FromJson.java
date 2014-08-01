package org.litesoft.json.shared;

public interface FromJson<JO extends FromJson<JO>> {
    JO fromJsonAsNamedAttributes( JsonObject pObject ); // pObject is Parent!

    JO fromJsonAsListEntryValue( JsonObject pObject ); // pObject is Us!

    void fromJsonComplete( Double pVersion );
}
