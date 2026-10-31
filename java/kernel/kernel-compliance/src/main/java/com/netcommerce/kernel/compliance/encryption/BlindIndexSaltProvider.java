package com.netcommerce.kernel.compliance.encryption;

/** The secret salt behind blind indexes, by version, so old indexes still search. */
public interface BlindIndexSaltProvider {

    byte[] getCurrentSalt();

    /** A historical salt; null when that version is no longer available. */
    byte[] getSaltByVersion(int version);

    int getCurrentSaltVersion();
}
