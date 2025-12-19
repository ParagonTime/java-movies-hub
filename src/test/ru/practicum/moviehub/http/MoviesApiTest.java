package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {

    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client = null;

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(new MoviesStore(), 8080);
        server.start();
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    @BeforeEach
    void beforeEach() {
        // добавить очистку хранилища store
        server.clearMovieStore();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_whenNotEmpty_returnsNotEmptyArray() throws Exception {
        addMovies();
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]") && body.length() > 2,
                "Ожидается JSON-массив");
    }

    @Test
    void getMoviesById_whenEmptyStore_returnsNotFound() throws Exception {
        int id = 144;
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/" + id))
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(404, resp.statusCode(), "GET /movies/id должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.equalsIgnoreCase("Movie not found"),
                "Ожидается сообщение");
    }

    @Test
    void getMoviesById_whenNoValidId_returnsBadRequest() throws Exception {
        String id = "2sdAsa122";
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/" + id))
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(400, resp.statusCode(), "GET /movies/id должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("No valid"),
                "Ожидается сообщение");
    }

    @Test
    void getMoviesById_whenNoEmptyStoreAndValidId_returnsMovie() throws Exception {
        addMovies();
        int id = 1;
        // создайте объект GET-запроса на эндпоинт /movies
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/" + id))
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(200, resp.statusCode(), "GET /movies/id должен вернуть 200");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.contains("Movie0") && body.contains("1990"),
                "Ожидается сообщение");
    }

    @Test
    void deleteMovie_whenNoMovie_returnsNotFound() throws Exception {
        String id = "2122";
        // создайте объект GET-запроса на эндпоинт /movies
        HttpRequest req = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/" + id))
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(404, resp.statusCode(), "DELETE /movies/id должен вернуть 404");
        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.isEmpty(),
                "Ожидается пустое body");
    }

    @Test
    void deleteMovie_whenNoValidId_returnsBadRequest() throws Exception {
        String id = "2122s";
        // создайте объект GET-запроса на эндпоинт /movies
        HttpRequest req = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/" + id))
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(400, resp.statusCode(), "DELETE /movies/id должен вернуть 400");
        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.startsWith("No valid"),
                "Ожидается сообщение");
    }

    @Test
    void deleteMovie_whenValidId_returnsNoContent() throws Exception {
        addMovies();
        String id = "1";
        // создайте объект GET-запроса на эндпоинт /movies
        HttpRequest req = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/" + id))
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(204, resp.statusCode(), "DELETE /movies/id должен вернуть 204");
        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.isEmpty(),
                "Ожидается пустое body");
    }

    @Test
    void getMoviesByYear_whenValidYearAndNoMoviesInStore_returnsEmptyArrayInBody() throws Exception {
        String year = "1990";
        // создайте объект GET-запроса на эндпоинт /movies
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=" + year))
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(200, resp.statusCode(), "GET /movies?year={year} должен вернуть 200");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается пустой массив");
    }

    @Test
    void getMoviesByYear_whenNoValidYearAfterNowYear_returnsBadRequest() throws Exception {
        String year = "19900";
        // создайте объект GET-запроса на эндпоинт /movies
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=" + year))
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(400, resp.statusCode(), "GET /movies?year={year} должен вернуть 400");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.startsWith("No valid"),
                "Ожидается пустой массив");
    }

    @Test
    void getMoviesByYear_whenNoValidYearBefore1888_returnsBadRequest() throws Exception {
        String year = "1887";
        // создайте объект GET-запроса на эндпоинт /movies
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=" + year))
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(400, resp.statusCode(), "GET /movies?year={year} должен вернуть 400");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.startsWith("No valid"),
                "Ожидается пустой массив");
    }

    @Test
    void getMoviesByYear_whenValidYear_returnsOK200() throws Exception {
        addMovies();
        String year = "1990";
        // создайте объект GET-запроса на эндпоинт /movies
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=" + year))
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(200, resp.statusCode(), "GET /movies?year={year} должен вернуть 200");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]") && body.length() > 2,
                "Ожидается пустой массив");
    }

    @Test
    void postMovie_whenAllValidFields_returnsOK200() throws Exception {
        // создайте объект GET-запроса на эндпоинт /movies
        String title = "title";
        int year = 1900;
        Gson gson = new Gson();
        MovieEntry entry = new MovieEntry(title, year);
        String jsonBody = gson.toJson(entry);
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json; charset=UTF-8")
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        Movie movie = gson.fromJson(body, Movie.class);
        assertTrue(movie.title().equals(title) && movie.year() == year,
                "Ожидается Movie объект");
    }

    @Test
    void postMovie_whenNotValidFields_returnsUnprocessableEntity() throws Exception {
        String title = "title".repeat(21);
        int year = 2900;
        Gson gson = new Gson();
        MovieEntry entry = new MovieEntry(title, year);
        String jsonBody = gson.toJson(entry);
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json; charset=UTF-8")
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);
        assertTrue(errorResponse.getDetails().contains("Year must be between 1888 and 2026"));
        assertTrue(errorResponse.getDetails().contains("Title length more that 100 characters"));
        assertTrue(errorResponse.getError().equalsIgnoreCase("No valid data"));
    }

    // написать функцию по добавлению movies для тестов
    private void addMovies() throws IOException, InterruptedException {

        for (int i = 0; i < 20; i++) {
            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(new MovieEntry("Movie" + i, 1990 + i))))
                    .uri(URI.create(BASE + "/movies"))
                    .headers("Content-Type", "application/json; charset=UTF-8")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            client.send(req, responseBodyHandler);
        }
        for (int i = 0; i < 20; i++) {
            HttpRequest req = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(new MovieEntry("Movie_" + i, 1990 + i))))
                    .uri(URI.create(BASE + "/movies"))
                    .headers("Content-Type", "application/json; charset=UTF-8")
                    .build();

            HttpResponse.BodyHandler<String> responseBodyHandler =
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            client.send(req, responseBodyHandler);
        }

    }


    static class MovieEntry {
        String title;
        int year;

        public MovieEntry(String title, int year) {
            this.title = title;
            this.year = year;
        }
    }
}