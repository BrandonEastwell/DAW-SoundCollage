package com.sound_collage.models;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class WaveVisualization {
    public ArrayList<SampleData> samples = new ArrayList<>();
    private Canvas waveCanvas;
    private final int SAMPLE_RATE = 44100; // Adjust this according to your actual sample rate
    private final int MAX_SECONDS_TO_DISPLAY = 2;
    private final int MAX_SAMPLES = SAMPLE_RATE * MAX_SECONDS_TO_DISPLAY;

    public WaveVisualization(Canvas waveCanvas) {
        this.waveCanvas = waveCanvas;
    }

    private void removeOldSamples(long currentTimestamp) {
        long oldestTimestampToKeep = currentTimestamp - (MAX_SECONDS_TO_DISPLAY * 1000);
        samples.removeIf(sampleData -> sampleData.getTimestamp() < oldestTimestampToKeep);
    }

    public void updateWaveform(byte[] audioBytes, AudioFormat format) {
        ByteBuffer buffer = ByteBuffer.wrap(audioBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int numSamples = audioBytes.length / format.getFrameSize();
        long currentTimestamp = System.currentTimeMillis();
        removeOldSamples(currentTimestamp);

        int samplesToAdd = Math.min(numSamples, MAX_SAMPLES - samples.size());

        for (int i = 0; i < samplesToAdd; i++) {
            float normalizedSample = buffer.getShort() / (float) Math.pow(2, format.getSampleSizeInBits() - 1);
            samples.add(new SampleData(normalizedSample, currentTimestamp));
        }

        Platform.runLater(this::drawWaveform);
    }

    private void drawWaveform() {
        GraphicsContext gc = waveCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, waveCanvas.getWidth(), waveCanvas.getHeight());
        gc.setStroke(Color.BLACK);

        if (!samples.isEmpty()) {
            for (int i = 1; i < samples.size(); i++) {
                int x1 = (int) ((i - 1) * (waveCanvas.getWidth() / samples.size()));
                int y1 = (int) ((1 + samples.get(i - 1).getValue()) * waveCanvas.getHeight() / 2);
                int x2 = (int) (i * (waveCanvas.getWidth() / samples.size()));
                int y2 = (int) ((1 + samples.get(i).getValue()) * waveCanvas.getHeight() / 2);

                // Draw a smoother line using interpolation
                int steps = 10; // Adjust this value for more or less interpolation steps
                for (int step = 0; step <= steps; step++) {
                    double t = (double) step / steps;
                    int x = (int) (x1 + t * (x2 - x1));
                    int y = (int) (y1 + t * (y2 - y1));
                    gc.strokeRect(x, y, 1, 1); // Draw a small point
                }
            }
        }
    }


    private static class SampleData {
        private final float value;
        private final long timestamp;

        public SampleData(float value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public float getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

}
