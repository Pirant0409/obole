module com.pirant.obole {
    requires javafx.controls;
    requires javafx.fxml;
    requires javax.jmdns;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires jdk.httpserver;
    requires java.datatransfer;
    requires java.desktop;
    requires com.google.gson;
    requires org.slf4j;

    opens com.pirant.obole to javafx.fxml;
    opens com.pirant.obole.models to com.google.gson;
    exports com.pirant.obole;
}