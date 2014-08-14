package org.litesoft.json.shared;

import org.litesoft.commonfoundation.typeutils.*;

public class SupportingVersion {
    private final Double mDefaultVersion;
    private final double mCurrentVersion;
    private final double[] mSupportedVersions;

    public Double getDefaultVersion() {
        return mDefaultVersion;
    }

    public double getCurrentVersion() {
        return mCurrentVersion;
    }

    public double[] getSupportedVersions() {
        return mSupportedVersions;
    }

    public static Builder of( double pCurrentVersion ) {
        return new Builder( pCurrentVersion );
    }

    public static class Builder extends AbstractBuilder {
        private final double mCurrentVersion;
        private Double mDefaultVersion;
        private double[] mOtherVersions;

        private Builder( double pCurrentVersion ) {
            mCurrentVersion = pCurrentVersion;
        }

        public Builder defaultingTo( double pDefaultVersion ) {
            mDefaultVersion = assertSetOnce( mDefaultVersion, "DefaultVersion", pDefaultVersion );
            return this;
        }

        public Builder andAccepting( double pOtherVersion, double... pOtherVersions ) {
            mOtherVersions = assertSetOnce( mOtherVersions, "OtherVersions", Doubles.prepend( pOtherVersion, pOtherVersions ) );
            return this;
        }

        public SupportingVersion build() {
            return new SupportingVersion( this );
        }
    }

    private SupportingVersion( Builder pBuilder ) {
        this.mDefaultVersion = pBuilder.mDefaultVersion;
        this.mSupportedVersions = Doubles.prepend( this.mCurrentVersion = pBuilder.mCurrentVersion,
                                                   pBuilder.mOtherVersions );
    }
}
