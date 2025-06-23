package com.pirant.obole.discovery;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;


public class ServiceAdvertiser {

    private final JmDNS jmdns;
    private final int port;
    private ServiceInfo serviceInfo;
    public static final int SERVER_PORT = 5050;

    public ServiceAdvertiser(JmDNS jmdns, int port) {
        this.jmdns = jmdns;
        this.port = port;
    }

    public void start (){
        try{
            String serviceType = "_obole._tcp.local.";
            String serviceName = InetAddress.getLocalHost().getHostName();
            String description = "Obole Service Advertiser";

            serviceInfo = ServiceInfo.create(serviceType,serviceName,port,description);

            jmdns.registerService(serviceInfo);

            System.out.println("Service Advertiser started");
        } catch (IOException e) {
            System.err.println("Error while starting advertiser:" + e.getMessage());
        }
    }

    public void stop(){
        if (serviceInfo != null){
            jmdns.unregisterService(serviceInfo);
            serviceInfo = null;
            System.out.println("Service Advertiser stopped");
        }
    }
}