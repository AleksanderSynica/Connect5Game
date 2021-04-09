package handler;

import model.Player;
import model.ResponseEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static handler.HttpHandler.DISCONNECT_URL;
import static handler.HttpHandler.JOIN_URL;
import static handler.HttpHandler.MAKE_MOVE_URL;
import static handler.HttpHandler.MOVE_STATUS_URL;
import static model.RequestVerb.GET;
import static model.RequestVerb.POST;
import static model.RequestVerb.PUT;

/**
 * This class handles interaction with the server
 */
public class InteractionHandler {

    private Player player;
    private BufferedReader scanner = new BufferedReader(
            new InputStreamReader(System.in));

    private static HttpHandler httpHandler = new HttpHandler();

    /**
     * Make the initial request to the server to join a game
     * Retry if name chosen is not unique or if a game is in progress
     *
     * @throws IOException
     */
    public void joinGame() throws IOException {
        System.out.println("Hello, to join a game please provide some information");

        Map<String, String> params = getPlayerInfo();

        // Send initial request to the server to join the game
        ResponseEntity response = httpHandler.sendHttpRequest(PUT, JOIN_URL, params);

        while (response.getResponseCode() != 200) {
            System.out.println(response.getMessage());

            System.out.println("Please enter the information again.");

            params = getPlayerInfo();

            // Send another join request if it fails
            response = httpHandler.sendHttpRequest(PUT, JOIN_URL, params);
        }

        System.out.println(response.getMessage());
    }

    /**
     * Loop method to send move requests to the server
     *
     * A request for status is sent every 2 seconds
     *
     * Initially to check if another player has joined the game
     *
     * Later to determine whose move it is
     *
     * Once the game has begun check whether the other player has disconnected
     *
     * If it is the players move ask for column input and send it to the server
     * Then return to status calls every 2 seconds
     *
     * @throws IOException
     */
    public void play() throws IOException {

        Map<String, String> params = new HashMap<>();
        params.put("playerName", player.getPlayerName());

        while (true) {
            ResponseEntity moveStatusResponse = httpHandler.sendHttpRequest(GET, MOVE_STATUS_URL, params);
            while (moveStatusResponse.getResponseCode() != 200 && moveStatusResponse.getResponseCode() != 500) {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                moveStatusResponse = httpHandler.sendHttpRequest(GET, MOVE_STATUS_URL, params);

            }

            if (moveStatusResponse.getResponseCode() == 500) {
                System.out.println(moveStatusResponse.getMessage());
                break;
            }

            System.out.println(moveStatusResponse.getMessage());

            int column;
            String columnInput = scanner.readLine();

            while (true) {
                try {
                    column = Integer.parseInt(columnInput);

                    if (column < 0 || column > 9) {
                        throw new NumberFormatException();
                    }
                    break;
                } catch (NumberFormatException nfe) {
                    System.out.println("Column value must be a number between 1 and 9 (0 to disconnect):");
                }
                columnInput = scanner.readLine();
            }

            if (column == 0) {
                disconnectFromGame();
                break;
            }

            Map<String, String> moveParams = new HashMap<>();
            moveParams.put("playerName", player.getPlayerName());
            moveParams.put("column", String.valueOf(column));

            ResponseEntity moveResponse = httpHandler.sendHttpRequest(POST, MAKE_MOVE_URL, moveParams);

            System.out.println(moveResponse.getMessage());

            if (moveResponse.getResponseCode() == 200) {
                break;
            }
        }

        System.out.println("Goodbye!");
    }

    /**
     * Send a request to the server to remove the player from the game
     */
    private void disconnectFromGame() {

        Map<String, String> params = new HashMap<>();
        params.put("playerName", player.getPlayerName());

        System.out.println(httpHandler.sendHttpRequest(POST, DISCONNECT_URL, params).getMessage());
    }

    /**
     * Take in user input from the console and create a player
     *
     * @return map containing user name and disc color
     */
    private Map<String, String> getPlayerInfo() throws IOException {

        Map<String, String> paramMap = new HashMap<>();

        System.out.print("Name: ");
        String playerName = scanner.readLine();

        while (playerName == null || playerName.trim().equals("")) {
            System.out.println("Name must not be empty or null");
            playerName = scanner.readLine();
        }

        System.out.print("Disc Color (Red | Blue):");
        String discColor = scanner.readLine();

        while (!discColor.equals("Red") && !discColor.equals("Blue")) {
            System.out.println("Disc color must be either Red or Blue");
            discColor = scanner.readLine();
        }

        player = new Player(playerName, discColor);

        paramMap.put("playerName", playerName);
        paramMap.put("discColor", discColor);

        return paramMap;
    }
}
