package model_tests;

import com.sound_collage.models.VolumeControl;
import javafx.application.Application;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class VolumeControlTest {
    @Test
    void launchSliderInterfaceOpensSuccessfullyForSliderStage() {
        try {
            Application.launch(String.valueOf(VolumeControl.class));
        } catch (Exception e) {
            fail();
        }
    }
}