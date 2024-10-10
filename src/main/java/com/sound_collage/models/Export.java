package com.sound_collage.models;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;

import static java.lang.StrictMath.ceil;

public class Export {

    private static short audioFormat = 1;
    private static int sampleRate = 44100;
    private static short sampleSize = 16;
    private static short channels = 2;
    private static short blockAlign = (short) (sampleSize * channels / 8);
    private static int byteRate = sampleRate * sampleSize * channels / 8;


    public static void appendFiles(Sound file1, Sound file2, File fileToSave) {
        try {

            AudioInputStream appendFiles =
                    new AudioInputStream(new SequenceInputStream(file1.audioInputStream, file2.audioInputStream),
                            file1.format,
                            file1.audioInputStream.getFrameLength() + file2.audioInputStream.getFrameLength());
            AudioSystem.write(appendFiles, AudioFileFormat.Type.WAVE, fileToSave);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int secondsToByte(double seconds) {
        //int byteRate = (int) (sound1.format.getSampleRate() * sound1.format.getSampleSizeInBits() * sound1.format.getChannels() / 8);
        //int seconds = (int) (sound1.audioInputStream.getFrameLength() / sound1.format.getFrameRate());
        //System.out.println(seconds * byteRate);
        return (int) ceil(seconds * byteRate);
    }

    public static ByteBuffer mixFiles(ByteBuffer smaller, ByteBuffer larger) {
        ByteBuffer outputBuffer = ByteBuffer.allocate(larger.capacity());

        while (larger.hasRemaining()) {
            short sum = Short.reverseBytes(larger.getShort());
            int matches = 1;

            if (smaller.hasRemaining()) {
                sum += Short.reverseBytes(smaller.getShort());
                matches++;
            }
            outputBuffer.putShort(Short.reverseBytes((short) (sum / (float) matches)));
        }
        return outputBuffer;
    }

    public static byte[] mixBuffers(Sound[] sounds, int capacity) {
        byte[] array = new byte[capacity];
        short[] buffA1 = new short[sounds.length];
        short[] buffA2 = new short[sounds.length];
        int count;
        try {
            for (int i = 0; i < array.length; i += 2) {
                count = 0;
                for (Sound sound : sounds) {
                    short buf1 = sound.bytes[i + 1];
                    short buf2 = sound.bytes[i];
                    buf1 = (short) ((buf1 & 0xff) << 8);
                    buf2 = (short) (buf2 & 0xff);
                    buffA1[count] = buf1;
                    buffA2[count] = buf2;
                    count++;
                }
                short bufF1 = 0;
                short bufF2 = 0;
                for (int n : buffA1) {
                    bufF1 += (short) n;
                }
                for (int n : buffA2) {
                    bufF2 += (short) n;
                }
                short result = (short) (bufF1 + bufF2);
                array[i] = (byte) result;
                array[i + 1] = (byte) (result >> 8);
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {

        }
        return array;
    }

    public static void saveToFile(File file, ByteBuffer audioData, AudioFormat format) throws IOException {
        AudioInputStream audioInputStream = Utilities.getAudioInputStreamFromByteBuffer(audioData, format); // Gets the AudioInputStream from the mixed buffer
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
    }
}