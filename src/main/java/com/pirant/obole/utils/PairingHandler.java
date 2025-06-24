package com.pirant.obole.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pirant.obole.models.Device;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class PairingHandler implements HttpHandler {

    public void send(String receiver){
        try {
            RSAManager sm = new RSAManager();
            Gson gson = new Gson();

            //Creating device & storing it in a json object
            String deviceName = InetAddress.getLocalHost().getHostName();
            String pk = sm.getPublicKeyBase64();
            String fp = sm.getFingerPrintBase64(pk);
            Device pr = new Device(deviceName, pk, fp);
            String json = gson.toJson(pr);

            System.out.println("Sending" + json + "to " + receiver);

            //Creating connection
            URL url = new URI(receiver).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            //Sending pairing message to the other device
            try (OutputStream os = conn.getOutputStream()){
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            System.out.println("Short code : " + sm.getShortCode(pk));

            int responseCode = conn.getResponseCode();
            //Handling answer
            try(InputStream is = conn.getInputStream()){
                String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Device remoteDevice = gson.fromJson(response, Device.class);
                System.out.println("Received device : " + remoteDevice);
                if (responseCode == 200 && !PairedUtils.isDeviceKnown(remoteDevice.getFingerPrint())){
                    PairedUtils.saveDevice(remoteDevice);
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())){

            Gson gson = new Gson();

            //Reading message and parsing json object to get device data
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Device remoteDevice = gson.fromJson(body, Device.class);

            try {
                RSAManager sm = new RSAManager();

                //Generating shortCode to check if the publicKey hasn't been altered
                String remoteShortCode = sm.getShortCode(remoteDevice.getPublicKey());
                Platform.runLater(()->
                {
                    boolean confirmed = showConfirmationDialog(remoteDevice.getName(), remoteShortCode);
                    try {

                        //Sending answer to the other device
                        String deviceName = InetAddress.getLocalHost().getHostName();
                        String publicKey = sm.getPublicKeyBase64();
                        String fingerPrint = sm.getFingerPrintBase64(publicKey);
                        Device device = new Device(deviceName, publicKey, fingerPrint);
                        String json = gson.toJson(device);

                        String response = confirmed ? json : "NOK";
                        exchange.sendResponseHeaders(confirmed ? 200 : 403, response.length());
                        if (confirmed){
                            PairedUtils.saveDevice(remoteDevice);
                        }
                        try (OutputStream os = exchange.getResponseBody()) {
                           os.write(response.getBytes(StandardCharsets.UTF_8));
                        }


                    } catch (Exception ex){
                        ex.printStackTrace();
                   }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }
    }

    private boolean showConfirmationDialog(String deviceName, String shortCode) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Pairing");
        alert.setHeaderText("Pairing request from " + deviceName);
        alert.setContentText("Does the following code matches the code displayed on the other device ?\n" + shortCode);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
