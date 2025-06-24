package com.pirant.obole.models;

import java.io.File;

public class Device {
    private final String name;
    private final String fingerPrint;
    private final String publicKey;

    public Device(String name, String fingerprint, String publicKey) {
        this.name = name;
        this.fingerPrint = fingerprint;
        this.publicKey = publicKey;
    }

    public String getName() {
        return name;
    }
    public String getFingerPrint() {
        return fingerPrint;
    }
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return "Device [name=" + name +
                "; fingerPrint=" + fingerPrint +
                "; publicKey=" + publicKey + "]";
    }
}
