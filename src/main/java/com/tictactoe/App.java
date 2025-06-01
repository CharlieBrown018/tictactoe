package com.tictactoe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static StackPane mainContainer;
    private static Stage mainStage;

    @Override
    public void start(Stage stage) {
        try {
            mainStage = stage;
            mainContainer = new StackPane();
            Scene scene = new Scene(mainContainer, 800, 600);
            stage.setTitle("Tic Tac Toe");
            stage.setScene(scene);
            
            // Load initial menu view
            loadView("/com/tictactoe/fxml/menu.fxml");
            
            stage.show();
            logger.info("Application started successfully");
        } catch (Exception e) {
            logger.error("Error starting application", e);
        }
    }

    @Override
    public void stop() {
        logger.info("Shutting down application");
        com.tictactoe.config.DatabaseConfig.closeAllConnections();
        logger.info("Application stopped successfully");
    }

    public static void loadView(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxmlPath));
            Node view = fxmlLoader.load();
            mainContainer.getChildren().setAll(view);
            logger.info("Successfully loaded view: {}", fxmlPath);
        } catch (IOException e) {
            logger.error("Error loading view: {}", fxmlPath, e);
        }
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        launch();
    }
}