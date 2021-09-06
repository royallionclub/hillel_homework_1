import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HttpRequests {

    static final String APP = "/app";
    static final String APP_TXTS = "/app/txts";
    
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getProperty("port", "8080"));

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        HttpHandler httpHandler = exchange -> {
            try {
                System.out.println("processing " + exchange.getRequestURI());
                String query1 = exchange.getRequestURI().getQuery();
                Map<String, String> requestParams = Optional.ofNullable(query1).stream()
                        .flatMap(query -> Arrays.stream(query.split("&")))
                        .filter(param -> param.indexOf("=") > 0)
                        .collect(Collectors.toMap(
                                param -> param.substring(0, param.indexOf("=")),
                                param -> param.substring(param.indexOf("=") + 1)
                        ));

                String resp = doBusiness(requestParams);

                String s = String.format("<html><body><h1>%s</h1></body></html>", resp);

                exchange.sendResponseHeaders(200, s.length());
                exchange.getResponseBody().write(s.getBytes());
                exchange.close();
                System.out.println(exchange.getRequestURI() + " processed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        httpServer.createContext(APP, httpHandler);
        httpServer.createContext(APP_TXTS, new HttpRequestsHandler().createHandler());
        httpServer.start();
    }

    private static String doBusiness(Map<String, String> requestParams) {
        String recipient = requestParams.getOrDefault("name", "world");
        return String.format("Hello, %s!", recipient);
    }

}
