package com.pirant.obole.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ClipboardHandler implements HttpHandler {

    public void handle (HttpExchange exchange) throws IOException{

        System.out.println("test");
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
}
