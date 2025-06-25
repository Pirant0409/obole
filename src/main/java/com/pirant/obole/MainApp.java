package com.pirant.obole;

import com.pirant.obole.discovery.ServiceAdvertiser;
import com.pirant.obole.discovery.ServiceListener;
import com.pirant.obole.network.LocalHttpReceiver;
import com.pirant.obole.utils.ClipboardHandler;
import com.pirant.obole.utils.PairingHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainApp extends Application{

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    private final ObservableList<String> discoveredDevices = FXCollections.observableArrayList();
    private final ObservableList<String> pairedDevices = FXCollections.observableArrayList();
    private ServiceAdvertiser advertiser;


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
        return new TabPane(pairedTab,discoveryTab);

    }

    public Tab setupDiscoveryTab(){
        ListView<String> discoveryListView = new ListView<>(discoveredDevices);

        Button pairButton = new Button("Pair");
        pairButton.setOnAction(event -> {
            String selected = discoveryListView.getSelectionModel().getSelectedItem();
            if (selected != null){
                String receiver = findAddress(selected);
                if (receiver != null){
                    new Thread(() -> {
                        try {
                            PairingHandler.send(receiver+"/pair");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }).start();
                }
            }
        });
        VBox discoveryTabContent = new VBox(discoveryListView, pairButton);

        return new Tab("Discovery", discoveryTabContent);
    }

    public Tab setupPairedTab(){
        ListView<String> pairedDevicesListView = new ListView<>(pairedDevices);

        Button clipBoardButton = new Button("Send clipboard");
        clipBoardButton.setOnAction(e->{
            String selected = pairedDevicesListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String receiver = findAddress(selected);
                if (receiver != null) {
                    new Thread(()->{
                        ClipboardHandler.send(receiver+"/clipboard");
                    }).start();

                }
            }
        });

        VBox pairedDevicesTabContent = new VBox(pairedDevicesListView, clipBoardButton);

        return new Tab("Paired Devices", pairedDevicesTabContent);
    }

    public void runDiscovery(){
        new Thread(()->{
            JmDNS jmdns = null;
            try{
                InetAddress localAddress = InetAddress.getLocalHost();
                jmdns = JmDNS.create(localAddress);
                advertiser = new ServiceAdvertiser(jmdns,5000);
                advertiser.start();

                ServiceListener listener = new ServiceListener(discoveredDevices, pairedDevices);
                jmdns.addServiceListener("_obole._tcp.local.", listener);

                new LocalHttpReceiver().start();
            } catch (Exception e){
                log.error("e: ", e);
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
        return "http://" + ip + ":" + 5050;
    }
    @Override
    public void stop() throws Exception {
        if (advertiser != null){
            advertiser.stop();
        }
        super.stop();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
