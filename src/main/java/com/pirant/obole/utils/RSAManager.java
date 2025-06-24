package com.pirant.obole.utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAManager {
    private static final String KEY_FOLDER = System.getProperty("user.home") + File.separator + ".obole";
    private static final String PUBLIC_KEY_FILE = KEY_FOLDER + File.separator + "public.key";
    private static final String PRIVATE_KEY_FILE = KEY_FOLDER + File.separator + "private.key";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSAManager() throws Exception {

        if (keyExist()){
            loadKeys();
        }
        else {
            generateKeys();
            saveKeys();
        }
    }

    public boolean keyExist() {
        return Files.exists(Paths.get(PUBLIC_KEY_FILE)) && Files.exists(Paths.get(PRIVATE_KEY_FILE));
    }

    public void loadKeys() throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(Files.readAllBytes(Paths.get(PUBLIC_KEY_FILE)));
        byte[] privateKeyBytes = Base64.getDecoder().decode(Files.readAllBytes(Paths.get(PRIVATE_KEY_FILE)));

        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        this.privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
    }

    public void generateKeys() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    public void saveKeys() throws Exception {
        Files.createDirectories(Paths.get(KEY_FOLDER));

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(this.publicKey.getEncoded());
        Files.write(Paths.get(PUBLIC_KEY_FILE), Base64.getEncoder().encode(keySpec.getEncoded()));

        PKCS8EncodedKeySpec keySpec2 = new PKCS8EncodedKeySpec(this.privateKey.getEncoded());
        Files.write(Paths.get(PRIVATE_KEY_FILE), Base64.getEncoder().encode(keySpec2.getEncoded()));
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String getFingerPrintHex(String pkb64) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(pkb64.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02X", b));
        }

        return hexString.toString();
    }

    public String getFingerPrintBase64(String pkb64) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(pkb64.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public String getShortCode(String pkb64) throws Exception {
        String fingerPrintHex = getFingerPrintHex(pkb64);
        return fingerPrintHex.substring(0, 6);
    }
}
