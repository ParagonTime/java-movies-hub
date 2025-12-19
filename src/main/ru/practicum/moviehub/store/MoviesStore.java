package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoviesStore {
    private int currentId;
    private final Map<Integer, Movie> storage;

    public MoviesStore() {
        storage = new HashMap<>();
        currentId = 0;
    }

    public Movie addMovie(String title, int year) {
        currentId++;
        Movie newMovie = new Movie(currentId, title, year);
        storage.put(currentId, newMovie);
        return newMovie;
    }

    public List<Movie> getMovies() {
        return storage.values().stream().toList();
    }

    public Movie getMovieById(int id) {
        return storage.get(id);
    }

    public boolean deleteMovie(int id) {
        return storage.remove(id) != null;
    }

    public List<Movie> getMoviesByYear(int year) {
        return storage.values().stream()
                .filter(movie -> movie.year() == year)
                .toList();
    }

    public void clear() {
        storage.clear();
        currentId = 0;
    }
}