//package handler;
//
//import model.Player;
//import model.RequestVerb;
//import model.ResponseEntity;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Scanner;
//
//import static handler.HttpHandler.JOIN_URL;
//import static model.RequestVerb.PUT;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//public class InteractionHandlerTest {
//
//    private InteractionHandler interactionHandler = new InteractionHandler();
//    private HttpHandler httpHandlerMock = Mockito.mock(HttpHandler.class);
//    private BufferedReader scanner = Mockito.mock(BufferedReader.class);
//    private Player mockPlayer = new Player("player", "Red");
//
//    @Test
//    void joinGame() throws IOException {
//
//        Map<String, String> params = new HashMap<>();
//        params.put("playerName", mockPlayer.getPlayerName());
//        params.put("discColor", mockPlayer.getDiscColor());
//
//        doNothing().when(scanner.readLine());
//
//        when(httpHandlerMock.sendHttpRequest(PUT, JOIN_URL, params))
//                .thenReturn(new ResponseEntity(200, "OK"));
//
//        interactionHandler.joinGame();
//    }
//}
