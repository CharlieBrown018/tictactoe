package com.tictactoe.repository;

import com.tictactoe.config.DatabaseConfig;
import com.tictactoe.model.entity.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerRepository {
    private static final Logger logger = LoggerFactory.getLogger(PlayerRepository.class);

    public PlayerEntity save(PlayerEntity player) {
        if (player.getId() == null) {
            return insert(player);
        } else {
            return update(player);
        }
    }

    private PlayerEntity insert(PlayerEntity player) {
        String sql = "INSERT INTO players (name, wins, losses, draws) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.getName());
                stmt.setInt(2, player.getWins());
                stmt.setInt(3, player.getLosses());
                stmt.setInt(4, player.getDraws());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Creating player failed, no rows affected.");
                }

                // Get the last inserted ID using SQLite's last_insert_rowid()
                try (Statement idStmt = conn.createStatement();
                     ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        player.setId(rs.getLong(1));
                        logger.info("Successfully saved player: {} with ID: {}", player.getName(), player.getId());
                    } else {
                        throw new SQLException("Creating player failed, no ID obtained.");
                    }
                }
                
                return player;
            }
        } catch (SQLException e) {
            logger.error("Error saving player: {}", player.getName(), e);
            throw new RuntimeException("Error saving player", e);
        } finally {
            if (conn != null) {
                DatabaseConfig.releaseConnection(conn);
            }
        }
    }

    private PlayerEntity update(PlayerEntity player) {
        String sql = "UPDATE players SET name = ?, wins = ?, losses = ?, draws = ? WHERE id = ?";
        Connection conn = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.getName());
                stmt.setInt(2, player.getWins());
                stmt.setInt(3, player.getLosses());
                stmt.setInt(4, player.getDraws());
                stmt.setLong(5, player.getId());
                
                stmt.executeUpdate();
                logger.info("Successfully updated player: {}", player.getName());
                return player;
            }
        } catch (SQLException e) {
            logger.error("Error updating player: {}", player.getId(), e);
            throw new RuntimeException("Error updating player", e);
        } finally {
            if (conn != null) {
                DatabaseConfig.releaseConnection(conn);
            }
        }
    }

    public Optional<PlayerEntity> findById(Long id) {
        String sql = "SELECT * FROM players WHERE id = ?";
        Connection conn = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return Optional.of(mapResultSetToPlayer(rs));
                }
                
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("Error finding player by id: {}", id, e);
            throw new RuntimeException("Error finding player", e);
        } finally {
            if (conn != null) {
                DatabaseConfig.releaseConnection(conn);
            }
        }
    }

    public Optional<PlayerEntity> findByName(String name) {
        String sql = "SELECT * FROM players WHERE name = ?";
        Connection conn = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return Optional.of(mapResultSetToPlayer(rs));
                }
                
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("Error finding player by name: {}", name, e);
            throw new RuntimeException("Error finding player", e);
        } finally {
            if (conn != null) {
                DatabaseConfig.releaseConnection(conn);
            }
        }
    }

    public List<PlayerEntity> findTopPlayers(int limit) {
        String sql = "SELECT * FROM players ORDER BY wins DESC LIMIT ?";
        List<PlayerEntity> players = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    players.add(mapResultSetToPlayer(rs));
                }
                
                return players;
            }
        } catch (SQLException e) {
            logger.error("Error finding top players", e);
            throw new RuntimeException("Error finding top players", e);
        } finally {
            if (conn != null) {
                DatabaseConfig.releaseConnection(conn);
            }
        }
    }

    private PlayerEntity mapResultSetToPlayer(ResultSet rs) throws SQLException {
        PlayerEntity player = new PlayerEntity();
        player.setId(rs.getLong("id"));
        player.setName(rs.getString("name"));
        player.setWins(rs.getInt("wins"));
        player.setLosses(rs.getInt("losses"));
        player.setDraws(rs.getInt("draws"));
        player.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return player;
    }
} 