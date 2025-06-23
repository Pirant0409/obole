package com.pirant.obole.discovery;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;

public class ServiceListener implements javax.jmdns.ServiceListener{
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
        ServiceInfo info = event.getInfo();
        System.out.println("Service resolved: " + info.getName());
    }
}
