module com.sound_collage {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires TarsosDSP;
    requires javafx.graphics;

    opens com.sound_collage to javafx.fxml;
    exports com.sound_collage;
    exports com.sound_collage.controllers;
    opens com.sound_collage.controllers to javafx.fxml;
}