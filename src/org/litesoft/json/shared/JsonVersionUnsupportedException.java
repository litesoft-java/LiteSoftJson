package org.litesoft.json.shared;

public class JsonVersionUnsupportedException extends JsonVersionException {
    private final double mVersion;

    public JsonVersionUnsupportedException( double pVersion ) {
        mVersion = pVersion;
    }

    public double getVersion() {
        return mVersion;
    }
}

