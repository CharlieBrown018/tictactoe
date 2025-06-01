package com.tictactoe.controller;

import com.tictactoe.App;
import com.tictactoe.model.entity.PlayerEntity;
import com.tictactoe.repository.PlayerRepository;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleDoubleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LeaderboardController {
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardController.class);
    private final PlayerRepository playerRepository;

    @FXML
    private TableView<PlayerEntity> leaderboardTable;
    @FXML
    private TableColumn<PlayerEntity, String> nameColumn;
    @FXML
    private TableColumn<PlayerEntity, Integer> winsColumn;
    @FXML
    private TableColumn<PlayerEntity, Integer> lossesColumn;
    @FXML
    private TableColumn<PlayerEntity, Integer> drawsColumn;
    @FXML
    private TableColumn<PlayerEntity, Double> winRateColumn;

    public LeaderboardController() {
        this.playerRepository = new PlayerRepository();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadLeaderboard();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
        lossesColumn.setCellValueFactory(new PropertyValueFactory<>("losses"));
        drawsColumn.setCellValueFactory(new PropertyValueFactory<>("draws"));
        winRateColumn.setCellValueFactory(cellData -> {
            PlayerEntity player = cellData.getValue();
            int totalGames = player.getWins() + player.getLosses() + player.getDraws();
            double winRate = totalGames > 0 ? (double) player.getWins() / totalGames * 100 : 0.0;
            return new SimpleDoubleProperty(winRate).asObject();
        });
    }

    private void loadLeaderboard() {
        try {
            List<PlayerEntity> topPlayers = playerRepository.findTopPlayers(10);
            leaderboardTable.setItems(FXCollections.observableArrayList(topPlayers));
            logger.info("Leaderboard loaded successfully with {} players", topPlayers.size());
        } catch (Exception e) {
            logger.error("Error loading leaderboard", e);
        }
    }

    @FXML
    private void handleRefresh() {
        loadLeaderboard();
    }

    @FXML
    private void handleBack() {
        try {
            logger.info("Navigating back to previous view");
            App.loadView("/com/tictactoe/fxml/menu.fxml");
        } catch (Exception e) {
            logger.error("Error navigating back", e);
        }
    }
} 