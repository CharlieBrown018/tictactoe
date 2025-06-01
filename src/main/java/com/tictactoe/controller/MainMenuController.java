package com.tictactoe.controller;

import com.tictactoe.App;
import com.tictactoe.animation.BackgroundAnimator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainMenuController {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuController.class);

    @FXML private Text versionText;
    @FXML private Circle circle1;
    @FXML private Circle circle2;
    @FXML private Polygon triangle1;
    @FXML private Rectangle rectangle1;
    private BackgroundAnimator backgroundAnimator;

    @FXML
    public void initialize() {
        logger.info("Initializing main menu");
        backgroundAnimator = new BackgroundAnimator(circle1, circle2, triangle1, rectangle1);
        versionText.setText("Version 1.0");
    }

    @FXML
    private void handleNewGame() {
        try {
            logger.info("Loading game view");
            App.loadView("/com/tictactoe/fxml/game.fxml");
        } catch (Exception e) {
            logger.error("Error loading game view", e);
            showError("Error", "Could not load game view");
        }
    }

    @FXML
    private void handleLeaderboard() {
        try {
            logger.info("Loading leaderboard view");
            App.loadView("/com/tictactoe/fxml/leaderboard.fxml");
        } catch (Exception e) {
            logger.error("Error loading leaderboard view", e);
            showError("Error", "Could not load leaderboard");
        }
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Tic Tac Toe");
        alert.setHeaderText("Tic Tac Toe Game");
        alert.setContentText("A modern implementation of the classic Tic Tac Toe game.\n\n" +
                           "Features:\n" +
                           "- Player statistics tracking\n" +
                           "- Leaderboard\n" +
                           "- Game history\n" +
                           "- Modern dark theme UI\n\n" +
                           "Version 1.0");
        
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm()
        );
        
        alert.showAndWait();
    }

    @FXML
    private void handleQuit() {
        logger.info("Application shutting down");
        Platform.exit();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm()
        );
        alert.showAndWait();
    }
} 