module lutz.niklas.merckprinter {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;
    requires com.jfoenix;
    requires javafx.swing;

    opens lutz.niklas.labelprinter to javafx.fxml;
    exports lutz.niklas.labelprinter;
}