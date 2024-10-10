package com.sound_collage.models;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.MultichannelToMono;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.WaveformWriter;
import be.tarsos.dsp.resample.RateTransposer;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Scanner;

public class Utilities {
    public static AudioInputStream uploadSoundFileToAudioStream(String filePath) throws IOException, UnsupportedAudioFileException {
        try {
            return AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
        } catch (UnsupportedAudioFileException e) {
            throw new UnsupportedAudioFileException();
        } catch (IOException e) {
            throw new IOException();
        }
    }

    public static AudioInputStream getAudioInputStreamFromByteBuffer(ByteBuffer buffer, AudioFormat format) {
        byte[] byteArray = buffer.array();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        return new AudioInputStream(byteArrayInputStream, format, byteArray.length / format.getFrameSize());
    }

    public static ByteBuffer uploadSoundFileToByteBuffer(String filePath) throws IOException {
        if (!Objects.equals(filePath.split("\\.")[1], "wav")) throw new IOException();
        DataInputStream in = new DataInputStream(new FileInputStream(filePath));
        byte[] sound = new byte[in.available() - 44];
        // skip the header
        in.skipNBytes(44);
        return ByteBuffer.wrap(sound);
    }

    public static void playAudio(AudioInputStream audioInputStream) {
        //new thread so sound runs independently
        Thread thread = new Thread(() -> {
            Clip clip;
            try {
                //turn stream into clip
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
            } catch (LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            }
            //start playing clip
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            Scanner option = new Scanner(System.in);
            while (true) {
                //offer options to user
                System.out.println("Playing music!");
                System.out.println("Pause: 1");
                System.out.println("Stop: 2");
                System.out.println("Play: 3");
                //get option from user
                String a = option.nextLine();
                long pause = 0;
                //handle options
                if (Objects.equals(a, "1")) {
                    pause = clip.getMicrosecondLength();
                    clip.stop();
                }
                if (Objects.equals(a, "2")) {
                    break;
                }
                if (Objects.equals(a, "3")) {
                    clip.setMicrosecondPosition(pause);
                    clip.start();
                }
            }
        });
        thread.start();
    }

    public static boolean exportFileToWAV(AudioInputStream audioInputStream, String path) throws IOException {
        return AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(path)) > 0;
    }

    public static void alterPitch(String sourceFileLocation, String targetFileLocation, double cents) throws UnsupportedAudioFileException, IOException {
        // cents ranges between 2,786.314 to -1,586.314
        File inputFile = new File(sourceFileLocation);
        AudioFormat format = AudioSystem.getAudioFileFormat(inputFile.getAbsoluteFile()).getFormat();

        double sampleRate = format.getSampleRate();
        double factor = centToFactor(cents);

        RateTransposer rateTransposer = new RateTransposer(factor);
        WaveformSimilarityBasedOverlapAdd wsola = new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(factor, sampleRate));
        WaveformWriter writer = new WaveformWriter(format, targetFileLocation);
        AudioDispatcher dispatcher;

        if (format.getChannels() != 1) {
            dispatcher = AudioDispatcherFactory.fromFile(inputFile.getAbsoluteFile(), wsola.getInputBufferSize() * format.getChannels(), wsola.getOverlap() * format.getChannels());
            dispatcher.addAudioProcessor(new MultichannelToMono(format.getChannels(), true));
        } else {
            dispatcher = AudioDispatcherFactory.fromFile(inputFile.getAbsoluteFile(), wsola.getInputBufferSize(), wsola.getOverlap());
        }
        wsola.setDispatcher(dispatcher);
        dispatcher.addAudioProcessor(wsola);
        dispatcher.addAudioProcessor(rateTransposer);
        dispatcher.addAudioProcessor(writer);
        dispatcher.run();
    }

    private static double centToFactor(double cents) {
        return 1 / Math.pow(Math.E, cents * Math.log(2) / 1200 / Math.log(Math.E));
    }

    public static void deleteFiles(File ... files){
        for (File file: files){
            file.delete();
        }
    }
}