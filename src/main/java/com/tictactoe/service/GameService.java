package com.tictactoe.service;

import com.tictactoe.model.entity.GameEntity;
import com.tictactoe.model.entity.PlayerEntity;
import com.tictactoe.model.enums.GameMode;
import com.tictactoe.repository.GameRepository;
import com.tictactoe.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final String[][] board;
    private PlayerEntity player1;
    private PlayerEntity player2;
    private boolean isGameOver;
    private int moveCount;
    private List<String> moves;
    private GameMode gameMode;
    private Set<String> clearedPositions; // Track cleared positions

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.board = new String[3][3];
        this.moves = new ArrayList<>();
        this.gameMode = GameMode.CLASSIC; // Default mode
        this.clearedPositions = new HashSet<>();
        resetGame();
    }

    public PlayerEntity startNewGame(String player1Name, String player2Name, GameMode mode) {
        this.gameMode = mode;
        player1 = playerRepository.findByName(player1Name)
                .orElseGet(() -> {
                    PlayerEntity newPlayer = new PlayerEntity();
                    newPlayer.setName(player1Name);
                    return playerRepository.save(newPlayer);
                });

        player2 = playerRepository.findByName(player2Name)
                .orElseGet(() -> {
                    PlayerEntity newPlayer = new PlayerEntity();
                    newPlayer.setName(player2Name);
                    return playerRepository.save(newPlayer);
                });

        resetGame();
        logger.info("Started new {} game between {} and {}", mode, player1Name, player2Name);
        return player1;
    }

    public void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
            }
        }
        isGameOver = false;
        moveCount = 0;
        moves.clear();
        clearedPositions.clear();
    }

    public boolean makeMove(int row, int col) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3 || !board[row][col].isEmpty() || isGameOver) {
            return false;
        }
        String symbol = getCurrentPlayerSymbol();
        board[row][col] = symbol;
        moveCount++;
        moves.add(String.format("%d,%d,%s", row, col, symbol));
        // Remove this position from cleared positions if it was previously cleared
        clearedPositions.remove(row + "," + col);
        if (checkWinForSymbol(symbol)) {
            isGameOver = true;
            // Since getCurrentPlayerSymbol() returns X for even moveCount and O for odd
            // moveCount
            // and moveCount has been incremented, we need to check the previous state
            // If moveCount is now odd, X just played (player1)
            // If moveCount is now even, O just played (player2)
            PlayerEntity winner = ((moveCount - 1) % 2 == 0) ? player1 : player2;
            PlayerEntity loser = ((moveCount - 1) % 2 == 0) ? player2 : player1;
            updateStats(winner, loser, false);
            saveGameResult(winner.getId(), false);
            return true;
        }

        if (isBoardFull()) {
            if (gameMode == GameMode.CLASSIC) {
                isGameOver = true;
                updateStats(player1, player2, true);
                saveGameResult(null, true);
            }
            return true;
        }

        return false;
    }

    public void clearTile(int row, int col) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3) {
            board[row][col] = "";
            moves.add(String.format("%d,%d,CLEAR", row, col));
            clearedPositions.add(row + "," + col);
            logger.debug("Cleared tile at position ({}, {})", row, col);
        }
    }

    public boolean hasWinner() {
        return checkWinForSymbol("X") || checkWinForSymbol("O");
    }

    private boolean checkWinForSymbol(String symbol) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            boolean rowWin = true;
            for (int j = 0; j < 3; j++) {
                if (isPositionCleared(i, j) || !board[i][j].equals(symbol)) {
                    rowWin = false;
                    break;
                }
            }
            if (rowWin)
                return true;
        }

        // Check columns
        for (int j = 0; j < 3; j++) {
            boolean colWin = true;
            for (int i = 0; i < 3; i++) {
                if (isPositionCleared(i, j) || !board[i][j].equals(symbol)) {
                    colWin = false;
                    break;
                }
            }
            if (colWin)
                return true;
        }

        // Check main diagonal
        boolean diagWin = true;
        for (int i = 0; i < 3; i++) {
            if (isPositionCleared(i, i) || !board[i][i].equals(symbol)) {
                diagWin = false;
                break;
            }
        }
        if (diagWin)
            return true;

        // Check other diagonal
        diagWin = true;
        for (int i = 0; i < 3; i++) {
            if (isPositionCleared(i, 2 - i) || !board[i][2 - i].equals(symbol)) {
                diagWin = false;
                break;
            }
        }
        return diagWin;
    }

    private boolean isPositionCleared(int row, int col) {
        return clearedPositions.contains(row + "," + col);
    }

    private void updateStats(PlayerEntity winner, PlayerEntity loser, boolean isDraw) {
        if (isDraw) {
            player1.setDraws(player1.getDraws() + 1);
            player2.setDraws(player2.getDraws() + 1);
        } else {
            // First update the actual winner and loser stats
            int currentWins = winner.getWins();
            int currentLosses = loser.getLosses();
            winner.setWins(currentWins + 1);
            loser.setLosses(currentLosses + 1);
            // Now update the player1/player2 references to match
            if (winner.getId().equals(player1.getId())) {
                player1 = winner; // Update player1 reference with new stats
                player2 = loser; // Update player2 reference with new stats
            } else {
                player2 = winner; // Update player2 reference with new stats
                player1 = loser; // Update player1 reference with new stats
            }
        }
        // Save the updated stats to the database
        playerRepository.save(player1);
        playerRepository.save(player2);
    }

    private void saveGameResult(Long winnerId, boolean isDraw) {
        try {
            GameEntity game = new GameEntity();
            game.setPlayer1Id(player1.getId());
            game.setPlayer2Id(player2.getId());
            game.setWinnerId(winnerId);
            game.setDraw(isDraw);
            game.setGameMode(gameMode);
            game.setMoves(String.join(";", moves));
            game.setPlayedAt(LocalDateTime.now());
            gameRepository.save(game);
            logger.info("Game result saved successfully");
        } catch (Exception e) {
            logger.error("Failed to save game result", e);
        }
    }

    public String getGameResult() {
        if (hasWinner()) {
            return getCurrentPlayerSymbol();
        }
        if (isBoardFull() && gameMode == GameMode.CLASSIC) {
            return "DRAW";
        }
        return "";
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getCurrentPlayerSymbol() {
        return (moveCount % 2 == 0) ? "O" : "X"; // Reversed to make player1 (first player) X
    }

    public PlayerEntity getPlayer1() {
        return player1;
    }

    public PlayerEntity getPlayer2() {
        return player2;
    }

    public String getCell(int row, int col) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3) {
            if (isPositionCleared(row, col)) {
                return "";
            }
            return board[row][col];
        }
        return "";
    }

    private boolean checkWin(int row, int col) {
        String symbol = board[row][col];
        return checkWinForSymbol(symbol);
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
}