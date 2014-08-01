package org.litesoft.json.shared;

public interface ToJson {
    void toJsonAsNamedAttributes( JsonBuilder pBuilder );

    void toJsonAsListEntryValue( JsonBuilder pBuilder );
}
