package com.pirant.obole.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClipboardHandler implements HttpHandler {

    public void send(String receiver){
        String clipboardText = getClipboard();

        if (clipboardText != null){
            try{
                System.out.println("Tentative d'envoi vers " + receiver);
                URL url = new URL(receiver);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

                try (OutputStream os = conn.getOutputStream()){
                    os.write(clipboardText.getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                System.out.println("Sent to " + receiver + " - response: " + responseCode);
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
    public void handle (HttpExchange exchange) throws IOException{

        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes());
            System.out.println("Received clipboard: " + body);

            //paste in this device clipboard
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(body);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

            String response = "Received";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public String getClipboard(){
        String cbText;
        try {
            cbText = (String) Toolkit.getDefaultToolkit().
                    getSystemClipboard().
                    getData(DataFlavor.stringFlavor);
        } catch (Exception ex){
            ex.printStackTrace();
            return "null";
        }
        return cbText;
    }
}
