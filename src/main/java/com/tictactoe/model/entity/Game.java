package com.tictactoe.model.entity;

import com.tictactoe.model.enums.GameState;
import com.tictactoe.model.enums.Player;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Player[][] board;
    private Player currentPlayer;
    private GameState state;
    private final List<Integer> moves;

    public Game() {
        board = new Player[3][3];
        currentPlayer = Player.X;
        state = GameState.IN_PROGRESS;
        moves = new ArrayList<>();
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = Player.NONE;
            }
        }
    }

    public boolean makeMove(int row, int col) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3 || board[row][col] != Player.NONE || state != GameState.IN_PROGRESS) {
            return false;
        }

        board[row][col] = currentPlayer;
        moves.add(row * 3 + col);
        
        updateGameState();
        if (state == GameState.IN_PROGRESS) {
            currentPlayer = (currentPlayer == Player.X) ? Player.O : Player.X;
        }
        
        return true;
    }

    private void updateGameState() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (checkLine(board[i][0], board[i][1], board[i][2])) {
                state = (board[i][0] == Player.X) ? GameState.X_WON : GameState.O_WON;
                return;
            }
        }

        // Check columns
        for (int j = 0; j < 3; j++) {
            if (checkLine(board[0][j], board[1][j], board[2][j])) {
                state = (board[0][j] == Player.X) ? GameState.X_WON : GameState.O_WON;
                return;
            }
        }

        // Check diagonals
        if (checkLine(board[0][0], board[1][1], board[2][2]) || 
            checkLine(board[0][2], board[1][1], board[2][0])) {
            state = (board[1][1] == Player.X) ? GameState.X_WON : GameState.O_WON;
            return;
        }

        // Check for draw
        boolean isDraw = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == Player.NONE) {
                    isDraw = false;
                    break;
                }
            }
        }
        
        if (isDraw) {
            state = GameState.DRAW;
        }
    }

    private boolean checkLine(Player a, Player b, Player c) {
        return a != Player.NONE && a == b && b == c;
    }

    public Player getCell(int row, int col) {
        return board[row][col];
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getState() {
        return state;
    }

    public List<Integer> getMoves() {
        return new ArrayList<>(moves);
    }

    public void reset() {
        initializeBoard();
        currentPlayer = Player.X;
        state = GameState.IN_PROGRESS;
        moves.clear();
    }
} 