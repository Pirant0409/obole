package com.pirant.obole.models;

public class PairingRequest {
    private String publicKey;
    private String deviceName;

    public PairingRequest(String publicKey, String deviceName) {
        this.publicKey = publicKey;
        this.deviceName = deviceName;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
