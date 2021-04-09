package com.server.connect5.rest;

import com.server.connect5.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.server.connect5.service.GameService.GAME_IN_PROGRESS_MESSAGE;
import static com.server.connect5.service.GameService.NOT_AUTHORIZED_MESSAGE;
import static com.server.connect5.service.GameService.USER_EXISTS_MESSAGE;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    /**
     * Add a new player to the game
     * <p>
     * Responds with 409 CONFLICT if two players are already playing or if a
     * player with the passed in name already exists
     *
     * @param playerName
     * @param discColor
     * @return message saying the player has been added and whether the game has started
     */
    @PutMapping(value = "join", produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> joinGame(@RequestParam String playerName,
                                           @RequestParam String discColor) {

        if (gameService.isGameActive()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(GAME_IN_PROGRESS_MESSAGE);
        }

        if (gameService.verifyPlayer(playerName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(USER_EXISTS_MESSAGE);
        }

        return gameService.addPlayer(playerName, discColor);
    }

    /**
     * Make a move on the game board by providing a column to insert the disc in
     * <p>
     * Validates whether the player is in the current game
     *
     * @param playerName
     * @param column
     * @return message about the move result
     */
    @PostMapping(value = "make-move", produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> makeMove(@RequestParam String playerName,
                                           @RequestParam int column) {

        if (gameService.verifyPlayer(playerName)) {
            return gameService.makeMove(playerName, column - 1);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(NOT_AUTHORIZED_MESSAGE);
        }
    }

    /**
     * Checks the status of the move (e.g. not your turn)
     * <p>
     * Validates whether the player is in the current game
     *
     * @param playerName
     * @return message about the move status
     */
    @GetMapping(value = "move-status")
    public ResponseEntity<String> moveStatus(@RequestParam String playerName) {

        if (gameService.verifyPlayer(playerName)) {
            return gameService.moveStatus(playerName);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(NOT_AUTHORIZED_MESSAGE);
        }
    }

    /**
     * Removes the player from the game
     * <p>
     * Validates whether the player is in the current game
     *
     * @param playerName
     * @return message saying whether the player has been disconnected
     */
    @PostMapping(value = "disconnect", produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> disconnect(@RequestParam String playerName) {

        if (gameService.verifyPlayer(playerName)) {
            return ResponseEntity.ok(gameService.disconnect(playerName));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(NOT_AUTHORIZED_MESSAGE);
        }
    }
}
