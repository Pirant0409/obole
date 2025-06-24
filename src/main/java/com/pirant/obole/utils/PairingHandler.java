package com.pirant.obole.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pirant.obole.models.PairingRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Optional;

public class PairingHandler implements HttpHandler {

    public void send(String receiver){
        try {
            RSAManager sm = new RSAManager();
            Gson gson = new Gson();
            String deviceName = InetAddress.getLocalHost().getHostName();

            String pk = sm.getPublicKeyBase64();
            PairingRequest pr = new PairingRequest(pk, deviceName);
            String json = gson.toJson(pr);
            if (pk != null){
                System.out.println("Sending" + json  + "to " + receiver);
                URL url = new URL(receiver);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream os = conn.getOutputStream()){
                    os.write(json.getBytes("UTF-8"));
                }
                System.out.println("Short code : " + sm.getPublicKeyShortCode());
                String responseCode = conn.getResponseMessage();
                System.out.println("Sent to " + receiver + " - response: " + responseCode);

            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())){
            String body = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            String publicKey = json.get("publicKey").getAsString();
            String deviceName = json.get("deviceName").getAsString();
            try {
                RSAManager sm = new RSAManager();
                String remoteShortCode = sm.getPublicKeyShortCode(publicKey);
                Platform.runLater(()->{
                   boolean confirmed = showConfirmationDialog(deviceName, remoteShortCode);
                   try {
                       String response = confirmed ? "OK" : "DECLINED";
                       exchange.sendResponseHeaders(confirmed ? 200 : 403, response.length());
                       try (OutputStream os = exchange.getResponseBody()) {
                           os.write(response.getBytes("UTF-8"));
                       }
                   } catch (IOException ex){
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
