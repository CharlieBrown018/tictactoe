package com.tictactoe.model.entity;

import java.time.LocalDateTime;

public class PlayerEntity {
    private Long id;
    private String name;
    private int wins;
    private int losses;
    private int draws;
    private LocalDateTime createdAt;

    public PlayerEntity() {
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
        this.createdAt = LocalDateTime.now();
    }

    public PlayerEntity(String name) {
        this();
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void incrementWins() {
        this.wins++;
    }

    public void incrementLosses() {
        this.losses++;
    }

    public void incrementDraws() {
        this.draws++;
    }

    public int getTotalGames() {
        return wins + losses + draws;
    }

    public double getWinRate() {
        int totalGames = getTotalGames();
        return totalGames > 0 ? (double) wins / totalGames : 0.0;
    }
} 