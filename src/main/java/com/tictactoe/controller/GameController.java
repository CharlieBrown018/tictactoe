package com.tictactoe.controller;

import com.tictactoe.App;
import com.tictactoe.model.entity.PlayerEntity;
import com.tictactoe.model.enums.GameMode;
import com.tictactoe.service.GameService;
import com.tictactoe.repository.GameRepository;
import com.tictactoe.repository.PlayerRepository;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import com.tictactoe.animation.ParticleEffect;
import javafx.scene.paint.Color;
import javafx.animation.PauseTransition;
import javafx.scene.effect.Glow;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.animation.Timeline;
import javafx.scene.shape.Shape;

import java.util.*;

public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private GameService gameService;
    private GameMode gameMode;
    private Timeline backgroundAnimator;
    private List<Shape> backgroundShapes;
    private Random random;

    @FXML private GridPane gameBoard;
    @FXML private Label player1Name;
    @FXML private Label player2Name;
    @FXML private Label player1Stats;
    @FXML private Label player2Stats;
    @FXML private Label gameStatus;
    @FXML private Label gameModeLabel;
    @FXML private Circle circle1;
    @FXML private Circle circle2;
    @FXML private Polygon triangle1;
    @FXML private Rectangle rectangle1;

    private PlayerEntity currentPlayer;
    private PlayerEntity player1;
    private PlayerEntity player2;

    @FXML
    public void initialize() {
        logger.info("Initializing game controller");
        gameService = new GameService(new GameRepository(), new PlayerRepository());
        initializeBackground();
        showGameModeDialog();
    }

    private void initializeBackground() {
        random = new Random();
        backgroundShapes = Arrays.asList(circle1, circle2, triangle1, rectangle1);
        
        // Initialize triangle points
        triangle1.getPoints().addAll(
            0.0, 0.0,
            20.0, 0.0,
            10.0, 17.32
        );
    }

    private void showGameModeDialog() {
        Dialog<GameMode> dialog = new Dialog<>();
        dialog.setTitle("Select Game Mode");
        dialog.setHeaderText("Choose Your Game Mode");

        ButtonType playButton = new ButtonType("Play", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(playButton, cancelButton);

        HBox content = new HBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        GameMode[] selectedMode = new GameMode[1];
        selectedMode[0] = GameMode.CLASSIC; // Default selection

        // Classic mode card
        VBox classicCard = createGameModeCard(
            "Classic",
            "Traditional Tic Tac Toe\nFirst to get 3 in a row wins!",
            GameMode.CLASSIC,
            selectedMode
        );
        classicCard.getStyleClass().add("selected"); // Default selection

        // Endless mode card
        VBox endlessCard = createGameModeCard(
            "Endless",
            "Keep playing until someone wins!\nTiles clear randomly when board is full.",
            GameMode.ENDLESS,
            selectedMode
        );

        content.getChildren().addAll(classicCard, endlessCard);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == playButton) {
                return selectedMode[0];
            }
            return null;
        });

        Optional<GameMode> result = dialog.showAndWait();
        result.ifPresentOrElse(mode -> {
            gameMode = mode; // Set the local gameMode field
            gameService.setGameMode(mode);
            updateGameModeLabel();
            showPlayerDialog();
        }, () -> App.loadView("/com/tictactoe/fxml/menu.fxml"));
    }

    private VBox createGameModeCard(String title, String description, GameMode mode, GameMode[] selectedMode) {
        VBox card = new VBox(10);
        card.getStyleClass().add("game-mode-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("game-mode-title");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("game-mode-description");

        card.getChildren().addAll(titleLabel, descLabel);

        // Make the entire card clickable
        card.setOnMouseClicked(event -> {
            // Remove selection from all cards in the parent container
            if (card.getParent() instanceof HBox) {
                ((HBox) card.getParent()).getChildren().forEach(node -> {
                    if (node instanceof VBox) {
                        node.getStyleClass().remove("selected");
                    }
                });
            }
            
            // Add selection to this card
            card.getStyleClass().add("selected");
            selectedMode[0] = mode;

            // Add selection animation
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        // Add hover effect
        card.setOnMouseEntered(event -> {
            if (!card.getStyleClass().contains("selected")) {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                st.setToX(1.05);
                st.setToY(1.05);
                st.play();
            }
        });

        card.setOnMouseExited(event -> {
            if (!card.getStyleClass().contains("selected")) {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            }
        });

        return card;
    }

    @FXML
    private void handleMove(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        
        if (clickedButton.getText().isEmpty()) {
            String symbol = currentPlayer == player1 ? "X" : "O";
            
            // Add impact animation
            ScaleTransition st = new ScaleTransition(Duration.millis(150), clickedButton);
            st.setFromX(0.8);
            st.setFromY(0.8);
            st.setToX(1.0);
            st.setToY(1.0);
            
            int row = GridPane.getRowIndex(clickedButton);
            int col = GridPane.getColumnIndex(clickedButton);
            
            logger.info("Player {} made move at position ({}, {})", currentPlayer.getName(), row, col);
            
            try {
                boolean isGameComplete = gameService.makeMove(row, col);
                
                // Set the text and style after confirming the move is valid
                clickedButton.setText(symbol);
                clickedButton.getStyleClass().removeAll("x", "o");
                clickedButton.getStyleClass().add(symbol.toLowerCase());
                st.play();

                if (isGameComplete) {
                    if (gameMode == GameMode.ENDLESS && !gameService.hasWinner()) {
                        // In endless mode, only clear tiles if there's no winner
                        clearRandomTiles(3);
                        switchPlayer();
                        updateGameStatus();
                    } else {
                        handleGameOver();
                    }
                } else {
                    switchPlayer();
                    updateGameStatus();
                }
            } catch (RuntimeException e) {
                logger.error("Error processing move", e);
                showError("Error", "Could not process move");
            }
        }
    }

    private void clearRandomTiles(int count) {
        List<Button> occupiedButtons = new ArrayList<>();
        gameBoard.getChildren().stream()
            .filter(node -> node instanceof Button)
            .map(node -> (Button) node)
            .filter(button -> !button.getText().isEmpty())
            .forEach(occupiedButtons::add);

        // Shuffle the list and take first 'count' buttons to clear
        Collections.shuffle(occupiedButtons, random);
        
        // Animate clearing of tiles
        for (int i = 0; i < Math.min(count, occupiedButtons.size()); i++) {
            Button button = occupiedButtons.get(i);
            
            // Fade out animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), button);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            
            // Clear the tile after fade out
            fadeOut.setOnFinished(e -> {
                button.setText("");
                button.setOpacity(1.0); // Reset opacity
                button.getStyleClass().removeAll("x", "o", "winning-move");
                
                int row = GridPane.getRowIndex(button);
                int col = GridPane.getColumnIndex(button);
                gameService.clearTile(row, col);
            });
            
            fadeOut.play();
        }
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private void updateGameStatus() {
        gameStatus.setText(currentPlayer.getName() + "'s turn");
    }

    private void handleGameOver() {
        String result = gameService.getGameResult();
        if (result.equals("DRAW")) {
            logger.info("Game ended in a draw between {} and {}", player1.getName(), player2.getName());
            showGameOverDialog("Game Over", "It's a draw!");
        } else if (result.equals("X") || result.equals("O")) {
            PlayerEntity winner = result.equals("X") ? player1 : player2;
            PlayerEntity loser = result.equals("X") ? player2 : player1;
            logger.info("Game ended. Winner: {}, Loser: {}", winner.getName(), loser.getName());
            highlightWinningCombination();
            showGameOverDialog("Game Over", winner.getName() + " wins!");
        }
        updateStatsDisplay();
    }

    private void updateStatsDisplay() {
        // Refresh player entities to get updated stats
        player1 = gameService.getPlayer1();
        player2 = gameService.getPlayer2();
        
        player1Stats.setText(String.format("W: %d L: %d D: %d", 
            player1.getWins(), player1.getLosses(), player1.getDraws()));
        player2Stats.setText(String.format("W: %d L: %d D: %d", 
            player2.getWins(), player2.getLosses(), player2.getDraws()));
    }

    @FXML
    private void startNewGame() {
        gameBoard.getChildren().stream()
            .filter(node -> node instanceof Button)
            .map(node -> (Button) node)
            .forEach(button -> {
                button.setText("");
                button.getStyleClass().removeAll("x", "o", "winning-move");
                button.setOpacity(1.0);
                button.setEffect(null);
            });
        
        gameService.resetGame();
        currentPlayer = player1;
        updateGameStatus();
    }

    @FXML
    private void backToMenu() {
        try {
            logger.info("Navigating back to main menu");
            App.loadView("/com/tictactoe/fxml/menu.fxml");
        } catch (Exception e) {
            logger.error("Error loading menu view", e);
            showError("Error", "Could not load menu");
        }
    }

    @FXML
    private void showLeaderboard() {
        try {
            logger.info("Loading leaderboard view");
            App.loadView("/com/tictactoe/fxml/leaderboard.fxml");
        } catch (Exception e) {
            logger.error("Error loading leaderboard view", e);
            showError("Error", "Could not load leaderboard");
        }
    }

    private void showPlayerDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Player Names");
        String modeName = gameService.getGameMode().name();
        dialog.setHeaderText("Enter player names for " + 
            modeName.charAt(0) + modeName.substring(1).toLowerCase() + " Mode");

        ButtonType confirmButtonType = new ButtonType("Start Game", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField player1Field = new TextField();
        TextField player2Field = new TextField();
        
        grid.add(new Label("Player 1 (X):"), 0, 0);
        grid.add(player1Field, 1, 0);
        grid.add(new Label("Player 2 (O):"), 0, 1);
        grid.add(player2Field, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return player1Field.getText() + "," + player2Field.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresentOrElse(names -> {
            String[] playerNames = names.split(",");
            if (playerNames.length == 2) {
                player1 = gameService.startNewGame(playerNames[0], playerNames[1], gameMode);
                player2 = gameService.getPlayer2();
                currentPlayer = player1;
                
                player1Name.setText(player1.getName() + " (X)");
                player2Name.setText(player2.getName() + " (O)");
                updateStatsDisplay();
                updateGameStatus();
            }
        }, () -> {
            backToMenu();
        });
    }

    private void showGameOverDialog(String title, String content) {
        // Add a small delay to ensure animations are complete
        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(e -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(content);
                alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
                
                ButtonType newGameButton = new ButtonType("New Game");
                ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
                
                alert.getButtonTypes().setAll(newGameButton, closeButton);
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == newGameButton) {
                    startNewGame();
                }
            });
        });
        delay.play();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
        alert.showAndWait();
    }

    private void highlightWinningCombination() {
        // Get winning combination from game service
        List<Button> winningButtons = new ArrayList<>();
        
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (!gameService.getCell(i, 0).isEmpty() && 
                gameService.getCell(i, 0).equals(gameService.getCell(i, 1)) && 
                gameService.getCell(i, 1).equals(gameService.getCell(i, 2))) {
                for (int j = 0; j < 3; j++) {
                    Button btn = getButtonAt(i, j);
                    if (btn != null) {
                        winningButtons.add(btn);
                    }
                }
                break;
            }
        }

        // Check columns
        if (winningButtons.isEmpty()) {
            for (int j = 0; j < 3; j++) {
                if (!gameService.getCell(0, j).isEmpty() && 
                    gameService.getCell(0, j).equals(gameService.getCell(1, j)) && 
                    gameService.getCell(1, j).equals(gameService.getCell(2, j))) {
                    for (int i = 0; i < 3; i++) {
                        Button btn = getButtonAt(i, j);
                        if (btn != null) {
                            winningButtons.add(btn);
                        }
                    }
                    break;
                }
            }
        }

        // Check diagonals
        if (winningButtons.isEmpty()) {
            if (!gameService.getCell(0, 0).isEmpty() && 
                gameService.getCell(0, 0).equals(gameService.getCell(1, 1)) && 
                gameService.getCell(1, 1).equals(gameService.getCell(2, 2))) {
                addButtonIfNotNull(winningButtons, getButtonAt(0, 0));
                addButtonIfNotNull(winningButtons, getButtonAt(1, 1));
                addButtonIfNotNull(winningButtons, getButtonAt(2, 2));
            } else if (!gameService.getCell(0, 2).isEmpty() && 
                      gameService.getCell(0, 2).equals(gameService.getCell(1, 1)) && 
                      gameService.getCell(1, 1).equals(gameService.getCell(2, 0))) {
                addButtonIfNotNull(winningButtons, getButtonAt(0, 2));
                addButtonIfNotNull(winningButtons, getButtonAt(1, 1));
                addButtonIfNotNull(winningButtons, getButtonAt(2, 0));
            }
        }

        // Apply winning animation to the buttons only if we found a winning combination
        if (!winningButtons.isEmpty()) {
            for (Button button : winningButtons) {
                button.getStyleClass().add("winning-move");
            }
        }
    }

    private void addButtonIfNotNull(List<Button> buttons, Button button) {
        if (button != null) {
            buttons.add(button);
        }
    }

    private Button getButtonAt(int row, int col) {
        for (javafx.scene.Node node : gameBoard.getChildren()) {
            if (node instanceof Button && 
                GridPane.getRowIndex(node) == row && 
                GridPane.getColumnIndex(node) == col) {
                return (Button) node;
            }
        }
        return null;
    }

    private void updateGameModeLabel() {
        String modeName = gameService.getGameMode().name();
        String displayText = modeName.charAt(0) + modeName.substring(1).toLowerCase() + " Mode";
        gameModeLabel.setText(displayText);
    }
} 