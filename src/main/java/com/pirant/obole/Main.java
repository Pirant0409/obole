package com.pirant.obole;

import com.pirant.obole.discovery.ServiceAdvertiser;
import com.pirant.obole.discovery.ServiceListener;

import javax.jmdns.JmDNS;
import java.io.IOException;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args) {
        JmDNS jmdns = null;

        try {

            //Setup
            InetAddress localAddress = InetAddress.getLocalHost();
            jmdns = JmDNS.create(localAddress);

            //Advertiser
            ServiceAdvertiser advertiser = new ServiceAdvertiser(jmdns, 5000);
            advertiser.start();

            //Listener
            ServiceListener listener = new ServiceListener();
            jmdns.addServiceListener("_obole._tcp.local.", listener);

            System.out.println("Network descovery on. Press enter to quit.");
            System.in.read();

        } catch (IOException e) {
            System.err.println("JmDNS initialisation error : " + e.getMessage());
        } finally {
            // Nettoyage
            if (jmdns != null) {
                try {
                    jmdns.close();
                } catch (IOException e) {
                    System.err.println("JmDNS closing error: " + e.getMessage());
                }
            }
        }

        System.out.println("App off");
    }
}
