package org.litesoft.json.shared;

public interface HasJson<JO extends HasJson<JO>> extends ToJson,
                                                         FromJson<JO> {
    String getFieldName();
}
