package com.pirant.obole.network;

import com.pirant.obole.utils.ClipboardHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;


public class LocalHttpReceiver {
    public static final int SERVER_PORT = 5050;

    public void start() {
        try{
            HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
            server.createContext("/clipboard", new ClipboardHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e){
            System.err.println("Error while starting HTTPServer:" + e.getMessage());
        }

    }
}
