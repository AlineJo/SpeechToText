package com.programining.speechtotext.apimodel;

import java.util.ArrayList;

public class AlternativesHolder {

    private ArrayList<Alternatives> alternatives = new ArrayList<>();

    public ArrayList<Alternatives> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(ArrayList<Alternatives> alternatives) {
        this.alternatives = alternatives;
    }
}
