package com.server.connect5.rest;

import com.server.connect5.service.GameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.server.connect5.service.GameService.GAME_IN_PROGRESS_MESSAGE;
import static com.server.connect5.service.GameService.NOT_AUTHORIZED_MESSAGE;
import static com.server.connect5.service.GameService.USER_EXISTS_MESSAGE;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    private static final String BASE_URL = "http://localhost:8080/";

    private static final String JOIN_URL = BASE_URL + "join";
    private static final String MAKE_MOVE_URL = BASE_URL + "make-move";
    private static final String MOVE_STATUS_URL = BASE_URL + "move-status";
    private static final String DISCONNECT_URL = BASE_URL + "disconnect";

    private static final String PLAYER_JOINS_MESSAGE = "Joined game, waiting for second player to join...";
    private static final String MOVE_MADE_MESSAGE = "You have made your move";
    private static final String MOVE_STATE_MESSAGE = "It is your move";
    private static final String DISCONNECTED_MESSAGE = "It is your move";

    private final String testPlayerName = "player";
    private final String testDiscColor = "Red";
    private final int columnNumber = 3;

    private final String JOIN_PARAMS = "?playerName=" + testPlayerName + "&discColor=" + testDiscColor;
    private final String MAKE_MOVE_PARAMS = "?playerName=" + testPlayerName + "&column=" + columnNumber;
    private final String MOVE_STATUS_PARAMS = "?playerName=" + testPlayerName;

    @Test
    @DisplayName("Testing that a player can't join if there is a game in progress")
    void joinGameConflictGameInProgress() throws Exception {

        when(gameService.isGameActive()).thenReturn(true);

        this.mockMvc.perform(put(JOIN_URL + JOIN_PARAMS)
                .contentType(MediaType.ALL_VALUE))
                .andExpect(status().isConflict())
                .andExpect(content().string(GAME_IN_PROGRESS_MESSAGE));

        verify(gameService, times(0)).addPlayer(testPlayerName, testDiscColor);
    }

    @Test
    @DisplayName("Testing that a player can't join if there is a player in the game with the same name")
    void joinGameConflictUserExists() throws Exception {

        when(gameService.verifyPlayer(testPlayerName)).thenReturn(true);

        this.mockMvc.perform(put(JOIN_URL + JOIN_PARAMS)
                .contentType(MediaType.ALL_VALUE))
                .andExpect(status().isConflict())
                .andExpect(content().string(USER_EXISTS_MESSAGE));

        verify(gameService, times(0)).addPlayer(testPlayerName, testDiscColor);
    }

    @Test
    @DisplayName("Testing that a player can join if no game is in progress and the name is unique")
    void joinGame() throws Exception {

        when(gameService.addPlayer(testPlayerName, testDiscColor))
                .thenReturn(ResponseEntity.ok(PLAYER_JOINS_MESSAGE));

        this.mockMvc.perform(put(JOIN_URL + JOIN_PARAMS)
                .contentType(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(PLAYER_JOINS_MESSAGE));

        verify(gameService, times(1)).addPlayer(testPlayerName, testDiscColor);
    }

    @Test
    @DisplayName("Testing that a player cannot make a move if he is not in the game")
    void makeMoveUnauthorizedPlayer() throws Exception {

        this.mockMvc.perform(post(MAKE_MOVE_URL + MAKE_MOVE_PARAMS)
                .contentType(MediaType.ALL_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(NOT_AUTHORIZED_MESSAGE));

        verify(gameService, times(0)).makeMove(testPlayerName, columnNumber - 1);
    }

    @Test
    @DisplayName("Testing that a player can make a move if he is in the game")
    void makeMove() throws Exception {

        when(gameService.verifyPlayer(testPlayerName)).thenReturn(true);
        when(gameService.makeMove(testPlayerName, columnNumber - 1))
                .thenReturn(ResponseEntity.ok(MOVE_MADE_MESSAGE));

        this.mockMvc.perform(post(MAKE_MOVE_URL + MAKE_MOVE_PARAMS)
                .contentType(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(MOVE_MADE_MESSAGE));

        verify(gameService, times(1)).makeMove(testPlayerName, columnNumber - 1);
    }

    @Test
    @DisplayName("Testing that a player cannot request a move status if he is not in the game")
    void moveStatusUnauthorizedPlayer() throws Exception {

        this.mockMvc.perform(get(MOVE_STATUS_URL + MOVE_STATUS_PARAMS)
                .contentType(MediaType.ALL_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(NOT_AUTHORIZED_MESSAGE));

        verify(gameService, times(0)).moveStatus(testPlayerName);
    }

    @Test
    @DisplayName("Testing that a player can request a move status if he is in the game")
    void moveStatus() throws Exception {

        when(gameService.verifyPlayer(testPlayerName)).thenReturn(true);
        when(gameService.moveStatus(testPlayerName)).thenReturn(ResponseEntity.ok(MOVE_STATE_MESSAGE));

        this.mockMvc.perform(get(MOVE_STATUS_URL + MOVE_STATUS_PARAMS)
                .contentType(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(MOVE_STATE_MESSAGE));

        verify(gameService, times(1)).moveStatus(testPlayerName);
    }

    @Test
    @DisplayName("Testing that a player cannot disconnect from the game if he is not in it")
    void disconnectUnauthorized() throws Exception {

        this.mockMvc.perform(post(DISCONNECT_URL + MOVE_STATUS_PARAMS)
                .contentType(MediaType.ALL_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(NOT_AUTHORIZED_MESSAGE));

        verify(gameService, times(0)).disconnect(testPlayerName);
    }

    @Test
    @DisplayName("Testing that a player can disconnect from the game if he is in it")
    void disconnect() throws Exception {

        when(gameService.verifyPlayer(testPlayerName)).thenReturn(true);
        when(gameService.disconnect(testPlayerName)).thenReturn((DISCONNECTED_MESSAGE));

        this.mockMvc.perform(post(DISCONNECT_URL + MOVE_STATUS_PARAMS)
                .contentType(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(DISCONNECTED_MESSAGE));

        verify(gameService, times(1)).disconnect(testPlayerName);
    }
}
