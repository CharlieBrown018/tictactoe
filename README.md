
# TicTacToe

A modern JavaFX implementation of the classic Tic Tac Toe game with multiple game modes, player statistics tracking and database persistence.

## Features

- Modern, dark-themed UI with animations
- Two game modes:
  - Classic Mode: Traditional 3x3 gameplay
  - Endless Mode: Board clears randomly when full until someone wins
- Two-player gameplay
- Player statistics tracking
- Game history persistence
- Dark theme with dynamic animations
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

## Running the Game

After building, you can run the game using:

```bash
mvn clean javafx:run
```

## Gameplay Instructions

1. **Starting a Game**
   - Launch the game
   - Choose between Classic or Endless mode
   - Enter names for Player 1 (X) and Player 2 (O)

2. **Game Modes**
   - **Classic Mode**: Traditional Tic Tac Toe rules apply
   - **Endless Mode**: When the board fills up, random tiles clear automatically until someone wins

3. **Making Moves**
   - Players take turns placing their symbols (X or O)
   - Player 1 always plays as X
   - Player 2 always plays as O
   - Click any empty cell to make your move

4. **Winning the Game**
   - Get three of your symbols in a row (horizontal, vertical, or diagonal)
   - In Classic Mode: Game ends in a draw if board fills with no winner
   - In Endless Mode: Game continues with random tile clearing until there's a winner

## Statistics and Leaderboard

- View player statistics including:
  - Total wins
  - Total losses
  - Total draws
  - Win rate percentage
- Access the leaderboard from the main menu or after each game
- Statistics are persistently stored in the SQLite database

## Technical Features

- **Modern UI Components**
  - Smooth animations and transitions
  - Dynamic background effects
  - Responsive game board
  - Intuitive player information display

- **Database Integration**
  - SQLite database for persistent storage
  - Player statistics tracking
  - Game history recording
  - Efficient connection pooling

- **Architecture**
  - Model-View-Controller (MVC) pattern
  - Clean separation of concerns
  - Event-driven gameplay
  - Efficient state management

## Project Structure

```
src/main/java/com/tictactoe/
├── App.java                 # Main application class
├── controller/             # Game controllers
├── model/                 # Data models and entities
├── repository/           # Database access layer
├── service/             # Game logic and business rules
└── util/               # Utility classes
```

## Contributing

Feel free to fork the repository and submit pull requests for any improvements you'd like to add.

## License

This project is open source and available under the MIT License.</parameter>

