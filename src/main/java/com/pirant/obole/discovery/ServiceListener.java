package com.pirant.obole.discovery;

import javafx.application.Platform;
import javafx.collections.ObservableList;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class ServiceListener implements javax.jmdns.ServiceListener{

    private final String localHostAddress;
    private final ObservableList<String> discoveredDevices;

    public ServiceListener(ObservableList<String> discoveredDevices) {
        String localAddress = "127.0.0.1";
        this.discoveredDevices = discoveredDevices;
        try{
            localAddress = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e){
            System.err.println("Could not get local host address " + e.getMessage());
        }
        this.localHostAddress = localAddress;
    }
    @Override
    public void serviceAdded(ServiceEvent event) {
        System.out.println("Service added: " + event.getName());
        event.getDNS().requestServiceInfo(event.getType(), event.getName(), true);
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        System.out.println("Service removed: " + event.getName());
    }

    @Override
    public void serviceResolved(ServiceEvent event) {

        Platform.runLater(() ->{
            ServiceInfo info = event.getInfo();
            String remoteAddress = info.getHostAddresses()[0];

            if (!remoteAddress.equals(localHostAddress)){
                String entry = info.getName() + " - " + remoteAddress + " : " + info.getPort();
                if (!discoveredDevices.contains(entry)){
                    System.out.println("Service resolved: " + entry);
                    discoveredDevices.add(entry);
                }
        }});

    }
}
