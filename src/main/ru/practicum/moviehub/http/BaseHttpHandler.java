package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

abstract public class BaseHttpHandler implements HttpHandler {

    private static final int RESPONSE_LENGTH_NO_CONTENT = -1;
    private static final int HTTP_STATUS_NO_CONTENT = 204;
    protected static final String CT_JSON = "application/json; charset=UTF-8";

    protected final MoviesStore moviesStore;

    public BaseHttpHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        // !!! Реализуйте общий для всех хендлеров метод
        // для отправки ответа с телом в формате JSON
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {
        // !!! Реализуйте общий для всех хендлеров метод
        // для отправки ответа без тела и кодом 204
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(HTTP_STATUS_NO_CONTENT, RESPONSE_LENGTH_NO_CONTENT);
    }

    protected void sendNoContent(HttpExchange ex, int status) throws java.io.IOException {
        // !!! Реализуйте общий для всех хендлеров метод
        // для отправки ответа без тела и кодом 204
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, RESPONSE_LENGTH_NO_CONTENT);
    }
}