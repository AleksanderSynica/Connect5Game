package com.server.connect5.service;

import com.server.connect5.component.GameBoard;
import com.server.connect5.component.GameState;
import com.server.connect5.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class GameServiceTest {

    @Autowired
    private GameService gameService;

    @MockBean
    private GameState gameState;

    @MockBean
    private GameBoard gameBoard;

    private static final String PLAYER_JOINS_MESSAGE = "Joined game, waiting for second player to join...";
    private static final String GAME_STARTED_MESSAGE = "Game has started" + System.getProperty("line.separator") +
            "You'll be notified when it's your move";

    private final String testPlayerName = "player";
    private final String testDiscColor = "Red";
    private final int columnNumber = 3;

    private Map<String, Player> params = new HashMap<>();
    private Player testPlayer = new Player(testPlayerName, testDiscColor);

    @BeforeEach
    void setUp() {
        gameBoard.currentBoard = new char[GameBoard.ROW_LENGTH][GameBoard.COL_LENGTH];

        for (char[] r : gameBoard.currentBoard)
            Arrays.fill(r, ' ');
    }

    @Test
    @DisplayName("Testing that a player can be added to the game and game doesn't start if it's the first player")
    void addPlayerFirstPlayer() {

        when(gameState.getPlayerCount()).thenReturn(0);

        ResponseEntity<String> testResponse = gameService.addPlayer(testPlayerName, testDiscColor);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 200 OK", testResponse.getStatusCode(),
                        is(HttpStatus.OK)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(PLAYER_JOINS_MESSAGE)));

        verify(gameState, times(1)).addPlayer(testPlayerName, testDiscColor);
    }

    @Test
    @DisplayName("Testing that if a second player joins he is added to the state and the game starts")
    void addPlayerSecondPlayerStartGame() {

        Player existingPlayer = new Player("player1", "Blue");

        params.put(existingPlayer.getPlayerName(), existingPlayer);

        when(gameState.getPlayerCount()).thenReturn(1);
        when(gameState.getPlayers()).thenReturn(params);

        ResponseEntity<String> testResponse = gameService.addPlayer(testPlayerName, testDiscColor);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 200 OK", testResponse.getStatusCode(),
                        is(HttpStatus.OK)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(GAME_STARTED_MESSAGE)));

        verify(gameState, times(1)).addPlayer(testPlayerName, testDiscColor);
    }

    @Test
    @DisplayName("Testing that if a second player joins with the same colour, the color will be changed")
    void addPlayerSecondPlayerStartGameSameColor() {

        Player existingPlayer = new Player("player1", "Red");

        params.put(existingPlayer.getPlayerName(), existingPlayer);

        String newColorMessage = "The color you chose was taken. New color is Blue"
                + System.getProperty("line.separator");

        when(gameState.getPlayerCount()).thenReturn(1);
        when(gameState.getPlayers()).thenReturn(params);

        ResponseEntity<String> testResponse = gameService.addPlayer(testPlayerName, testDiscColor);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 200 OK", testResponse.getStatusCode(),
                        is(HttpStatus.OK)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(newColorMessage + GAME_STARTED_MESSAGE)));

        verify(gameState, times(1)).addPlayer(testPlayerName, "Blue");
    }

    @Test
    @DisplayName("Testing that if a player tries to make a move but it's not their turn a correct message is returned")
    void makeMoveNotPlayerTurn() {

        when(gameState.getLastToMove()).thenReturn(testPlayerName);

        String responseMessage = "It's not your turn " + testPlayerName;

        ResponseEntity<String> testResponse = gameService.makeMove(testPlayerName, columnNumber);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 400 BAD REQUEST", testResponse.getStatusCode(),
                        is(HttpStatus.BAD_REQUEST)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(responseMessage)));

        verify(gameState, times(1)).getLastToMove();
        verify(gameState, times(0)).getPlayers();
        verify(gameState, times(0)).setWinner(testPlayerName);
        verify(gameState, times(0)).removePlayer(testPlayerName);
        verify(gameBoard, times(0)).printBoard();
    }

    @Test
    @DisplayName("Testing that if a player tries to make a move but the column is full a correct message is returned")
    void makeMoveFullColumn() {

        params.put(testPlayer.getPlayerName(), testPlayer);

        when(gameState.getLastToMove()).thenReturn("otherPlayer");
        when(gameState.getPlayers()).thenReturn(params);

        gameBoard.currentBoard = new char[GameBoard.ROW_LENGTH][GameBoard.COL_LENGTH];

        String responseMessage = gameBoard.printBoard() + System.getProperty("line.separator") +
                "Column " + (columnNumber + 1) + " is full" + System.getProperty("line.separator") +
                "It's your turn " + testPlayerName + ", please enter column  (1-9 or 0 to disconnect)";

        ResponseEntity<String> testResponse = gameService.makeMove(testPlayerName, columnNumber);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 400 BAD REQUEST", testResponse.getStatusCode(),
                        is(HttpStatus.BAD_REQUEST)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(responseMessage)));

        verify(gameState, times(1)).getLastToMove();
        verify(gameState, times(1)).getPlayers();
        verify(gameState, times(0)).setWinner(testPlayerName);
        verify(gameState, times(0)).removePlayer(testPlayerName);
        verify(gameBoard, times(2)).printBoard();
    }

    @Test
    @DisplayName("Testing that if a player makes a valid move but is not a winner the correct response is returned")
    void makeMoveValidNotWinner() {

        params.put(testPlayer.getPlayerName(), testPlayer);

        when(gameState.getLastToMove()).thenReturn("otherPlayer");
        when(gameState.getPlayers()).thenReturn(params);

        String responseMessage = gameBoard.printBoard() +
                "You made your move " + testPlayerName + ", please wait for the other player to make his";

        ResponseEntity<String> testResponse = gameService.makeMove(testPlayerName, columnNumber);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 202 ACCEPTED", testResponse.getStatusCode(),
                        is(HttpStatus.ACCEPTED)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(responseMessage)));

        verify(gameState, times(1)).getLastToMove();
        verify(gameState, times(2)).getPlayers();
        verify(gameState, times(0)).setWinner(testPlayerName);
        verify(gameState, times(0)).removePlayer(testPlayerName);
        verify(gameBoard, times(2)).printBoard();
    }

    @Test
    @DisplayName("Testing that if a player makes a valid move and is a winner")
    void makeMoveValidIsWinner() {

        params.put(testPlayer.getPlayerName(), testPlayer);

        when(gameState.getLastToMove()).thenReturn("otherPlayer");
        when(gameState.getPlayers()).thenReturn(params);

        Arrays.fill(gameBoard.currentBoard[1], testDiscColor.charAt(0));

        String responseMessage = gameBoard.printBoard() + System.getProperty("line.separator") +
                "Game is over. You have won the game";

        ResponseEntity<String> testResponse = gameService.makeMove(testPlayerName, columnNumber);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 200 OK", testResponse.getStatusCode(),
                        is(HttpStatus.OK)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(responseMessage)));

        verify(gameState, times(1)).getLastToMove();
        verify(gameState, times(2)).getPlayers();
        verify(gameState, times(1)).setWinner(testPlayerName);
        verify(gameState, times(1)).removePlayer(testPlayerName);
        verify(gameBoard, times(2)).printBoard();
    }

    @Test
    @DisplayName("Testing that if a player exists in the game state true is returned")
    void verifyPlayerValid() {
        params.put(testPlayer.getPlayerName(), testPlayer);

        when(gameState.getPlayers()).thenReturn(params);

        assertTrue(gameService.verifyPlayer(testPlayerName));
    }

    @Test
    @DisplayName("Testing that if a player doesn't exist in the game state false is returned")
    void verifyPlayerInvalid() {
        assertFalse(gameService.verifyPlayer(testPlayerName));
    }

    @Test
    @DisplayName("Testing that disconnect returns correct message")
    void disconnect() {
        doNothing().when(gameState).removePlayer(testPlayerName);

        String response = gameService.disconnect(testPlayerName);

        assertEquals(response, "You have successfully disconnected from the game");

        verify(gameState, times(1)).removePlayer(testPlayerName);
    }

    @Test
    @DisplayName("Testing that getting a move status when it's not your move returns the correct message")
    void moveStatusNotPlayerMove() {

        when(gameState.getWinner()).thenReturn(null);
        when(gameState.getPlayerCount()).thenReturn(2);
        when(gameState.getLastToMove()).thenReturn(testPlayerName);

        ResponseEntity<String> testResponse = gameService.moveStatus(testPlayerName);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 409 Conflict", testResponse.getStatusCode(),
                        is(HttpStatus.CONFLICT)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is("Not your move")));

        verify(gameState, times(1)).getWinner();
        verify(gameState, times(1)).getLastToMove();
    }

    @Test
    @DisplayName("Testing that getting a move status when it is your move returns the correct message")
    void moveStatusIsPlayerMove() {

        String responseMessage = gameBoard.printBoard() + System.getProperty("line.separator") +
                "It's your turn " + testPlayerName + ", please enter column  (1-9 or 0 to disconnect)";

        when(gameState.getWinner()).thenReturn(null);
        when(gameState.getPlayerCount()).thenReturn(2);
        when(gameState.getLastToMove()).thenReturn("otherPlayer");

        ResponseEntity<String> testResponse = gameService.moveStatus(testPlayerName);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 200 OK", testResponse.getStatusCode(),
                        is(HttpStatus.OK)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(responseMessage)));

        verify(gameState, times(1)).getWinner();
        verify(gameState, times(1)).getLastToMove();
        verify(gameBoard, times(2)).printBoard();
    }

    @Test
    @DisplayName("Testing that getting a move status when the other player disconnected returns the correct message")
    void moveStatusOtherPlayerLeft() {

        String responseMessage = "The other player has disconnected." + System.getProperty("line.separator") +
                "You have won!";

        when(gameState.getWinner()).thenReturn(null);
        when(gameState.getPlayerCount()).thenReturn(1);
        when(gameState.isGameActive()).thenReturn(true);

        ResponseEntity<String> testResponse = gameService.moveStatus(testPlayerName);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 500 INTERNAL SERVER ERROR", testResponse.getStatusCode(),
                        is(HttpStatus.INTERNAL_SERVER_ERROR)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(responseMessage)));

        verify(gameState, times(1)).getWinner();
        verify(gameState, times(1)).getPlayerCount();
        verify(gameState, times(1)).isGameActive();
        verify(gameState, times(1)).removePlayer(testPlayerName);
    }

    @Test
    @DisplayName("Testing that getting a move status when the other player hasn't joined returns the correct message")
    void moveStatusOtherPlayerHasNotJoined() {

        String responseMessage = "The game has not started, waiting on another player";

        when(gameState.getWinner()).thenReturn(null);
        when(gameState.getPlayerCount()).thenReturn(1);
        when(gameState.isGameActive()).thenReturn(false);

        ResponseEntity<String> testResponse = gameService.moveStatus(testPlayerName);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 409 CONFLICT", testResponse.getStatusCode(),
                        is(HttpStatus.CONFLICT)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(responseMessage)));

        verify(gameState, times(1)).getWinner();
        verify(gameState, times(1)).getPlayerCount();
    }

    @Test
    @DisplayName("Testing that getting a move status when there is already a winner returns the correct message")
    void moveStatusOtherPlayerHasWon() {

        String responseMessage = gameBoard.printBoard() + System.getProperty("line.separator") +
                "Game is over. You have lost.";

        when(gameState.getWinner()).thenReturn("otherPlayer");

        ResponseEntity<String> testResponse = gameService.moveStatus(testPlayerName);

        assertAll("Response entity has the correct response code and message",
                () -> assertThat("Response code is 500 INTERNAL_SERVER_ERROR", testResponse.getStatusCode(),
                        is(HttpStatus.INTERNAL_SERVER_ERROR)),
                () -> assertThat("Response message is as expected", testResponse.getBody(),
                        is(responseMessage)));

        verify(gameState, times(1)).getWinner();
        verify(gameState, times(1)).removePlayer(testPlayerName);
        verify(gameBoard, times(2)).printBoard();
    }
}
