package com.sound_collage.models;
//import GUI.PlayerGUI;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * class to control the volume level of a clip file
 *
 *
 * Known bugs involve errors when changing the gain value during a slider change so volume control
 * does not actually work
 * Volume control needs a different solution that does not use decibels and is ideally independent
 * from the system volume controls so that multiple clip files can be adjusted separately.
 */
public class VolumeControl {
    BorderPane view;
    Slider slider;
    HBox box;
    Label label;
    Sound sound;
    public VolumeControl(Sound sound) {
        this.sound = sound;
        initializeUI();
        label.setText(String.valueOf(sound.volume));
        slider.valueProperty().addListener((observableValue, number, t1) -> {
            label.setText(Integer.toString((int) slider.getValue()));
            int value = (int) slider.getValue();
            sound.adjustVolume(value);
        });
        view.setCenter(box);
    }
    public void initializeUI() {
        view = new BorderPane();
        slider = new Slider(0, 100, 50);
        slider.setPrefWidth(125);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setBlockIncrement(1);
        slider.setMinorTickCount(3);
        slider.setMajorTickUnit(10);
        slider.setSnapToTicks(true);
        label = new Label("volume");
        box = new HBox(slider, label);
        box.setAlignment(Pos.CENTER);
    }
}
