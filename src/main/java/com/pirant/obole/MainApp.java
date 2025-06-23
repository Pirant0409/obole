package com.pirant.obole;

import com.pirant.obole.discovery.ServiceAdvertiser;
import com.pirant.obole.discovery.ServiceListener;
import com.pirant.obole.network.LocalHttpReceiver;
import com.pirant.obole.utils.ClipboardHandler;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
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

    @Override
    public void start(Stage primaryStage){
        ListView<String> listView = new ListView<>(discoveredDevices);

        Button clipBoardButton = new Button("Send clipboard");
        clipBoardButton.setOnAction(e->{
            ClipboardHandler cbhandler = new ClipboardHandler();
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Pattern pattern = Pattern.compile(".* - ([0-9.]+):(\\d+)");
                Matcher matcher = pattern.matcher(selected);
                if (!matcher.matches()){
                    System.err.println("Unknown format: " + selected);
                    return;
                }

                String ip = matcher.group(1);
                String port = matcher.group(2);
                //TODO: Hard-coded 5050 port. Should get the right port (not the obole service listener one but the HTTP receiver one)
                String receiver = "http://" + ip + ":" + 5050 + "/clipboard";
                new Thread(()->{
                    cbhandler.send(receiver);
                }).start();
            }
        });

        VBox root = new VBox(listView, clipBoardButton);
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

                new LocalHttpReceiver().start();
            } catch (IOException e){
                System.err.println(e);
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
