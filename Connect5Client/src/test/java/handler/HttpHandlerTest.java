//package handler;
//
//import model.RequestVerb;
//import model.ResponseEntity;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.lang.reflect.Method;
//import java.util.HashMap;
//import java.util.Map;
//
//public class HttpHandlerTest {
//
//    private static final String BASE_URL = "http://localhost:8081/";
//
//    private static final String JOIN_URL = BASE_URL + "join";
//    private static final String MAKE_MOVE_URL = BASE_URL + "make-move";
//    private static final String MOVE_STATUS_URL = BASE_URL + "move-status";
//    private static final String DISCONNECT_URL = BASE_URL + "disconnect";
//
//    private HttpHandler httpHandler = new HttpHandler();
//    Map<String, String> params = new HashMap<>();
//
//    @BeforeEach
//    void setUp() throws IOException {
//
////        MockWebServer server = new MockWebServer();
////        server.start(8081);
////        params.put("playerName", "playerName");
////
////        final Dispatcher dispatcher = new Dispatcher() {
////
////            @Override
////            public MockResponse dispatch (RecordedRequest request) {
////
////                if (request.getPath().contains("join")) {
////                    return new MockResponse().setResponseCode(200).setBody("Joined Game");
////                }
////
////                return new MockResponse().setResponseCode(404);
////            }
////        };
////        server.setDispatcher(dispatcher);
//
//    }
//
//    @Test
//    void test() {
//
//        Class response = HttpHandler.class.getClass();
//        Method[] methods = response.getMethods();
//    }
//}
