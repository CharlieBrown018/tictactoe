package com.tictactoe.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String DB_URL = "jdbc:sqlite:tictactoe.db";
    private static final int POOL_SIZE = 5;
    private static final BlockingQueue<Connection> connectionPool = new ArrayBlockingQueue<>(POOL_SIZE);
    private static boolean isInitialized = false;

    static {
        initializePool();
    }

    private static void initializePool() {
        try {
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection conn = createConnection();
                if (i == 0 && !isInitialized) {
                    initializeDatabase(conn);
                    isInitialized = true;
                }
                connectionPool.offer(conn);
            }
            logger.info("Database connection pool initialized with {} connections", POOL_SIZE);
        } catch (SQLException e) {
            logger.error("Error initializing connection pool", e);
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }

    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void initializeDatabase(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            // Create players table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    wins INTEGER DEFAULT 0,
                    losses INTEGER DEFAULT 0,
                    draws INTEGER DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create games table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS games (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player1_id INTEGER NOT NULL,
                    player2_id INTEGER NOT NULL,
                    winner_id INTEGER,
                    is_draw BOOLEAN NOT NULL DEFAULT FALSE,
                    game_mode TEXT NOT NULL DEFAULT 'CLASSIC',
                    moves TEXT NOT NULL,
                    played_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (player1_id) REFERENCES players (id),
                    FOREIGN KEY (player2_id) REFERENCES players (id),
                    FOREIGN KEY (winner_id) REFERENCES players (id)
                )
            """);
            
            logger.info("Database tables initialized successfully");
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
            throw e;
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionPool.poll();
            if (conn == null || conn.isClosed()) {
                conn = createConnection();
            }
            return conn;
        } catch (SQLException e) {
            logger.error("Error getting connection from pool", e);
            throw e;
        }
    }

    public static void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed() && connectionPool.size() < POOL_SIZE) {
                    connectionPool.offer(conn);
                } else {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error releasing connection", e);
            }
        }
    }

    public static void closeAllConnections() {
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
        logger.info("All database connections closed");
    }
} 