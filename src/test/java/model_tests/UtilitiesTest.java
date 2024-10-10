package model_tests;

import com.sound_collage.models.Utilities;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.sound_collage.models.Utilities.alterPitch;
import static org.junit.jupiter.api.Assertions.*;

public class UtilitiesTest {
    @Test
    void uploadFileReturnsAnAudioStreamWhenFileUploadsCorrectly() throws IOException, UnsupportedAudioFileException {
        String pathToFile = "src/main/resources/sound_files/University_of_Essex.wav";

        assertInstanceOf(AudioInputStream.class, Utilities.uploadSoundFileToAudioStream(pathToFile));
    }

    @Test
    void uploadFileThrowsIOExceptionForAFileThatDoesNotExist() {
        String pathToFile = ".src/bla/bla/bla.wav";

        assertThrows(IOException.class, () -> Utilities.uploadSoundFileToAudioStream(pathToFile));
    }

    @Test
    void uploadFileThrowsUnsupportedAudioFileExceptionForAFileThatDoesNotExist() {
        String pathToFile = "src/main/resources/sound_files/University_of_Essex.m4a";

        assertThrows(UnsupportedAudioFileException.class, () -> Utilities.uploadSoundFileToAudioStream(pathToFile));
    }

    @Test
    void uploadToByteBufferReturnsAByteBuffer() throws IOException {
        String pathToFile = "src/main/resources/sound_files/University_of_Essex.wav";
        assertInstanceOf(ByteBuffer.class, Utilities.uploadSoundFileToByteBuffer(pathToFile));
    }

    @Test
    void uploadSoundFileWithoutSoundFileThrowsAnException() {
        String pathToFile = "src/main/resources/sound_files/text.txt";

        assertThrows(IOException.class, () -> Utilities.uploadSoundFileToByteBuffer(pathToFile));
    }

    @Test
    void playSoundThrowsExceptionWhenCalledOnATextFile() {
        assertThrows(IOException.class, () -> Utilities.playAudio(Utilities.uploadSoundFileToAudioStream("src/resources/text.txt")));
    }

    @Test
    void playSoundDoesNotThrowExceptionForAValidFile() {
        assertDoesNotThrow(() -> Utilities.playAudio(Utilities.uploadSoundFileToAudioStream("src/main/resources/sound_files/University_of_Essex.wav")));
    }

    @Test
    public void successfullyExportsFileFromAudioStreamToAWav() throws IOException, UnsupportedAudioFileException {
        assertTrue(Utilities.exportFileToWAV(Utilities.uploadSoundFileToAudioStream("src/main/resources/sound_files/Digi G'Alessio - Morning Jam With Darth Vader.wav"), "src/main/resources/sound_files/MethodWorks.wav"));
        File destroyFile = new File("src/main/resources/sound_files/MethodWorks.wav");
        destroyFile.delete();
    }

    @Test
    void pitchChangeCorrectlyModifiesTheSound() throws UnsupportedAudioFileException, IOException {
        String original = "src/main/resources/sound_files/CantinaBand60.wav";
        String edited = "src/main/resources/sound_files/TEST.wav";
        alterPitch(original, edited, 2721);

        File file = new File(edited);
        assertTrue(file.isFile());
        if (file.isFile()) file.delete();
    }

    @Test
    void pitchChangeThrowsExceptionForInvalidFile() {
        String original = "src/main/resources/sound_files/CantinaBand60.wa";
        String edited = "src/main/resources/sound_files/TEST.wav";

        assertThrows(IOException.class, () -> alterPitch(original, edited, 2721));
    }

    @Test
    void pitchChangeThrowsExceptionForUnsupportedAudioFile() {
        String original = "src/main/resources/sound_files/University_of_Essex.m4a";
        String edited = "src/main/resources/sound_files/University_of_Essex.m4a";

        assertThrows(UnsupportedAudioFileException.class, () -> alterPitch(original, edited, 2721));
    }

    @Test
    void testThatDeleteFilesDeleteTheFileSpecified() {
        String filePath1 = "src/main/resources/sound_files/test";

        // Create files
        try {
            Files.createFile(Paths.get(filePath1));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File file1 = new File(filePath1);

        Utilities.deleteFiles(file1);

        assertFalse(file1.exists());
    }

    @Test
    void testThatDeleteFilesDeleteTheFilesSpecified() {
        String filePath1 = "src/main/resources/sound_files/test";
        String filePath2 = "src/main/resources/sound_files/test1";

        // Create files
        try {
            Files.createFile(Paths.get(filePath1));
            Files.createFile(Paths.get(filePath2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File file1 = new File(filePath1);
        File file2 = new File(filePath2);

        Utilities.deleteFiles(file1, file2);

        assertFalse(file1.exists() && file2.exists());
    }
}