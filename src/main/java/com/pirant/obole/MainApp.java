package com.pirant.obole;

import com.pirant.obole.discovery.ServiceAdvertiser;
import com.pirant.obole.discovery.ServiceListener;
import com.pirant.obole.network.LocalHttpReceiver;
import com.pirant.obole.utils.ClipboardHandler;
import com.pirant.obole.utils.PairingHandler;
import com.pirant.obole.utils.SecurityManager;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.jmdns.JmDNS;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainApp extends Application{

    private final ObservableList<String> discoveredDevices = FXCollections.observableArrayList();
    private final ObservableList<String> pairedDevices = FXCollections.observableArrayList();
    @Override
    public void start(Stage primaryStage) throws Exception {

        TabPane tabPane = setupTabs();
        Scene scene = new Scene(tabPane, 400,300);

        primaryStage.setTitle("Obole");
        primaryStage.setScene(scene);
        primaryStage.show();

        runDiscovery();
    }

    public TabPane setupTabs(){

        Tab discoveryTab = setupDiscoveryTab();
        Tab pairedTab = setupPairedTab();
        TabPane tabPane = new TabPane(pairedTab,discoveryTab);
        return tabPane;

    }

    public Tab setupDiscoveryTab(){
        ListView<String> discoveryListView = new ListView<>(discoveredDevices);

        Button pairButton = new Button("Pair");
        pairButton.setOnAction(event -> {
            PairingHandler phandler = new PairingHandler();
            String selected = discoveryListView.getSelectionModel().getSelectedItem();
            if (selected != null && !pairedDevices.contains(selected)){
                String receiver = findAddress(selected);
                if (receiver != null){
                    new Thread(() -> {
                        try {
                            phandler.send(receiver+"/pair");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }).start();
                }
            }
        });
        VBox discoveryTabContent = new VBox(discoveryListView, pairButton);
        Tab discoveryTab = new Tab("Discovery", discoveryTabContent);

        return discoveryTab;
    }

    public Tab setupPairedTab(){
        ListView<String> pairedDevicesListView = new ListView<>(pairedDevices);

        Button clipBoardButton = new Button("Send clipboard");
        clipBoardButton.setOnAction(e->{
            ClipboardHandler cbhandler = new ClipboardHandler();
            String selected = pairedDevicesListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String receiver = findAddress(selected);
                if (receiver != null) {
                    new Thread(()->{
                        cbhandler.send(receiver+"/clipboard");
                    }).start();

                }
            }
        });

        VBox pairedDevicesTabContent = new VBox(pairedDevicesListView, clipBoardButton);
        Tab pairedTab = new Tab("Paired Devices", pairedDevicesTabContent);

        return pairedTab;
    }

    public void runDiscovery(){
        new Thread(()->{
            JmDNS jmdns = null;
            try{
                InetAddress localAddress = InetAddress.getLocalHost();
                jmdns = JmDNS.create(localAddress);
                ServiceAdvertiser advertiser = new ServiceAdvertiser(jmdns,5000);
                advertiser.start();

                ServiceListener listener = new ServiceListener(discoveredDevices);
                jmdns.addServiceListener("_obole._tcp.local.", listener);

                new LocalHttpReceiver().start();
            } catch (IOException e){
                System.err.println(e);
            }
        }).start();
    }

    public String findAddress(String selected){
        Pattern pattern = Pattern.compile(".* - ([0-9.]+):(\\d+)");
        Matcher matcher = pattern.matcher(selected);
        if (!matcher.matches()){
            System.err.println("Unknown format: " + selected);
            return null;
        }

        String ip = matcher.group(1);
        String port = matcher.group(2);
        //TODO: Hard-coded 5050 port. Should get the right port (not the obole service listener one but the HTTP receiver one)
        String receiver = "http://" + ip + ":" + 5050;
        return receiver;
    }
    public static void main(String[] args) {
        launch(args);
    }
}
