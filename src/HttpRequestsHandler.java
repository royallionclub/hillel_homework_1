import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class HttpRequestsHandler {

    private Map<String, String> postMap = new HashMap<>();


    public HttpHandler createHandler() {

        return exchange -> {
            try {

                switch (exchange.getRequestMethod()) {
                    case "POST": {
                        doPost(exchange);
                        break;
                    }
                    case "GET": {
                        doGet(exchange);
                        break;
                    }
                    case "DELETE": {
                        doDelete(exchange);
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
        System.out.println(exchange.getRequestURI() + " processed");
    }

    private void doDelete(HttpExchange exchange) throws IOException {
        postMap.remove(getKey(exchange.getRequestURI().getPath()));
        sendResponse(exchange, "");
    }

    private void doGet(HttpExchange exchange) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        String path = exchange.getRequestURI().getPath();
        if (path.equals(HttpRequests.APP_TXTS)) {
            postMap.forEach((key, value) -> stringBuilder.append(key).append("=").append(value).append("\n"));
            sendResponse(exchange, stringBuilder.toString());
        } else {
            sendResponse(exchange,  postMap.getOrDefault(getKey(path), "key wasn't found"));
        }
    }

    private void doPost(HttpExchange exchange) throws IOException {
        String body = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody())).lines().collect(Collectors.joining());
        String path = exchange.getRequestURI().getPath();

        if (body.isEmpty()) {
            sendResponse(exchange, "");
            return;
        }
        if (path.equals(HttpRequests.APP_TXTS)) {
            if (!postMap.containsValue(body)) {
                postMap.put(String.valueOf(System.currentTimeMillis()), body);
                sendResponse(exchange, "");
            } else {
                for (Map.Entry<String, String> entry : postMap.entrySet()) {
                    if (entry.getValue().equals(body)) {
                        sendResponse(exchange, entry.getKey());
                        return;
                    }
                }
            }
        } else {
            postMap.computeIfPresent(getKey(path), (k, v) -> body);
            sendResponse(exchange, "");
        }
    }

    private String getKey(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
