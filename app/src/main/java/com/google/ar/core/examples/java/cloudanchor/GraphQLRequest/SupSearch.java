package com.google.ar.core.examples.java.cloudanchor.GraphQLRequest;

import java.util.List;

public class SupSearch {
    private int hits;
    private List<Result> results;

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}
