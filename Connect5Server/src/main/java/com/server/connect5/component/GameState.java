package com.server.connect5.component;

import com.server.connect5.model.Player;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GameState {

    private int playerCount = 0;
    private Map<String, Player> players = new HashMap<>();

    private String winner = null;

    private String lastToMove;
    private boolean gameActive = false;

    public boolean isGameActive() {
        return gameActive;
    }

    public void setGameActive(boolean gameActive) {
        this.gameActive = gameActive;
    }

    public String getLastToMove() {
        return lastToMove;
    }

    public void setLastToMove(String lastToMove) {
        this.lastToMove = lastToMove;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    /**
     * Adds the player to the player map with a name and disc color
     * Increment the playerCount
     *
     * @param playerName
     * @param discColor
     */
    public void addPlayer(final String playerName, final String discColor) {

        Player player = new Player(playerName, discColor);

        this.players.put(playerName, player);
        this.playerCount++;
    }

    /**
     * Removes the player from the player map
     * decrements the player count
     *
     * @param playerName
     */
    public void removePlayer(String playerName) {

        this.players.remove(playerName);
        this.playerCount--;
    }
}
