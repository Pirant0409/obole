package com.pirant.obole.discovery;

import com.pirant.obole.utils.RSAManager;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;


public class ServiceAdvertiser {

    private final JmDNS jmdns;
    private final int port;
    private ServiceInfo serviceInfo;
    public static final int SERVER_PORT = 5050;

    public ServiceAdvertiser(JmDNS jmdns, int port) {
        this.jmdns = jmdns;
        this.port = port;
    }

    public void start () throws Exception {
        RSAManager rsa = new RSAManager();

        try{
            String serviceType = "_obole._tcp.local.";
            String deviceName = InetAddress.getLocalHost().getHostName();
            Map<String, String> props = new HashMap<>();
            String deviceFingerPrint = rsa.getFingerPrintBase64(rsa.getPublicKeyBase64());
            props.put("fingerprint", deviceFingerPrint);
            System.out.println("props: " + props);
            serviceInfo = ServiceInfo.create(serviceType, deviceName,port,0,0, props);

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