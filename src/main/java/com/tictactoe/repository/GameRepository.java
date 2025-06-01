package com.tictactoe.repository;

import com.tictactoe.model.entity.GameEntity;
import com.tictactoe.model.enums.GameMode;
import com.tictactoe.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameRepository {
    private static final Logger logger = LoggerFactory.getLogger(GameRepository.class);
    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS games (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            player1_id INTEGER NOT NULL,
            player2_id INTEGER NOT NULL,
            winner_id INTEGER,
            is_draw BOOLEAN NOT NULL,
            game_mode TEXT NOT NULL,
            moves TEXT NOT NULL,
            played_at TIMESTAMP NOT NULL,
            FOREIGN KEY (player1_id) REFERENCES players(id),
            FOREIGN KEY (player2_id) REFERENCES players(id),
            FOREIGN KEY (winner_id) REFERENCES players(id)
        )
    """;

    public GameRepository() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
            logger.info("Games table created or verified");
        } catch (SQLException e) {
            logger.error("Error creating games table", e);
        }
    }

    public GameEntity save(GameEntity game) {
        String sql = "INSERT INTO games (player1_id, player2_id, winner_id, is_draw, game_mode, moves, played_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, game.getPlayer1Id());
                stmt.setLong(2, game.getPlayer2Id());
                if (game.getWinnerId() != null) {
                    stmt.setLong(3, game.getWinnerId());
                } else {
                    stmt.setNull(3, java.sql.Types.INTEGER);
                }
                stmt.setBoolean(4, game.isDraw());
                stmt.setString(5, game.getGameMode().name());
                stmt.setString(6, game.getMoves());
                stmt.setTimestamp(7, Timestamp.valueOf(game.getPlayedAt()));
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Creating game failed, no rows affected.");
                }

                // Get the last inserted ID using SQLite's last_insert_rowid()
                try (Statement idStmt = conn.createStatement();
                     ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        game.setId(rs.getLong(1));
                        logger.info("Saved game with ID: {}", game.getId());
                    } else {
                        throw new SQLException("Creating game failed, no ID obtained.");
                    }
                }
                
                return game;
            }
        } catch (SQLException e) {
            logger.error("Error saving game", e);
            throw new RuntimeException("Error saving game", e);
        } finally {
            if (conn != null) {
                DatabaseConfig.releaseConnection(conn);
            }
        }
    }

    public List<GameEntity> findRecentGames(int limit) {
        String sql = """
            SELECT g.*, 
                   p1.name as player1_name,
                   p2.name as player2_name,
                   w.name as winner_name
            FROM games g
            LEFT JOIN players p1 ON g.player1_id = p1.id
            LEFT JOIN players p2 ON g.player2_id = p2.id
            LEFT JOIN players w ON g.winner_id = w.id
            ORDER BY g.played_at DESC
            LIMIT ?
        """;

        List<GameEntity> games = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    GameEntity game = mapResultSetToGame(rs);
                    games.add(game);
                }
            }
            
            logger.info("Found {} recent games", games.size());
            return games;
        } catch (SQLException e) {
            logger.error("Error finding recent games", e);
            throw new RuntimeException("Error finding recent games", e);
        }
    }

    private GameEntity mapResultSetToGame(ResultSet rs) throws SQLException {
        GameEntity game = new GameEntity();
        game.setId(rs.getLong("id"));
        game.setPlayer1Id(rs.getLong("player1_id"));
        game.setPlayer2Id(rs.getLong("player2_id"));
        
        long winnerId = rs.getLong("winner_id");
        if (!rs.wasNull()) {
            game.setWinnerId(winnerId);
        }
        
        game.setDraw(rs.getBoolean("is_draw"));
        game.setGameMode(GameMode.valueOf(rs.getString("game_mode")));
        game.setMoves(rs.getString("moves"));
        game.setPlayedAt(rs.getTimestamp("played_at").toLocalDateTime());
        
        // Set player names if available in result set
        try {
            game.setPlayer1Name(rs.getString("player1_name"));
            game.setPlayer2Name(rs.getString("player2_name"));
            game.setWinnerName(rs.getString("winner_name"));
        } catch (SQLException e) {
            // Ignore if these columns aren't in the result set
        }
        
        return game;
    }

    public Optional<GameEntity> findById(Long id) {
        String sql = """
            SELECT id, player1_id, player2_id, winner_id, is_draw, moves, played_at
            FROM games
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    GameEntity game = new GameEntity();
                    game.setId(rs.getLong("id"));
                    game.setPlayer1Id(rs.getLong("player1_id"));
                    game.setPlayer2Id(rs.getLong("player2_id"));
                    game.setWinnerId(rs.getObject("winner_id", Long.class));
                    game.setDraw(rs.getBoolean("is_draw"));
                    game.setMoves(rs.getString("moves"));
                    game.setPlayedAt(rs.getTimestamp("played_at").toLocalDateTime());
                    return Optional.of(game);
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding game by ID: {}", id, e);
            throw new RuntimeException("Error finding game by ID: " + id, e);
        }
    }

    public List<GameEntity> findGamesByPlayer(Long playerId) {
        String sql = "SELECT * FROM games WHERE player1_id = ? OR player2_id = ? ORDER BY played_at DESC";
        List<GameEntity> games = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, playerId);
                pstmt.setLong(2, playerId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    games.add(mapResultSetToGame(rs));
                }
                
                logger.info("Found {} games for player {}", games.size(), playerId);
                return games;
            }
        } catch (SQLException e) {
            logger.error("Error finding games for player: {}", playerId, e);
            throw new RuntimeException("Error finding games", e);
        } finally {
            if (conn != null) {
                DatabaseConfig.releaseConnection(conn);
            }
        }
    }
} 