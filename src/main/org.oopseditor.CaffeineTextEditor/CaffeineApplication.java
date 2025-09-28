import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * The entry point for the CaffeineTextEditor JavaFX application.
 * Sets up the main window, menu bar, and text editing area.
 */
public class CaffeineApplication extends Application {

    private TextArea mainTextArea;       // The central component for text editing
    private File currentFile;            // Tracks the currently opened file
    private Stage primaryStage;          // Reference to the main window

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        // Initialize the main text area
        mainTextArea = new TextArea();
        mainTextArea.setWrapText(true);

        // 1. Create the Menu Bar and attach actions
        MenuBar menuBar = createMenuBar();

        // 2. Create the Main Layout Container
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(mainTextArea);

        // 3. Create the Scene and Stage
        Scene scene = new Scene(root, 1000, 700);

        // Removed potential NullPointerException by commenting out CSS load for now.
        // scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setTitle("Caffeine Text Editor - Untitled");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Helper method to create and populate the MenuBar with Menus and MenuItems.
     * Actions are attached here.
     * @return The configured MenuBar object.
     */
    private MenuBar createMenuBar() {
        // --- File Menu ---
        Menu fileMenu = new Menu("File");

        MenuItem newItem = new MenuItem("New");
        // Action: Use the stored primaryStage reference
        newItem.setOnAction(e -> newFile(primaryStage));

        MenuItem openItem = new MenuItem("Open...");
        openItem.setOnAction(e -> openFile(primaryStage));

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> saveFile(primaryStage));

        MenuItem saveAsItem = new MenuItem("Save As...");
        saveAsItem.setOnAction(e -> saveFileAs(primaryStage));

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> javafx.application.Platform.exit());

        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, new SeparatorMenuItem(), exitItem);

        // --- History Menu (for JDBC integration) ---
        Menu historyMenu = new Menu("History");
        MenuItem viewHistoryItem = new MenuItem("View Recent Files...");
        historyMenu.getItems().add(viewHistoryItem);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, historyMenu);

        return menuBar;
    }

    /**
     * Clears the editor and prepares for a new file.
     */
    private void newFile(Stage stage) {
        mainTextArea.clear();
        currentFile = null;
        stage.setTitle("Caffeine Text Editor - Untitled");
    }

    /**
     * Opens a file chooser dialog and loads the selected file's content.
     */
    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                String content = java.nio.file.Files.readString(selectedFile.toPath());

                mainTextArea.setText(content);
                currentFile = selectedFile;
                stage.setTitle("Caffeine Text Editor - " + selectedFile.getName());

                // JDBC TODO: Record this 'open' action in history here!

            } catch (java.io.IOException e) {
                showAlert("Error", "Could not read file: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Saves the current content to the current file. Prompts for 'Save As' if no file is open.
     */
    private void saveFile(Stage stage) {
        if (currentFile != null) {
            saveContentToFile(currentFile, stage);
        } else {
            saveFileAs(stage);
        }
    }

    /**
     * Opens a file chooser dialog to save the content to a new or selected file.
     */
    private void saveFileAs(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Text File");

        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
            }
            saveContentToFile(selectedFile, stage);
        }
    }

    /**
     * Core logic to write the TextArea content to the given File object.
     */
    private void saveContentToFile(File file, Stage stage) {
        try {
            java.nio.file.Files.writeString(file.toPath(), mainTextArea.getText());

            currentFile = file;
            stage.setTitle("Caffeine Text Editor - " + file.getName());

            // JDBC TODO: Record this 'save' action in history here!

            showAlert("Success", "File saved successfully!", Alert.AlertType.INFORMATION);

        } catch (java.io.IOException e) {
            showAlert("Error", "Could not save file: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Utility method to display a simple JavaFX alert dialog.
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
