package com.programining.speechtotext.apimodel;

import java.util.ArrayList;

public class ResultContainer {

    private ArrayList<AlternativesHolder> results;

    public ResultContainer() {
        results = new ArrayList();
    }

    public ArrayList<AlternativesHolder> getResults() {
        return results;
    }

    public void setResults(ArrayList<AlternativesHolder> results) {
        this.results = results;
    }
}
