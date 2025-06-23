package com.pirant.obole;

import com.pirant.obole.discovery.ServiceAdvertiser;
import com.pirant.obole.discovery.ServiceListener;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.jmdns.JmDNS;
import java.io.IOException;
import java.net.InetAddress;

public class MainApp extends Application{

    private final ObservableList<String> discoveredDevices = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage){
        ListView<String> listView = new ListView<>(discoveredDevices);

        VBox root = new VBox(listView);
        Scene scene = new Scene(root, 400,300);

        primaryStage.setTitle("Obole");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(()->{
            JmDNS jmdns = null;
            try{
                InetAddress localAddress = InetAddress.getLocalHost();
                jmdns = JmDNS.create(localAddress);
                ServiceAdvertiser advertiser = new ServiceAdvertiser(jmdns,5000);
                advertiser.start();

                ServiceListener listener = new ServiceListener(discoveredDevices);
                jmdns.addServiceListener("_obole._tcp.local.", listener);
            } catch (IOException e){
                System.err.println(e);
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
