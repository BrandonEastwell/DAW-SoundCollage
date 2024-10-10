package com.sound_collage.controllers;

import com.sound_collage.Application;
import com.sound_collage.models.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

public class MainController implements Initializable {
    @FXML
    public Button buildBtn;
    @FXML
    public Button reverseSongBtn;
    public Button clearBtn;
    public Label songLabel;
    public Label songLabel2;
    public Slider songVolumeSlider2;
    public Slider songVolumeSlider1;
    @FXML
    public Slider tempoSlider;
    @FXML
    public Button randomBtn;
    public Canvas waveCanvas1;
    private File selectedFile;
    private AudioFormat audioFormat;
    @FXML
    public Button mainPlayBtn;
    @FXML
    public Button rebuildBtn;
    @FXML
    public ProgressBar progressbar;
    public Button exportBtn;
    @FXML
    public Slider pitchSlider1;
    @FXML
    public Slider pitchSlider2;
    public Button playBtnSong1;
    public Button playBtnSong2;
    @FXML
    private SplitPane fileSplitPane;
    @FXML
    private Canvas waveCanvasMain;
    @FXML
    private Canvas waveCanvas2;
    @FXML
    private Slider lowSlider1, midSlider1, highSlider1;
    @FXML
    private Slider lowSlider2, midSlider2, highSlider2;
    private SoundEqualizer soundEqualizer1;
    private SoundEqualizer soundEqualizer2;
    private final ObservableList<String> availableFiles = FXCollections.observableArrayList();
    private static final ObservableList<String> selectedFiles = FXCollections.observableArrayList();
    private final FileChooser fileChooser = new FileChooser();
    private static Sound[] soundFiles;
    private MediaPlayer mediaPlayer;
    private MediaPlayer[] mediaPlayers;
    @FXML
    private Button rewindBtn;
    private Timeline progressTimeline;
    private WaveVisualization waveMain;
    private AudioInputStream waveformBuildStream;
    private final File[] alteredFiles = new File[2];
    @FXML
    public Label header;
    public static byte[] mixedSound; //merged sounds byte array
    static AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
            44100.0f,
            16,
            2,
            4,
            44100.0f,
            false);
    private static final File buildPath = new File("./src/main/resources/sound_files/build.wav");

    public MainController() {
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ListView<String> fileList = new ListView<>(availableFiles);
        ListView<String> selectedFilesList = new ListView<>(selectedFiles);

        configureFileChooser(fileChooser);

        rebuildBtn.setOnAction(e -> rebuildProject());

        populateFileList();

        Button button = new Button("Add");
        button.setOnAction(e -> showAddFileDialog(Application.mainStage));
        fileSplitPane.getItems().addAll(
                new VBox(new Label("File Name"), fileList, button),
                new VBox(new Label("Selected Files"), selectedFilesList)
        );

        // Add mouse click listener to the fileList ListView
        fileList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                String selectedItem = fileList.getSelectionModel().getSelectedItem();
                if (selectedItem != null && !selectedFiles.contains(selectedItem)) {
                    selectedFiles.add(selectedItem);
                }
            }
        });

        // Mouse click to remove items from the list
        selectedFilesList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                String selectedItem = selectedFilesList.getSelectionModel().getSelectedItem();
                selectedFilesList.getItems().remove(selectedItem);
            }
        });


        tempoSlider.valueProperty().addListener((observable, oldValue, newValue) -> adjustTempo());

        pitchSlider1.valueProperty().addListener((observable, oldValue, newValue) -> adjustPitch(observable, 0));
        pitchSlider2.valueProperty().addListener((observable, oldValue, newValue) -> adjustPitch(observable, 1));

        lowSlider1.valueProperty().addListener((obs, oldVal, newVal) -> updateEqualizer1());
        midSlider1.valueProperty().addListener((obs, oldVal, newVal) -> updateEqualizer1());
        highSlider1.valueProperty().addListener((obs, oldVal, newVal) -> updateEqualizer1());

        lowSlider2.valueProperty().addListener((obs, oldVal, newVal) -> updateEqualizer2());
        midSlider2.valueProperty().addListener((obs, oldVal, newVal) -> updateEqualizer2());
        highSlider2.valueProperty().addListener((obs, oldVal, newVal) -> updateEqualizer2());


    }

    private void adjustPitch(ObservableValue<? extends Number> observable, int slider) {
        if (!selectedFiles.isEmpty()) {
            String filePath = selectedFiles.get(slider);

            try {
                Utilities.alterPitch(filePath,
                        ".\\src\\main\\resources\\sound_files\\" + "song_" + slider + "_altered.wav",
                        (double) observable.getValue());
                if (alteredFiles[slider].exists()) applyChanges();
            } catch (UnsupportedAudioFileException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void applyChanges() {
        addMediaPlayersForOriginalFiles();
    }

    @FXML
    private void buildProject() {
        buildProject(new ArrayList<>(selectedFiles));
    }

    private void buildProject(ArrayList<String> filePaths) {
        if (selectedFiles.size() == 2) {
            // After merging, show the player GUI
            createInitialSoundFile(filePaths);
            addMediaPlayersForOriginalFiles();
            updateSongLabels();
            displayPlayer();
        } else {
            // Alert the user that they need to select exactly two files
            showAlert();
        }
    }

    @FXML
    private void buildRandomProject() {
        System.out.println("There are " + availableFiles.size() + " available songs:");
        for (int i = 0; i < availableFiles.size(); i++) {
            System.out.println(availableFiles.get(i));
        }
        System.out.print("\n");
        Random random = new Random();

        int randomNumber1 = random.nextInt(availableFiles.size() - 1);
        int randomNumber2 = random.nextInt(availableFiles.size() - 1);

        while (randomNumber1 == randomNumber2) {
            randomNumber2 = random.nextInt(availableFiles.size() - 1);
        }
        List<String> files = new ArrayList<>(availableFiles);
        selectedFiles.clear();

        selectedFiles.add(files.get(randomNumber1));
        selectedFiles.add(files.get(randomNumber2));
        createInitialSoundFile(selectedFiles);
        displayPlayer();
        addMediaPlayersForOriginalFiles();
        updateSongLabels();
    }


    private void addMediaPlayersForOriginalFiles() {
        mediaPlayers = new MediaPlayer[selectedFiles.size()];
        for (int i = 0; i < selectedFiles.size(); i++) {
            soundFiles[i].update(alteredFiles[i].toPath().toString());
            if (i == 0) {
                soundFiles[i].wave = new WaveVisualization(waveCanvas1);
            } else {
                soundFiles[i].wave = new WaveVisualization(waveCanvas2);
            }
            String[] pathBreakdown = alteredFiles[i].getPath().split(Pattern.quote("\\"));
            System.out.println(Arrays.toString(alteredFiles[i].toPath().toString().split(Pattern.quote("/"))));
            System.out.println(new File("./src/main/resources/sound_files/" + pathBreakdown[pathBreakdown.length - 1]));
            mediaPlayers[i] = new MediaPlayer(new Media(alteredFiles[i].toURI().toString()));
            mediaPlayers[i].setOnReady(() -> {
                byte[] buffer = new byte[4096];
                Timeline progressTimeline = new Timeline(new KeyFrame(Duration.seconds(0.05), e -> {
                    int bytesRead;
                    if (mediaPlayers[0] != null && mediaPlayers[0].getCurrentTime() != null) {
                        try {
                            if ((soundFiles[0].audioInputStream.read(buffer, 0, buffer.length) != -1)) {
                                bytesRead = (soundFiles[0].audioInputStream.read(buffer, 0, buffer.length));
                                final byte[] audioBytes1 = Arrays.copyOf(buffer, bytesRead);
                                soundFiles[0].wave.updateWaveform(audioBytes1, soundFiles[0].format);
                            }
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }));
                progressTimeline.setCycleCount(Timeline.INDEFINITE);
            });

        }
        System.out.println(Arrays.toString(mediaPlayers));
    }

    public void displayPlayer() {
        // Playback controls
        rewindBtn.setOnAction(e -> rewindMediaPlayer());
        mainPlayBtn.setText("● Pause");
        mainPlayBtn.setOnAction(e -> playBuildFile());
    }

    private void populateFileList() {
        File dir = new File(Paths.get("./src/main/resources/sound_files/").toAbsolutePath().toString());
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".wav")) {
                    availableFiles.add(file.getAbsolutePath());
                }
            }
        }
    }

    private void showAddFileDialog(Stage primaryStage) {
        // Show the FileChooser and get the selected files
        List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
        if (files != null)
            for (File file : files) {
                if (!availableFiles.contains(file.getAbsolutePath()))
                    availableFiles.add(file.getAbsolutePath());
            }
    }

    private void configureFileChooser(FileChooser fileChooser) {
        fileChooser.setTitle("Select .wav files");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("WAV Files", "*.wav")
        );
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Merge Error");
        alert.setContentText("Please select exactly two files to merge.");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public void createInitialSoundFile(List<String> filePaths) {
        soundFiles = new Sound[filePaths.size()];
        for (int i = 0; i < filePaths.size(); i++) {
            Sound sound = new Sound(filePaths.get(i));
            try {
                soundFiles[i] = sound;
                alteredFiles[i] = new File(".\\src\\main\\resources\\sound_files\\" + "song_" + i + "_altered.wav");
                copyFileUsingStream(new File(filePaths.get(i)), alteredFiles[i]);
                System.out.println(alteredFiles[i].toPath());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        buildSoundFile();
    }

    public void buildSoundFile() {
        header.setText("building..");
        Sound sound1 = soundFiles[0];
        Sound sound2 = soundFiles[1];

        try {
            ByteBuffer mixBuffer;
            if (sound1.bytes.length > sound2.bytes.length) {
                mixBuffer = Export.mixFiles(sound2.byteBuffer, sound1.byteBuffer);
            } else {
                mixBuffer = Export.mixFiles(sound1.byteBuffer, sound2.byteBuffer);
            }
            mixedSound = mixBuffer.array();
            Export.saveToFile(buildPath, mixBuffer, format);
            if (LocalTime.now().getMinute() < 10) {
                header.setText("build successful! " + LocalTime.now().getHour() + ":0" + LocalTime.now().getMinute());
            } else {
                header.setText("build successful! " + LocalTime.now().getHour() + ":" + LocalTime.now().getMinute());
            }
            createMediaPlayer(); //builds the media player with new build file
        } catch (IOException e) {
            e.printStackTrace();
            header.setText("build could not complete! " + LocalTime.now());
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void rebuildProject() {
        ArrayList<String> soundFiles = new ArrayList<>();

        for (int i = 0; i < alteredFiles.length; i++) {
            if (alteredFiles[i].exists()) soundFiles.add(alteredFiles[i].getAbsolutePath());
            else soundFiles.add(selectedFiles.get(i));
        }

        buildProject(soundFiles);
    }

    @FXML
    private void clear() {
        // Clear the imported and selected files lists
        selectedFiles.clear();

        // Stop and reset the media player
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose(); // Release the media player resources
            mediaPlayer = null;
        }

        for (Sound sound : soundFiles) {
            waveMain.samples.clear();
            sound.wave.samples.clear();
        }
        clearWaveformCanvas();

        // Reset UI elements if necessary (e.g., reset the progress bar, status labels, etc.)
        progressbar.setProgress(0);
        header.setText("Project Reset");

        for (File file : alteredFiles) {
            if (file.exists()) file.delete();
        }
    }

    private void clearWaveformCanvas() {
        GraphicsContext gc1 = waveCanvasMain.getGraphicsContext2D();
        GraphicsContext gc2 = waveCanvas2.getGraphicsContext2D();
        GraphicsContext gc3 = waveCanvas1.getGraphicsContext2D();
        gc1.clearRect(0, 0, waveCanvasMain.getWidth(), waveCanvasMain.getHeight());
        gc2.clearRect(0, 0, waveCanvas2.getWidth(), waveCanvas2.getHeight());
        gc3.clearRect(0, 0, waveCanvas1.getWidth(), waveCanvas1.getHeight());
    }


    @FXML
    private void exportBuildFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Download");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("WAV Files", "*.wav"));
        File fileToSave = fileChooser.showSaveDialog(Application.mainStage);

        if (fileToSave != null) {
            // Only appends files for now
            try {
                Export.saveToFile(fileToSave, ByteBuffer.wrap(mixedSound), format);
                showAlert("Success", "File exported successfully.");
            } catch (IOException e) {
                showAlert("Failed", "File could not be exported");
            }
        }
    }

    private void rewindMediaPlayer() {
        // Optionally, reset the MediaPlayer to allow replaying
        mediaPlayer.seek(Duration.ZERO);
    }

    private void createMediaPlayer() throws UnsupportedAudioFileException, IOException {
        Media sound = new Media(buildPath.toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        waveMain = new WaveVisualization(waveCanvasMain);
        waveformBuildStream = AudioSystem.getAudioInputStream(new File(buildPath.toPath().toString()));

        byte[] buffer = new byte[4096];
        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = mediaPlayer.getMedia().getDuration();
            progressTimeline = new Timeline(new KeyFrame(Duration.seconds(0.05), e -> {
                if (mediaPlayer != null && mediaPlayer.getCurrentTime() != null) {
                    int bytesRead;
                    double progress = mediaPlayer.getCurrentTime().toMillis() / totalDuration.toMillis();
                    progressbar.setProgress(progress);
                    AudioFormat format = waveformBuildStream.getFormat();
                    try {
                        if ((waveformBuildStream.read(buffer, 0, buffer.length) != -1)) {
                            bytesRead = (waveformBuildStream.read(buffer, 0, buffer.length));
                            final byte[] audioBytes = Arrays.copyOf(buffer, bytesRead);
                            waveMain.updateWaveform(audioBytes, format);
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }));
            progressTimeline.setCycleCount(Timeline.INDEFINITE);

            mediaPlayer.play();
            progressTimeline.play();
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            progressbar.setProgress(1.0); // Set to 100% when the song ends
            if (progressTimeline != null) {
                progressTimeline.stop();
            }
        });
    }

    public void playBuildFile() {
        MediaPlayer.Status currentStatus = mediaPlayer.getStatus();
        if (currentStatus == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            progressTimeline.stop();
            mainPlayBtn.setText("► Play");
        } else if (currentStatus == MediaPlayer.Status.PAUSED) {
            mediaPlayer.play();
            progressTimeline.play();
            mainPlayBtn.setText("● Pause");
        }
    }

    @FXML
    public void playSong(Event event) {
        Button button = (Button) event.getSource();
        MediaPlayer.Status currentStatus = mediaPlayer.getStatus();
        if (currentStatus == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            mainPlayBtn.setText("► Play");
        }

        button.setText("● Pause");
        System.out.println(button.getId());
        if (button.getId().equals("playBtnSong1")) {
            System.out.println("1");
            System.out.println(selectedFiles.get(0));
            currentStatus = mediaPlayers[0].getStatus();
            playPause(currentStatus, button, 0);
        } else {
            System.out.println("2");
            System.out.println(selectedFiles.get(1));
            currentStatus = mediaPlayers[1].getStatus();
            playPause(currentStatus, button, 1);
        }
    }

    private void playPause(MediaPlayer.Status currentStatus, Button button, int btnID) {
        if (currentStatus == MediaPlayer.Status.PLAYING) {
            mediaPlayers[btnID].pause();
            button.setText("► Play");
        } else if (currentStatus == MediaPlayer.Status.PAUSED) {
            mediaPlayers[btnID].play();
            button.setText("● Pause");
        } else {
            mediaPlayers[btnID].play();
        }
    }

    private void updateSongLabels() {
        if (selectedFiles.size() >= 2) {
            String song1Name = extractSongName(selectedFiles.get(0));
            String song2Name = extractSongName(selectedFiles.get(1));

            songLabel.setText("Song 1: " + song1Name);
            songLabel2.setText("Song 2: " + song2Name);
        }
    }

    private String extractSongName(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }


    private void updateEqualizer1() {
        if (soundEqualizer1 != null) {
            float low = (float) lowSlider1.getValue();
            float mid = (float) midSlider1.getValue();
            float high = (float) highSlider1.getValue();

            soundEqualizer1.updateSettings(low, mid, high);
        }
    }

    private void updateEqualizer2() {
        if (soundEqualizer2 != null) {
            float low = (float) lowSlider2.getValue();
            float mid = (float) midSlider2.getValue();
            float high = (float) highSlider2.getValue();

            soundEqualizer2.updateSettings(low, mid, high);
        }
    }


    public void reverseSong() {
        // Check if the merged audio data exists
        if (mixedSound == null || mixedSound.length == 0) {
            showAlert("Reverse Error", "No merged audio to reverse.");
            return;
        }

        try {
            // Convert the byte array back to an AudioInputStream
            ByteArrayInputStream bais = new ByteArrayInputStream(mixedSound);
            AudioInputStream inputStream = new AudioInputStream(bais, format, mixedSound.length / format.getFrameSize());

            // Read the audio bytes from the stream
            byte[] audioBytes = inputStream.readAllBytes();
            bais.close();
            inputStream.close();

            // Reverse the audio bytes
            reverseBytes(audioBytes, format);

            // Write the reversed audio back to the build.wav file
            ByteArrayInputStream reversedBais = new ByteArrayInputStream(audioBytes);
            AudioInputStream reversedStream = new AudioInputStream(reversedBais, format, audioBytes.length / format.getFrameSize());
            AudioSystem.write(reversedStream, AudioFileFormat.Type.WAVE, buildPath);
            reversedStream.close();

            // Update mixedSound with reversed bytes
            mixedSound = audioBytes;

            // Stop the current media player and play the reversed audio
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }


            Media reversedMedia = new Media(buildPath.toURI().toString());
            mediaPlayer = new MediaPlayer(reversedMedia);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Reverse Error", "Could not reverse the merged audio.");
        }
    }

    private void reverseBytes(byte[] audioBytes, AudioFormat format) {
        int bytesPerFrame = format.getFrameSize();
        for (int i = 0; i < audioBytes.length / 2; i += bytesPerFrame) {
            for (int j = 0; j < bytesPerFrame; j++) {
                byte temp = audioBytes[i + j];
                audioBytes[i + j] = audioBytes[audioBytes.length - i - bytesPerFrame + j];
                audioBytes[audioBytes.length - i - bytesPerFrame + j] = temp;
            }
        }
    }

    @FXML
    private void handleReverseSong() {
        reverseSong();
    }


    private byte[] readAudioFile(String filePath) {
        try {
            File file = new File(filePath);
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            byte[] bytes = stream.readAllBytes();
            stream.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @FXML
    private void adjustTempo() {
        if (selectedFile != null) {
            float tempoFactor = (float) tempoSlider.getValue();
            byte[] audioBytes = readAudioFile(selectedFile.getAbsolutePath());
            byte[] adjustedBytes = adjustTempo(audioBytes, tempoFactor);
            String outputPath = "adjusted_" + selectedFile.getName();
            writeAudioFile(adjustedBytes, outputPath, audioFormat);
            // Additional code to play the adjusted file if needed
        }
    }

    public byte[] adjustTempo(byte[] audioBytes, float tempoFactor) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Assuming 16-bit samples (2 bytes per sample)
        int bytesPerSample = 2;

        if (tempoFactor == 1.0) {
            return audioBytes;
        } else if (tempoFactor > 1.0) {
            // Speed up by skipping samples
            for (int i = 0; i < audioBytes.length; i += bytesPerSample) {
                if (i % tempoFactor < 1) {
                    outputStream.write(audioBytes, i, bytesPerSample);
                }
            }
        } else {
            // Slow down by duplicating samples
            for (int i = 0; i < audioBytes.length; i += bytesPerSample) {
                outputStream.write(audioBytes, i, bytesPerSample);
                if (i % (1 / tempoFactor) < 1) {
                    outputStream.write(audioBytes, i, bytesPerSample);
                }
            }
        }

        return outputStream.toByteArray();
    }

    public void writeAudioFile(byte[] audioBytes, String outputPath, AudioFormat format) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            AudioInputStream ais = new AudioInputStream(bais, format, audioBytes.length / format.getFrameSize());
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(outputPath));
            ais.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}