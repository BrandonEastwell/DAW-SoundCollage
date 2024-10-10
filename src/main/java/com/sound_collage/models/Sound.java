package com.sound_collage.models;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Sound {
    public SourceDataLine sourceDataLine;
    public AudioFormat format;
    public byte[] bytes;
    public ByteBuffer byteBuffer;
    int bytesRead;
    String filePath; // Add a variable to store the file path
    public AudioInputStream audioInputStream;
    String fileName;
    int volume;
    public WaveVisualization wave;
    public File file;
    public Sound(String filePath) {
        update(filePath);
    }
    public void update(String filePath) {
        System.out.println(filePath);
        this.filePath = filePath;
        try {
            file = new File(filePath);
            fileName = file.getName();
            audioInputStream = Utilities.uploadSoundFileToAudioStream(filePath);
            format = audioInputStream.getFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(format);

            byte[] buffer = new byte[sourceDataLine.getBufferSize()];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            sourceDataLine.start();
            while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            baos.flush();
            bytes = baos.toByteArray();
            byteBuffer = ByteBuffer.wrap(bytes);
            sourceDataLine.stop();
            sourceDataLine.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void adjustVolume(int volume) {
        this.volume = volume;
        try {
            for (int i = 0; i < bytes.length; i+=2) {
                // convert byte pair to int
                short buf1 = bytes[i + 1];
                short buf2 = bytes[i];

                buf1 = (short) ((buf1 & 0xff) << 8);
                buf2 = (short) (buf2 & 0xff);

                short res = (short) (buf1 | buf2);
                res = (short) (res * (this.volume / 100f));

                // convert back
                bytes[i] = (byte) res;
                bytes[i + 1] = (byte) (res >> 8);
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }

    public String getFilePath() {
        return filePath;
    }
}
