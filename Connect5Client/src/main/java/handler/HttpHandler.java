package handler;

import model.RequestVerb;
import model.ResponseEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpHandler {

    private static final String BASE_URL = "http://localhost:8080/";

    public static final String JOIN_URL = BASE_URL + "join";
    public static final String MAKE_MOVE_URL = BASE_URL + "make-move";
    public static final String MOVE_STATUS_URL = BASE_URL + "move-status";
    public static final String DISCONNECT_URL = BASE_URL + "disconnect";

    public ResponseEntity sendHttpRequest(final RequestVerb verb, final String urlString,
                                          final Map<String, String> params) {

        try {
            String queryParams = paramMapToBytes(params);

            URL url = new URL(urlString + "?" + queryParams);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(verb.toString());

            return getResponse(conn);

        } catch (IOException ioException) {
            return new ResponseEntity(500, ioException.getMessage());
        }
    }

    /**
     * Retrieves HTTP status code and message from the HTTP response
     *
     * @param connection
     * @return ResponseEntity with a status code and a message
     * @throws IOException
     */
    private ResponseEntity getResponse(HttpURLConnection connection) throws IOException {

        try {
            final String responseMessage = parseHttpMessageToString(connection.getInputStream());

            return new ResponseEntity(connection.getResponseCode(), responseMessage);
        } catch (IOException exception) {
            final String responseMessage = parseHttpMessageToString(connection.getErrorStream());

            return new ResponseEntity(connection.getResponseCode(), responseMessage);
        }

    }

    private String paramMapToBytes(Map<String, String> params) {
        StringBuilder paramString = new StringBuilder();

        for (Map.Entry<String, String> param : params.entrySet()) {
            if (paramString.length() != 0) {
                paramString.append('&');
            }

            paramString.append(param.getKey());
            paramString.append('=');
            paramString.append(param.getValue());
        }
        return paramString.toString();
    }

    /**
     * Parse response from an input stream to a string
     *
     * @param inputStream
     * @return response string
     * @throws IOException
     */
    private String parseHttpMessageToString(InputStream inputStream) {

        try {

            if (inputStream == null) {
                throw new IOException("Could not connect to the server");
            }

            StringBuilder stringBuilder = new StringBuilder();

            Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            for (int c; (c = reader.read()) >= 0; )
                stringBuilder.append((char) c);

            return stringBuilder.toString();
        } catch (IOException ioException) {
            return "Message could not be retrieved";
        }
    }
}
