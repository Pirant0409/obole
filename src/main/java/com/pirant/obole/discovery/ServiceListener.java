package com.pirant.obole.discovery;

import com.pirant.obole.utils.PairedUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class ServiceListener implements javax.jmdns.ServiceListener{

    private final String localHostAddress;
    private final ObservableList<String> discoveredDevices;
    private final ObservableList<String> pairedDevices;

    public ServiceListener(ObservableList<String> discoveredDevices, ObservableList<String> pairedDevices) {
        String localAddress = "127.0.0.1";
        this.discoveredDevices = discoveredDevices;
        this.pairedDevices = pairedDevices;
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
        discoveredDevices.remove(event.getName());
    }

    @Override
    public void serviceResolved(ServiceEvent event) {

        Platform.runLater(() ->{
            boolean isKnown = false;
            ServiceInfo info = event.getInfo();
            String remoteAddress = info.getHostAddresses()[0];
            String fp = info.getPropertyString("fingerprint");
            System.out.println("Remote name: " + info.getName() + ", remote fp: " + fp);
            try {
                isKnown = PairedUtils.isDeviceKnown(fp);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (!remoteAddress.equals(localHostAddress) && fp != null){
                String entry = info.getName() + " - " + remoteAddress + ":" + info.getPort();
                if (!discoveredDevices.contains(entry) && !isKnown){
                    System.out.println("Service resolved: " + entry);
                    discoveredDevices.add(entry);
                }
                else if (!pairedDevices.contains(entry) && isKnown){
                    System.out.println("Service resolved: " + entry);
                    pairedDevices.add(entry);
                }
        }});

    }
}
