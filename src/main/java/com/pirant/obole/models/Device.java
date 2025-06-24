package com.pirant.obole.models;

import java.io.File;

public class Device {
    private final String name;
    private final String fingerPrint;
    private final String publicKey;

    public Device(String name, String publicKey, String fingerprint) {
        this.name = name;
        this.publicKey = publicKey;
        this.fingerPrint = fingerprint;
    }

    public String getName() {
        return name;
    }
    public String getPublicKey() {
        return publicKey;
    }
    public String getFingerPrint() {
        return fingerPrint;
    }

    @Override
    public String toString() {
        return "Device [name=" + name +
                "; public key=" + publicKey +
                "; fingerprint=" + fingerPrint + "]";
    }
}
