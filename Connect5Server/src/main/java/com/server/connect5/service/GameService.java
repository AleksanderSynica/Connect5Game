package com.server.connect5.service;

import com.server.connect5.component.GameBoard;
import com.server.connect5.component.GameState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.server.connect5.component.GameBoard.COL_LENGTH;
import static com.server.connect5.component.GameBoard.ROW_LENGTH;
import static com.server.connect5.component.GameBoard.WINNING_SCORE;

@Service
public class GameService {

    @Autowired
    private GameState gameState;

    @Autowired
    private GameBoard gameBoard;

    public static final String NOT_AUTHORIZED_MESSAGE = "User with that name is not in the game";
    public static final String USER_EXISTS_MESSAGE = "User with that name already exists. Try a different name";
    public static final String GAME_IN_PROGRESS_MESSAGE = "A game is in progress. Try again later.";

    /**
     * Tries to add the requesting player to the game
     * If first player joins, he's notified he must wait for another player
     * <p>
     * If the second player joins, he's added to the game and the game is started
     * <p>
     * If the second player picks the color that the first player already picked,
     * he's automatically reassigned to the other color
     *
     * @param playerName
     * @param discColor
     * @return Response Entity with an appropriate code and message
     */
    public ResponseEntity<String> addPlayer(final String playerName, final String discColor) {

        boolean colorChanged = false;
        String newColour = discColor;

        if (gameState.getPlayerCount() == 0) {
            gameState.addPlayer(playerName, discColor);
            return ResponseEntity.ok("Joined game, waiting for second player to join...");
        } else {

            boolean colorExists = gameState.getPlayers().values().stream().anyMatch(player
                    -> player.getDiscColor().equals(discColor));

            if (colorExists) {
                newColour = discColor.equals("Red") ? "Blue" : "Red";
                colorChanged = true;
            }

            gameState.addPlayer(playerName, newColour);

            String response = beginGame();

            if (colorChanged) {
                response = "The color you chose was taken. New color is " + newColour + ""
                        + System.getProperty("line.separator") + response;
            }

            return ResponseEntity.ok(response);
        }
    }

    /**
     * Sets the game to active and picks one of the players to go first at random
     *
     * @return message to the player stating the game has started
     */
    private String beginGame() {

        gameState.setGameActive(true);

        List<?> keys = new ArrayList<>(gameState.getPlayers().keySet());
        Random r = new Random();

        gameState.setLastToMove(gameState.getPlayers().get(keys.get(r.nextInt(keys.size())))
                .getPlayerName());

        return "Game has started" + System.getProperty("line.separator") +
                "You'll be notified when it's your move";
    }

    /**
     * Attempts to insert a disc on the board from a player based on the entered column number
     * If the disc is not inserted it means the column was full
     * <p>
     * If the disc was inserted it checks if there are 5 discs in a row and the game is won
     * <p>
     * If it is not the turn of the player attempting to make the move the move is not allowed
     *
     * @param playerName
     * @param column
     * @return message to the player based on the move outcome
     */
    public ResponseEntity<String> makeMove(final String playerName, final int column) {

        if (!gameState.getLastToMove().equals(playerName)) {

            boolean discInserted = insertDisc(playerName, column, gameState.getPlayers().get(playerName)
                    .getDiscColor().charAt(0));

            if (discInserted) {

                if (checkIfWon(gameState.getPlayers().get(playerName).getDiscColor().charAt(0))) {
                    gameState.setWinner(playerName);
                    gameState.removePlayer(playerName);
                    return ResponseEntity.ok(gameBoard.printBoard() + System.getProperty("line.separator") +
                            "Game is over. You have won the game");
                }

                return ResponseEntity.status(HttpStatus.ACCEPTED).body(gameBoard.printBoard() +
                        "You made your move " + playerName + ", please wait for the other player to make his");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(gameBoard.printBoard()
                        + System.getProperty("line.separator") +
                        "Column " + (column + 1) + " is full" + System.getProperty("line.separator"));
            }

        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("It's not your turn " + playerName);
        }
    }

    /**
     * Tries to insert the disc on the lowermost part of the specified column number
     * <p>
     * Will return false if column was full
     *
     * @param playerName
     * @param column
     * @param playerDisc
     * @return boolean whether the disc was inserted
     */
    private boolean insertDisc(final String playerName, final int column, final char playerDisc) {
        for (int i = ROW_LENGTH - 1; i > -1; i--) {
            if (gameBoard.currentBoard[i][column] == ' ') {
                gameBoard.currentBoard[i][column] = playerDisc;

                gameState.setLastToMove(playerName);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the status of the game to the player
     * <p>
     * If there is a winner in the game state, it notifies the losing player,
     * removes him from the game and ends the game
     * <p>
     * If the game is active but there is only one player in the state it ends
     * the game as the other player has disconnected
     * <p>
     * If there are 2 players in the game and there is no winner,
     * it returns an whether it is the turn of the requesting player
     *
     * @param playerName
     * @return Response Entity with an appropriate code and message
     */
    public ResponseEntity<String> moveStatus(final String playerName) {

        if (gameState.getWinner() != null) {

            String message = gameBoard.printBoard() + System.getProperty("line.separator") +
                    "Game is over. You have lost.";

            gameState.removePlayer(playerName);
            endGame();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }

        if (gameState.getPlayerCount() == 1) {
            if (gameState.isGameActive()) {
                gameState.removePlayer(playerName);
                endGame();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The other player has disconnected." + System.getProperty("line.separator") +
                        "You have won!");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("The game has not started, waiting on another player");
            }
        }

        return gameState.getLastToMove().equals(playerName) ?
                ResponseEntity.status(HttpStatus.CONFLICT).body("Not your move")
                : ResponseEntity.ok(
                gameBoard.printBoard() + System.getProperty("line.separator") +
                        "It's your turn " + playerName + ", please enter column  (1-9 or 0 to disconnect)");
    }

    /**
     * End the current game. So a new game can be started.
     * Clear the board.
     */
    private void endGame() {
        gameState.setGameActive(false);
        gameBoard.clearBoard();
        gameState.setWinner(null);
        gameState.setLastToMove(null);
    }

    /**
     * Remove the player from the game
     *
     * @param playerName
     * @return message for the disconnecting player
     */
    public String disconnect(final String playerName) {
        gameState.removePlayer(playerName);
        return "You have successfully disconnected from the game";
    }

    /**
     * Verify if player exists in the Game State
     *
     * @param playerName
     * @return boolean
     */
    public boolean verifyPlayer(final String playerName) {
        return gameState.getPlayers().get(playerName) != null;
    }

    /**
     * Returns whether there is an ongoing game
     *
     * @return boolean
     */
    public boolean isGameActive() {
        return gameState.isGameActive();
    }

    /**
     * After a move is made by one of the players determine if the game has been won
     * by checking if there are 5 discs in a row somewhere on the board
     *
     * @param disc either (B)lue or (R)ed disc
     * @return boolean if the game is won or not
     */
    private boolean checkIfWon(final char disc) {

        for (int i = 0; i < gameBoard.currentBoard.length; i++) {
            for (int j = 0; j < gameBoard.currentBoard[i].length; j++) {
                if (gameBoard.currentBoard[i][j] == disc) {

                    // check if 5 in a row horizontally
                    int discCount = checkHorizontal(disc, i, j);

                    if (discCount >= WINNING_SCORE) {
                        return true;
                    }

                    // check if 5 in a row vertically
                    discCount = checkVertical(disc, i, j);

                    if (discCount >= WINNING_SCORE) {
                        return true;
                    }

                    // check if 5 in a row diagonally to the right
                    discCount = checkDiagonallyRight(disc, i, j);

                    if (discCount >= WINNING_SCORE) {
                        return true;
                    }

                    // check if 5 in a row diagonally to the left
                    discCount = checkDiagonallyLeft(disc, i, j);

                    if (discCount >= WINNING_SCORE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check the largest number of concurrent horizontal discs
     *
     * @param disc either (B)lue or (R)ed disc
     * @param i    row index
     * @param j    column index
     * @return largest disc count horizontally
     */
    private int checkHorizontal(final char disc, int i, int j) {
        int discCount = 1;

        int k = j - 1;
        while (k > 0) {
            if (gameBoard.currentBoard[i][k] == disc) {
                discCount++;
            } else {
                break;
            }
            k--;
        }

        k = j + 1;

        while (k < COL_LENGTH) {
            if (gameBoard.currentBoard[i][k] == disc) {
                discCount++;
            } else {
                break;
            }
            k++;
        }

        return discCount;
    }

    /**
     * Check the largest number of concurrent vertical discs
     *
     * @param disc either (B)lue or (R)ed disc
     * @param i    row index
     * @param j    column index
     * @return largest disc count vertically
     */
    private int checkVertical(final char disc, int i, int j) {
        int discCount = 1;

        int k = i - 1;
        while (k > 0) {
            if (gameBoard.currentBoard[k][j] == disc) {
                discCount++;
            } else {
                break;
            }
            k--;
        }

        k = i + 1;

        while (k < ROW_LENGTH) {
            if (gameBoard.currentBoard[k][j] == disc) {
                discCount++;
            } else {
                break;
            }
            k++;
        }

        return discCount;
    }

    /**
     * Check the largest number of concurrent diagonal (to the right) discs
     *
     * @param disc either (B)lue or (R)ed disc
     * @param i    row index
     * @param j    column index
     * @return largest disc count diagonally to the right
     */
    private int checkDiagonallyRight(final char disc, int i, int j) {
        int discCount = 1;

        int k = i - 1;
        int l = j + 1;
        while (k > 0 && l < COL_LENGTH) {
            if (gameBoard.currentBoard[k][l] == disc) {
                discCount++;
            } else {
                break;
            }
            k--;
            l++;
        }

        k = i + 1;
        l = j - 1;

        while (k < ROW_LENGTH && l > 0) {
            if (gameBoard.currentBoard[k][l] == disc) {
                discCount++;
            } else {
                break;
            }
            k++;
            l--;
        }

        return discCount;
    }

    /**
     * Check the largest number of concurrent diagonal (to the left) discs
     *
     * @param disc either (B)lue or (R)ed disc
     * @param i    row index
     * @param j    column index
     * @return largest disc count diagonally to the left
     */
    private int checkDiagonallyLeft(final char disc, int i, int j) {
        int discCount = 1;

        int k = i - 1;
        int l = j - 1;
        while (k > 0 && l > 0) {
            if (gameBoard.currentBoard[k][l] == disc) {
                discCount++;
            } else {
                break;
            }
            k--;
            l--;
        }

        k = i + 1;
        l = j + 1;

        while (k < ROW_LENGTH && l < COL_LENGTH) {
            if (gameBoard.currentBoard[k][l] == disc) {
                discCount++;
            } else {
                break;
            }
            k++;
            l++;
        }

        return discCount;
    }

}
