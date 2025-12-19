package ru.practicum.moviehub.api;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {
    private final String error;
    private final List<String> details;

    public ErrorResponse(String error) {
        this.error = error;
        details = new ArrayList<>();
    }

    public String getError() {
        return error;
    }

    public List<String> getDetails() {
        return details;
    }

    public void addDetails(String detail) {
        details.add(detail);
    }

    public boolean isDetailsEmpty() {
        return details.isEmpty();
    }
}
