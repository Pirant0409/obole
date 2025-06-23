package com.pirant.obole.network;

import com.pirant.obole.utils.ClipboardHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;


public class LocalHttpReceiver {
    public static final int SERVER_PORT = 5050;
    private HttpServer server;

    public void start() {

        if (server == null) {
            try{
                server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
                server.createContext("/clipboard", exchange -> {
                    new ClipboardHandler().handle(exchange);
                });
                server.setExecutor(null);
                server.start();
                System.out.println("[HTTP RECEIVER] Listening on http://" + InetAddress.getLocalHost().getHostAddress() + ":" + SERVER_PORT);
            } catch (IOException e){
                System.err.println("Error while starting HTTPServer:" + e.getMessage());
            }

        }

    }
}
