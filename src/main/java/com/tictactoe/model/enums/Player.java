package com.tictactoe.model.enums;

public enum Player {
    X("X"),
    O("O"),
    NONE("");

    private final String symbol;

    Player(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
} 