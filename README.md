# Tic Tac Toe Game

A modern JavaFX implementation of the classic Tic Tac Toe game with player statistics tracking and database persistence.

## Features

- Modern, responsive UI with animations
- Two-player gameplay
- Player statistics tracking
- Game history persistence
- Dark/Light theme support
- SQLite database integration
- Clean MVC architecture

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building the Project

To build the project, run:

```bash
mvn clean package
```

This will create an executable JAR file in the `target` directory.

## Running the Game

You can run the game in two ways:

1. Using Maven:
```bash
mvn javafx:run
```

2. Using the JAR file:
```bash
java -jar target/tictactoe-1.0-SNAPSHOT.jar
```

## Project Structure

```
src/main/java/com/tictactoe/
├── config/         # Configuration classes
├── controller/     # Game controllers
├── model/
│   ├── entity/    # Database entities
│   ├── enums/     # Game enums
│   └── dto/       # Data transfer objects
├── repository/    # Database access layer
├── service/       # Business logic
├── util/          # Utility classes
└── view/
    ├── component/ # Reusable UI components
    ├── dialog/    # Custom dialogs
    └── style/     # CSS styles
```

## Database Schema

### Players Table
- id (PRIMARY KEY)
- name (TEXT)
- wins (INTEGER)
- losses (INTEGER)
- draws (INTEGER)
- created_at (TIMESTAMP)

### Games Table
- id (PRIMARY KEY)
- player1_id (FOREIGN KEY)
- player2_id (FOREIGN KEY)
- winner_id (FOREIGN KEY)
- is_draw (BOOLEAN)
- moves (TEXT)
- played_at (TIMESTAMP)

## Technologies Used

- JavaFX - UI framework
- SQLite - Database
- SLF4J/Logback - Logging
- JUnit 5 - Testing
- TestFX - UI Testing
- Maven - Build tool

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 