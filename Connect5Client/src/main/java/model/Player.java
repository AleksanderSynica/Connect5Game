package model;

public class Player {

    private String playerName;
    private String discColor;

    public Player(String playerName, String playerColor) {
        this.playerName = playerName;
        this.discColor = playerColor;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDiscColor() {
        return discColor;
    }


}