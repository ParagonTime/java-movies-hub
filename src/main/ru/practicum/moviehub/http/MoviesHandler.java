package ru.practicum.moviehub.http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.api.ResponseException;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class MoviesHandler extends BaseHttpHandler {

    private static final String EMPTY_TITLE = "Empty title";
    private static final String YEAR_MUST_BE = "Year must be between 1888 and 2026";
    private static final String TOO_LARGE_TITLE = "Title length more that 100 characters";
    private static final String VALIDATION_EXCEPTION = "No valid";
    private static final String MOVIE_NOT_FOUND_MESSAGE = "Movie not found";
    private static final String RESOURCE_NOT_FOUND_MESSAGE = "Resource not found";
    private static final String UNPROCESSABLE_CONTENT_MESSAGE = "Unprocessable Content";
    private static final String UNSUPPORTED_MEDIA_TYPE_MESSAGE = "Unsupported Media Type";
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int YEAR_OF_START_CINEMA = 1888;
    private static final int YEAR_OF_NOW_PLUS_ONE = LocalDate.now().getYear() + 1;
    private static final int HTTP_STATUS_OK = 200;
    private static final int HTTP_STATUS_CREATED = 201;
    private static final int HTTP_STATUS_BAD_REQUEST = 400;
    private static final int HTTP_STATUS_NOT_FOUND = 404;
    private static final int HTTP_STATUS_UNSUPPORTED_MEDIA_TYPE = 415;
    private static final int HTTP_STATUS_UNPROCESSABLE_CONTENT = 422;
    private final Gson gson;

    public MoviesHandler(MoviesStore moviesStore, Gson gson) {
        super(moviesStore);
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        // Напишите реализацию, удовлетворяющую тест
        try {
            switch (ex.getRequestMethod().toUpperCase()) {
                case "POST":
                    callPostMethod(ex);
                    break;
                case "DELETE":
                    callDeleteMovie(ex);
                    break;
                case "GET":
                    if (ex.getRequestURI().toString().contains("year")) {
                        callGetMoviesByYear(ex);
                    } else if (ex.getRequestURI().toString().split("/").length > 2) {
                        callGetMovieById(ex);
                    } else {
                        callGetMovies(ex);
                    }
                    break;
                default:
                    sendJson(ex, HTTP_STATUS_NOT_FOUND, RESOURCE_NOT_FOUND_MESSAGE);
            }
        } catch (ResponseException errorResponse) {
            sendJson(ex, errorResponse.getStatusCode(), errorResponse.getMessage());
        } catch (IOException e) {
            sendNoContent(ex, HTTP_STATUS_BAD_REQUEST);
        }
    }

    private void callDeleteMovie(HttpExchange ex) throws IOException, ResponseException {
        String[] uriSplit = ex.getRequestURI().toString().split("/");
        try {
            int id = Integer.parseInt(uriSplit[2]);
            boolean isDeleted = moviesStore.deleteMovie(id);
            if (isDeleted) {
                sendNoContent(ex);
            } else {
                sendNoContent(ex, HTTP_STATUS_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            throw new ResponseException(VALIDATION_EXCEPTION + " ID: " + uriSplit[2], HTTP_STATUS_BAD_REQUEST);
        }
    }

    private void callGetMovies(HttpExchange ex) throws IOException {
        String data = gson.toJson(moviesStore.getMovies());
        sendJson(ex, HTTP_STATUS_OK, data);
    }

    private void callGetMovieById(HttpExchange ex) throws IOException, ResponseException {
        String[] uriSplit = ex.getRequestURI().toString().split("/");
        try {
            int id = Integer.parseInt(uriSplit[2]);
            Movie movie = moviesStore.getMovieById(id);
            if (movie == null) {
                throw new ResponseException(MOVIE_NOT_FOUND_MESSAGE, HTTP_STATUS_NOT_FOUND);
            }
            String data = gson.toJson(movie);
            sendJson(ex, HTTP_STATUS_OK, data);
        } catch (NumberFormatException e) {
            throw new ResponseException(VALIDATION_EXCEPTION + " ID: " + uriSplit[2], HTTP_STATUS_BAD_REQUEST);
        }
    }

    private void callGetMoviesByYear(HttpExchange ex) throws IOException, ResponseException {
        String[] uriSplit = ex.getRequestURI().toString().split("=");
        try {
            int year = Integer.parseInt(uriSplit[1]);
            if (year < YEAR_OF_START_CINEMA || year > YEAR_OF_NOW_PLUS_ONE) {
                throw new ResponseException(
                        String.format(VALIDATION_EXCEPTION + " " + YEAR_MUST_BE),
                        HTTP_STATUS_BAD_REQUEST
                );
            } else {
                String date = gson.toJson(moviesStore.getMoviesByYear(year));
                sendJson(ex, HTTP_STATUS_OK, date);
            }
        } catch (NumberFormatException e) {
            throw new ResponseException(VALIDATION_EXCEPTION + " year:" + uriSplit[1], HTTP_STATUS_BAD_REQUEST);
        }
    }

    private void callPostMethod(HttpExchange ex) throws IOException, ResponseException {
        ErrorResponse errorResponse = new ErrorResponse(VALIDATION_EXCEPTION + " data");
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null && !contentType.contains("application/json")) {
            throw new ResponseException(UNSUPPORTED_MEDIA_TYPE_MESSAGE, HTTP_STATUS_UNSUPPORTED_MEDIA_TYPE);
        }
        try {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonElement jsonElement = JsonParser.parseString(body);

            if (!jsonElement.isJsonObject()) {
                throw new ResponseException(UNPROCESSABLE_CONTENT_MESSAGE, HTTP_STATUS_UNPROCESSABLE_CONTENT);
            }

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String title = jsonObject.get("title").getAsString().trim();
            int year = jsonObject.get("year").getAsInt();

            if (title.isEmpty()) {
                errorResponse.addDetails(EMPTY_TITLE);
            }
            if (title.length() > MAX_TITLE_LENGTH) {
                errorResponse.addDetails(TOO_LARGE_TITLE);
            }
            if (year < YEAR_OF_START_CINEMA || year > YEAR_OF_NOW_PLUS_ONE) {
                errorResponse.addDetails(YEAR_MUST_BE);
            }
            if (!errorResponse.isDetailsEmpty()) {
                String data = gson.toJson(errorResponse);
                sendJson(ex, HTTP_STATUS_UNPROCESSABLE_CONTENT, data);
                return;
            }

            Movie newMovie = moviesStore.addMovie(title, year);
            String data = gson.toJson(newMovie);
            sendJson(ex, HTTP_STATUS_CREATED, data);

        } catch (JsonSyntaxException e) {
            throw new ResponseException(UNPROCESSABLE_CONTENT_MESSAGE, HTTP_STATUS_UNPROCESSABLE_CONTENT);
        }
    }
}
