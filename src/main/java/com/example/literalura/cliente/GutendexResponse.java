package com.example.literalura.cliente;

import java.util.List;

public class GutendexResponse {
    private int count;
    private List<GutendexBook> results;

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public List<GutendexBook> getResults() { return results; }
    public void setResults(List<GutendexBook> results) { this.results = results; }
}
