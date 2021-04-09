import handler.InteractionHandler;

import java.io.IOException;

public class Connect5ClientMain {

    private static InteractionHandler interactionHandler = new InteractionHandler();

    public static void main(String[] args) throws IOException {

        interactionHandler.joinGame();

        interactionHandler.play();
    }
}
