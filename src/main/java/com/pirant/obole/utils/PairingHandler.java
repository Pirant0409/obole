package com.pirant.obole.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PairingHandler implements HttpHandler {

    public void send(String receiver){
        try {
            SecurityManager sm = new SecurityManager();
            String pk = sm.getPublicKeyBase64();
            if (pk != null){
                System.out.println("Sending " + pk + " to " + receiver);
                URL url = new URL(receiver);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                try (OutputStream os = conn.getOutputStream()){
                    os.write(pk.getBytes("UTF-8"));
                }
                int responseCode = conn.getResponseCode();
                System.out.println("Sent to " + receiver + " - response: " + responseCode);
                System.out.println("Short code : " + sm.getPublicKeyShortCode());

            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())){
            String body = new String(exchange.getRequestBody().readAllBytes());
            try {
                SecurityManager sm = new SecurityManager();
                String remoteShortCode = sm.getRemoteShortCode(body);
                System.out.println("Short code : " + remoteShortCode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }
    }
}
